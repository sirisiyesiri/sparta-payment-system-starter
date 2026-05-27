package com.sparta.paymentsystem.domain.payment.port;

public record PaymentGatewayResponse(
        String id,
        String status,
        int totalAmount
) {}