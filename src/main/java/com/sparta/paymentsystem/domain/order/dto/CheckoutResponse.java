package com.sparta.paymentsystem.domain.order.dto;

import java.util.List;

public record CheckoutResponse(
        List<CheckoutItemResponse> items,
        int totalPrice
) {
    public record CheckoutItemResponse(
            Long productId,
            String productName,
            int price,
            int quantity,
            int subtotal
    ) {}
}

