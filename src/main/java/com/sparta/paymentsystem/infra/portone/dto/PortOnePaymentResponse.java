package com.sparta.paymentsystem.infra.portone.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PortOne V2 결제 조회 응답 (GET /payments/{paymentId})
 *
 * 이 프로젝트에서 필요한 핵심 필드만 선언했습니다.
 *
 * Jackson의 @JsonIgnoreProperties(ignoreUnknown = true)로
 * 여기에 없는 필드는 자동 무시됩니다.
 *
 * 전체 응답 필드는 PortOne V2 API 문서를 참고하세요.
 * https://developers.portone.io/api/rest-v2/payment
 *
 * 주요 생략 필드:
 * - transactionId: PortOne 거래 고유 채번 ID
 * - method: 결제수단 정보 (PaymentMethod)
 * - merchantId: 고객사 ID
 * - customer: 구매자 정보 (Customer)
 * - channel: 선택된 채널 정보
 * - scheduleId: 결제 예약 건 아이디 (결제 예약을 이용한 경우에만 존재)
 * - billingKey: 결제 시 사용된 빌링키 (빌링키 결제인 경우에만 존재)
 * - webhooks: 웹훅 발송 내역 (Array<PaymentWebhook>)
 * - promotionId: 프로모션 ID
 * - requestedAt, updatedAt, statusChangedAt : 각 시점 타임스탬프
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentResponse(
        String id,                            // 결제 건 ID (우리가 생성한 paymentId)
        String status,                        // 결제 상태: READY, PAID, FAILED, CANCELLED, PARTIAL_CANCELLED
        PaymentAmount amount                  // 결제 금액 세부 정보
) {

    /**
     * 결제 금액 세부 정보
     *
     * 주요 생략 필드:
     * - taxFree: 면세액
     * - vat: 부가세액
     * - supply: 공급가액
     * - discount: 총 할인금액
     * - paid: 실제 결제금액
     * - cancelled: 총 취소금액
     * - cancelledTaxFree: 총 취소금액 중 면세액
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentAmount(
            int total     // 총 결제 금액
    ) {}
}
