package com.sparta.paymentsystem.domain.cart.dto;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        int price,
        int quantity,
        int stock
) {}