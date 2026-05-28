package com.sparta.paymentsystem.domain.payment.entity;

import com.sparta.paymentsystem.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "refunded_at", nullable = false)
    private LocalDateTime refundedAt;

    public Refund(Payment payment, String reason, LocalDateTime refundedAt) {
        this.payment = payment;
        this.reason = reason;
        this.refundedAt = refundedAt;
    }

}
