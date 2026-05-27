package com.sparta.paymentsystem.domain.order.dto;

public record OrderItemResponse(
        String productName,
        int orderPrice,
        int quantity
) {}

