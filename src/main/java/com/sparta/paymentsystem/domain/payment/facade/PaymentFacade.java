package com.sparta.paymentsystem.domain.payment.facade;

import com.sparta.paymentsystem.domain.order.entity.Order;
import com.sparta.paymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.sparta.paymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.sparta.paymentsystem.domain.payment.entity.Payment;
import com.sparta.paymentsystem.domain.payment.entity.PaymentStatus;
import com.sparta.paymentsystem.domain.payment.port.PaymentGateway;
import com.sparta.paymentsystem.domain.payment.port.PaymentGatewayResponse;
import com.sparta.paymentsystem.domain.payment.service.PaymentCommandService;
import com.sparta.paymentsystem.domain.payment.service.PaymentService;
import com.sparta.paymentsystem.global.error.BusinessException;
import com.sparta.paymentsystem.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 결제 승인 유스케이스 Facade
 *
 * "결제 승인"은 단순히 DB 상태만 바꾸는 게 아니다.
 *   - 우리 DB의 결제/주문 상태
 *   - PG사(PortOne)의 실제 결제 상태/금액
 * 두 세계를 맞춰 보고, 조작/오류가 감지되면 즉시 취소까지 해야 하는 보안 로직이다.
 *
 * 이 클래스는 "외부 API 호출(트랜잭션 밖)"과 "DB 상태 변경(트랜잭션 안)"을 섞지 않기 위해
 * 트랜잭션을 걸지 않고, DB 변경은 PaymentCommandService 로 위임한다.
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {

    // PortOne 결제 완료 상태값. 문자열 비교 시 매직 스트링을 피하기 위해 상수화.
    private static final String PG_STATUS_PAID = "PAID";

    private final PaymentService paymentService;
    private final PaymentCommandService paymentCommandService;
    private final PaymentGateway paymentGateway;

    /**
     * 결제 승인 메인 흐름
     *
     * 1. 주문/결제 조회 + 소유자/상태/portonePaymentId 검증
     * 2. [외부 API] PG사에서 실제 결제 정보 조회
     * 3. PortOne 상태 검증 : PAID 가 아니면 결제/주문 실패 처리
     * 4. 금액 검증 : DB 금액과 PG 금액 불일치 시 PG 자동 취소 + 결제/주문 실패 처리
     * 5. [트랜잭션] 모두 통과하면 DB 상태를 PAID/CONFIRMED 로 변경
     *
     * 주의: 이 메서드 자체에는 @Transactional 이 없다.
     * 외부 API 호출을 트랜잭션 안에서 하면 커넥션을 오래 점유하고, 롤백 시에도 PG 상태를 되돌릴 수 없기 때문
     */
    public PaymentConfirmResponse confirmPayment(Long memberId, PaymentConfirmRequest request) {
        // 1. 결제 조회 + 소유자 검증
        //    다른 사람의 주문을 승인하지 못하게 memberId 를 함께 확인한다.
        Payment payment = paymentService.findByOrderIdWithOrder(request.orderId());
        Order order = payment.getOrder();
        if (!order.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 1-1. 이미 처리된 결제인지 선검증
        //      IN_PROGRESS 가 아니면 이미 승인/취소/실패된 건이므로 PG 호출 자체를 하지 않는다.
        //      (중복 클릭/재요청으로 인한 불필요한 외부 API 호출 방지)
        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        // 1-2. 클라이언트가 보낸 portonePaymentId 와 DB 값 일치 검증
        //      악의적인 사용자가 남의 결제 id 로 승인 요청을 보내는 것을 막는다.
        String portonePaymentId = payment.getPortonePaymentId();
        if (!portonePaymentId.equals(request.portonePaymentId())) {
            log.warn("결제 승인 거부 — portonePaymentId 불일치: DB={}, 요청={}",
                    portonePaymentId, request.portonePaymentId());
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        // 2. 외부 API: PG사에서 실제 결제 정보 조회
        //    "우리 DB 가 말하는 결제"가 아니라 "PG 가 말하는 결제"를 신뢰한다.
        PaymentGatewayResponse pgPayment = paymentGateway.getPayment(portonePaymentId);

        // 3. PortOne 상태 검증
        //    PG가 PAID 라고 응답하지 않으면 결제가 정상 완료된 것이 아니다.
        //    → 우리 DB 의 결제/주문을 실패 처리하고, 재고도 복구해 둔다.
        if (!PG_STATUS_PAID.equals(pgPayment.status())) {
            log.error("결제 승인 실패 — PG 상태 비정상: paymentId={}, pgStatus={}",
                    payment.getId(), pgPayment.status());
            paymentCommandService.failPaymentAndOrder(order.getId());
            throw new BusinessException(ErrorCode.PAYMENT_NOT_PAID);
        }

        // 4. 금액 검증 ("금액 조작 문제" 해결)
        //    프론트에서 가격을 조작해 결제창을 띄웠을 수 있으므로
        //    DB 에 기록된 "서버가 계산한 금액"과 PG 가 실제 받은 금액을 대조한다.
        //    불일치 시:
        //      1) PG 측 결제를 즉시 취소 (돈을 돌려줘야 함)
        //      2) 우리 DB 결제/주문도 실패 처리
        //    PG 취소 호출이 실패해도 로그만 남기고 흐름을 이어간다.
        //    돈이 이미 나갔을 수 있으므로 운영자 수동 대응이 필요
        if (payment.getAmount() != pgPayment.totalAmount()) {
            log.error("결제 승인 실패 — 금액 불일치 (조작 가능성): paymentId={}, DB금액={}, PG금액={}",
                    payment.getId(), payment.getAmount(), pgPayment.totalAmount());
            try {
                paymentGateway.cancelPayment(portonePaymentId, "결제 금액 불일치 자동 취소");
            } catch (Exception e) {
                log.error("PG 자동 취소 실패 : 수동 처리 필요: portonePaymentId={}", portonePaymentId, e);
            }
            paymentCommandService.failPaymentAndOrder(order.getId());
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 5. 모든 검증 통과 → DB 상태를 최종 승인으로 전환
        //    실제 상태 변경은 트랜잭션이 걸린 CommandService 에서 수행한다.
        return paymentCommandService.approvePaymentAndOrder(order.getId());
    }

}
