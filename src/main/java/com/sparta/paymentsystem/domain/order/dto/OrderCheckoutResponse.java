package com.sparta.paymentsystem.domain.order.dto;

public record OrderCheckoutResponse(
        Long orderId,
        String portonePaymentId,
        int totalPrice,
        String orderName,
        String status
) {}

