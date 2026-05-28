package com.sparta.paymentsystem.domain.payment.dto;

public record PaymentCancelResponse(
        Long paymentId,
        Long orderId,
        String portonePaymentId,
        String paymentStatus,
        String orderStatus,
        String message
) {}
