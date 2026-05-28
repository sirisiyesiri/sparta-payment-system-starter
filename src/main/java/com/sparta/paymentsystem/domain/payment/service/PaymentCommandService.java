package com.sparta.paymentsystem.domain.payment.service;

import com.sparta.paymentsystem.domain.order.entity.Order;
import com.sparta.paymentsystem.domain.order.entity.OrderItem;

import com.sparta.paymentsystem.domain.order.service.OrderService;
import com.sparta.paymentsystem.domain.payment.dto.PaymentCancelResponse;
import com.sparta.paymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.sparta.paymentsystem.domain.payment.entity.Payment;
import com.sparta.paymentsystem.domain.product.entity.Product;
import com.sparta.paymentsystem.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 결제 승인/실패 시 여러 도메인(결제, 주문, 상품)을 한 트랜잭션으로 묶어 처리하는 서비스
 *
 * - PaymentService: 결제 상태 변경
 * - OrderService: 주문 상태 변경
 * - ProductService: 재고 복구
 *
 * 개별 서비스는 자기 도메인만 책임지고, 여러 도메인에 걸친 "유스케이스"는 이 클래스가 조립한다.
 */
@Service
@RequiredArgsConstructor
public class PaymentCommandService {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductService productService;
    private final RefundService refundService;

    /**
     * 결제 실패 처리
     *
     * 흐름:
     *   1) 주문 ID로 결제 + 주문을 함께 조회 (fetch join)
     *   2) 결제 상태 → FAILED
     *   3) 주문 상태 → CANCELED
     *   4) 주문에 포함된 상품들의 재고를 원래대로 복구
     *
     * 주문 생성 시 차감했던 재고를 되돌려 놓아야 다른 고객이 구매할 수 있으므로 재고 복구가 필수다.
     * - @Transactional 로 묶어 전체가 원자적으로 실행된다.
     */
    @Transactional
    public void failPaymentAndOrder(Long orderId) {
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        Order order = payment.getOrder();

        paymentService.failPayment(payment);
        orderService.cancelOrder(order);

        restoreStock(order);
    }

    /**
     * 결제 승인 처리
     *
     * 흐름:
     *   1) 주문 ID로 결제 + 주문을 함께 조회 (fetch join)
     *   2) 결제 상태 → PAID
     *   3) 주문 상태 → CONFIRMED
     *   4) 화면/클라이언트에 내려줄 응답 DTO 생성
     *
     * 승인은 PG사 검증이 끝난 뒤 최종적으로 "결제 완료"를 확정 짓는 단계
     * 여기서 상태가 바뀌어야 이후 배송/취소 흐름이 이어진다.
     */
    @Transactional
    public PaymentConfirmResponse approvePaymentAndOrder(Long orderId) {
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        Order order = payment.getOrder();

        paymentService.confirmPayment(payment);
        orderService.confirmOrder(order);

        return new PaymentConfirmResponse(
                payment.getId(),
                orderId,
                payment.getAmount(),
                payment.getStatus().name(),
                order.getStatus().name(),
                "결제가 완료되었습니다."
        );
    }

    /**
     * 주문에 담긴 상품들의 재고를 복구한다.
     *
     * 주문이 생성될 때 상품 재고가 수량만큼 차감되므로,
     * 결제가 실패해서 주문이 취소되면 차감된 만큼 다시 더해 줘야 한다.
     * OrderItem은 상품의 "스냅샷"이므로 실제 재고 변경은 Product 엔티티에서 수행한다.
     */
    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = productService.findProductEntity(item.getProductId());
            product.restoreStock(item.getQuantity());
        }
    }

    /**
     * 결제 전액 취소 처리
     *
     * 흐름:
     *   1) paymentId 로 결제 + 주문을 함께 조회 (fetch join)
     *   2) 결제 상태 → CANCELED
     *   3) 주문 상태 → CANCELED
     *   4) 주문 상품들의 재고 복구
     *   5) 환불 이력(Refund) 생성 — 언제, 왜, 얼마를 돌려줬는지 기록
     *   6) 클라이언트에 내려줄 응답 DTO 반환
     *
     * 취소는 단순히 상태만 바꾸는 게 아니라 "환불 이력"을 남겨야 한다.
     * 고객 CS 대응, 정산, 분쟁 대응을 위해 누가/언제/왜 취소했는지 추적 가능해야 하기 때문이다.
     * 이 모든 단계는 @Transactional 로 묶여 하나라도 실패하면 전부 롤백된다.
     *
     */
    @Transactional
    public PaymentCancelResponse cancelPaymentAndOrder(Long paymentId, String cancelReason) {
        Payment payment = paymentService.findByIdWithOrder(paymentId);
        Order order = payment.getOrder();

        paymentService.cancelPayment(payment);
        orderService.cancelOrder(order);
        restoreStock(order);

        refundService.createRefund(payment, cancelReason, LocalDateTime.now());

        return new PaymentCancelResponse(
                paymentId,
                order.getId(),
                payment.getPortonePaymentId(),
                payment.getStatus().name(),
                order.getStatus().name(),
                "결제가 취소되었습니다"
        );
    }
}
