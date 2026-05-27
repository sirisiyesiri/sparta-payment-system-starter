package com.sparta.paymentsystem.practice;

import com.sparta.paymentsystem.practice.dto.ExchangeRateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateRestClientTest {

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.create("https://open.er-api.com");
    }

    @Test
    @DisplayName("USD 기준 최신 환율 조회 — KRW 포함 확인")
    void USD_KRW_환율_조회() {
        ExchangeRateResponse response = client.get()
                .uri("/v6/latest/{base}", "USD")
                .retrieve()
                .body(ExchangeRateResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.base()).isEqualTo("USD");
        assertThat(response.rates()).containsKey("KRW");
        assertThat(response.rates().get("KRW")).isPositive();

        System.out.println("기준 통화: " + response.base());
        System.out.println("날짜: " + response.date());
        System.out.println("USD → KRW: " + response.rates().get("KRW"));
    }

    @Test
    @DisplayName("EUR 기준 최신 환율 조회 — USD, KRW 포함 확인")
    void EUR_복수통화_환율_조회() {
        ExchangeRateResponse response = client.get()
                .uri("/v6/latest/{base}", "EUR")
                .retrieve()
                .body(ExchangeRateResponse.class);

        assertThat(response.base()).isEqualTo("EUR");
        assertThat(response.rates()).containsKeys("USD", "KRW");

        System.out.println("EUR → USD: " + response.rates().get("USD"));
        System.out.println("EUR → KRW: " + response.rates().get("KRW"));
    }
}
