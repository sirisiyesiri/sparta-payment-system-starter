package com.sparta.paymentsystem.domain.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long paymentId,
        int totalPrice,
        String status,
        String orderName,
        LocalDateTime createdAt,
        List<OrderItemResponse> orderItems
) {}

