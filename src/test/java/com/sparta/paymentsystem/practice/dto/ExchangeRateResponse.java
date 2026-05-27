package com.sparta.paymentsystem.practice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * ExchangeRate-API
 * - base_code → base 로 매핑
 * - rates: 통화코드 → 환율 Map (예: { "KRW": 1380.5 })
 */
public record ExchangeRateResponse(
        @JsonProperty("base_code") String base,
        @JsonProperty("time_last_update_utc") String date,
        Map<String, Double> rates
) {}
