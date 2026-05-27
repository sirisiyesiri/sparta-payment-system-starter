package com.sparta.paymentsystem.practice;

import com.sparta.paymentsystem.practice.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class DummyJsonRestClientTest {

    private RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.create("https://dummyjson.com");
    }

    @Test
    @DisplayName("상품 단건 조회 — 정상 응답")
    void 상품_단건_조회() {
        ProductResponse product = client.get()
                .uri("/products/{id}", 1)
                .retrieve()
                .body(ProductResponse.class);

        assertThat(product).isNotNull();
        assertThat(product.id()).isEqualTo(1);
        assertThat(product.price()).isPositive();
        assertThat(product.stock()).isNotNegative();
        assertThat(product.title()).isNotBlank();

        System.out.println("상품명: " + product.title());
        System.out.println("가격: " + product.price());
        System.out.println("재고: " + product.stock());
        System.out.println("카테고리: " + product.category());
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 — 4xx 예외 발생")
    void 존재하지_않는_상품_조회시_예외() {
        assertThatThrownBy(() ->
                client.get()
                        .uri("/products/{id}", 9999)
                        .retrieve()
                        .body(ProductResponse.class)
        ).isInstanceOf(HttpClientErrorException.class);
    }
}