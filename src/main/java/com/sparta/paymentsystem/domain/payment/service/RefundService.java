package com.sparta.paymentsystem.domain.payment.service;

import com.sparta.paymentsystem.domain.payment.entity.Payment;
import com.sparta.paymentsystem.domain.payment.entity.Refund;
import com.sparta.paymentsystem.domain.payment.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;

    @Transactional
    public void createRefund(Payment payment, String cancelReason, LocalDateTime refundedAt) {
        Refund refund = new Refund(payment, cancelReason, refundedAt);
        refundRepository.save(refund);
    }

}

