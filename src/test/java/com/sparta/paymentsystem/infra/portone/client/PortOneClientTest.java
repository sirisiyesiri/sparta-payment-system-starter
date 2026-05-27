package com.sparta.paymentsystem.infra.portone.client;

import com.sparta.paymentsystem.domain.payment.port.PaymentGateway;
import com.sparta.paymentsystem.domain.payment.port.PaymentGatewayResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
class PortOneClientTest {

    @Autowired
    private PaymentGateway paymentGateway;

    private static final String PAYMENT_ID = "TC-1에서_복사한_paymentId";

    @Test
    @DisplayName("TC-2. PortOne 결제 조회 — TC-1 결제 후 paymentId로 검증")
    void getPayment_TC1_paymentId() {
        PaymentGatewayResponse response = paymentGateway.getPayment(PAYMENT_ID);

        System.out.println("status     : " + response.status());
        System.out.println("totalAmount: " + response.totalAmount());

        assertThat(response.id()).isEqualTo(PAYMENT_ID);
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.totalAmount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("TC-3. PortOne 결제 취소 — TC-2 조회 확인 후 실행")
    void cancelPayment_TC1_paymentId() {
        // 취소 실행 (예외 없이 완료되면 성공)
        assertThatNoException().isThrownBy(() ->
                paymentGateway.cancelPayment(PAYMENT_ID, "테스트 취소")
        );

        // 취소 후 상태 재조회
        PaymentGatewayResponse response = paymentGateway.getPayment(PAYMENT_ID);
        assertThat(response.status()).isEqualTo("CANCELLED");
    }
}
