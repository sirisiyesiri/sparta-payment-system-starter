package com.sparta.paymentsystem.domain.order.service;

import com.sparta.paymentsystem.domain.member.entity.Member;
import com.sparta.paymentsystem.domain.order.dto.OrderItemResponse;
import com.sparta.paymentsystem.domain.order.dto.OrderResponse;
import com.sparta.paymentsystem.domain.order.entity.Order;
import com.sparta.paymentsystem.domain.order.entity.OrderItem;
import com.sparta.paymentsystem.domain.order.repository.OrderRepository;
import com.sparta.paymentsystem.global.error.BusinessException;
import com.sparta.paymentsystem.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    // 주문 생성
    @Transactional
    public Order createOrder(Member member, List<OrderItem> orderItems, int totalPrice) {
        Order order = new Order(member, totalPrice, orderItems);
        return orderRepository.save(order);
    }

    // 내 주문 목록 조회 (최신순)
    public List<Order> findOrderEntities(Long memberId) {
        return orderRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    // 주문 단건 상세 조회 : orderId만으로 조회
    public Order findOrderEntity(Long orderId) {
        return orderRepository.findByIdWithOrderItems(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    // Order -> OrderResponse 변환, OrderItem -> OrderItemResponse 변환
    public OrderResponse toResponse(Order order, Long paymentId) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(oi -> new OrderItemResponse(oi.getProductName(), oi.getOrderPrice(), oi.getQuantity()))
                .toList();
        return new OrderResponse(
                order.getId(),
                paymentId,
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getOrderName(),
                order.getCreatedAt(),
                items
        );
    }

}
