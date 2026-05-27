package com.sparta.paymentsystem.domain.payment.entity;

import com.sparta.paymentsystem.domain.order.entity.Order;
import com.sparta.paymentsystem.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "portone_payment_id", nullable = false, unique = true, length = 200)
    private String portonePaymentId;

    @Column(nullable = false, columnDefinition = "int UNSIGNED")
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.IN_PROGRESS;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public Payment(Order order, int amount) {
        this.order = order;
        this.amount = amount;
        this.portonePaymentId = generatePortonePaymentId();
    }

    private static String generatePortonePaymentId() {
        return "pay_" + UUID.randomUUID();
    }

}

