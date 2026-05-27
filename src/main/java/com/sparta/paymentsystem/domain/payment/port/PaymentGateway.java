package com.sparta.paymentsystem.domain.payment.port;

public interface PaymentGateway {

    // PG사에서 실제 결제 정보 조회 (금액 검증용)
    PaymentGatewayResponse getPayment(String paymentId);

    // 결제 전액 취소
    void cancelPayment(String paymentId, String reason);
}
