package com.sparta.paymentsystem.domain.order.entity;

import com.sparta.paymentsystem.domain.member.entity.Member;
import com.sparta.paymentsystem.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "total_price", nullable = false, columnDefinition = "int UNSIGNED")
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order(Member member, int totalPrice, List<OrderItem> orderItems) {
        this.member = member;
        this.totalPrice = totalPrice;
        orderItems.forEach(this::addOrderItem);
    }

    public Long getMemberId() {
        return member.getId();
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public String getOrderName() {
        if (orderItems.isEmpty()) return "주문";
        String firstName = orderItems.get(0).getProductName();
        if (orderItems.size() == 1) return firstName;
        return firstName + " 외 " + (orderItems.size() - 1) + "건";
    }

}

