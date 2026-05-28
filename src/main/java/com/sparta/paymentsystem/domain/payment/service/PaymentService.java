package com.sparta.paymentsystem.domain.payment.service;

import com.sparta.paymentsystem.domain.order.entity.Order;
import com.sparta.paymentsystem.domain.payment.entity.Payment;
import com.sparta.paymentsystem.domain.payment.repository.PaymentRepository;
import com.sparta.paymentsystem.global.error.BusinessException;
import com.sparta.paymentsystem.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // 결제 생성
    @Transactional
    public Payment createPayment(Order order, int amount) {
        Payment payment = new Payment(order, amount);
        return paymentRepository.save(payment);
    }

    // 주문 단건 조회 화면에서 결제 ID 조회
    public Optional<Long> findPaymentIdByOrderId(Long orderId) {
        return paymentRepository.findIdByOrderId(orderId);
    }

    // 주문 목록에 결제 ID를 붙이기 위한 "Order → Payment" 조회
    public Map<Long, Long> findPaymentIdMapByOrderIds(List<Long> orderIds) {
        //  IN () 쿼리가 DB로 나가는 걸 차단하고 조기 반환
        if (orderIds.isEmpty()) return Map.of();
        // Repository의 [orderId, paymentId] 튜플(Object[])을 Map<OrderId, PaymentId>로 재구성
        return paymentRepository.findIdsByOrderIds(orderIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    // 주문 단건 상세 조회 : orderId만으로 조회
    public Payment findByOrderIdWithOrder(Long orderId) {
        return paymentRepository.findByOrderIdWithOrder(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    // 결제 상태 변경 (PAID)
    @Transactional
    public void confirmPayment(Payment payment) {
        payment.markAsPaid();
    }

    // 결제 상태 변경 (FAILED)
    @Transactional
    public void failPayment(Payment payment) {
        payment.markAsFailed();
    }

    // 결제 상태 변경 (CANCELLED)
    @Transactional
    public void cancelPayment(Payment payment) {
        payment.markAsCancelled();
    }

    // 결제 정보와 연관된 주문 정보를 paymentId 기반으로 조회
    public Payment findByIdWithOrder(Long paymentId) {
        return paymentRepository.findByIdWithOrder(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}

