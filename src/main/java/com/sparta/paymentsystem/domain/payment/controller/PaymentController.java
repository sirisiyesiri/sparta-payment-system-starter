package com.sparta.paymentsystem.domain.payment.controller;

import com.sparta.paymentsystem.domain.payment.dto.PaymentCancelRequest;
import com.sparta.paymentsystem.domain.payment.dto.PaymentCancelResponse;
import com.sparta.paymentsystem.domain.payment.dto.PaymentConfirmRequest;
import com.sparta.paymentsystem.domain.payment.dto.PaymentConfirmResponse;
import com.sparta.paymentsystem.domain.payment.facade.PaymentFacade;
import com.sparta.paymentsystem.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentFacade paymentFacade;

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PaymentConfirmResponse>> confirmPayment(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PaymentConfirmRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(paymentFacade.confirmPayment(memberId, request)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PaymentCancelResponse>> cancelPayment(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long id,
            @Valid @RequestBody(required = false) PaymentCancelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(paymentFacade.cancelPayment(memberId, id, request)));
    }
}

