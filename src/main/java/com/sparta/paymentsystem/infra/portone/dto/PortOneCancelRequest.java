package com.sparta.paymentsystem.infra.portone.dto;

/**
 * PortOne V2 결제 취소 요청 (POST /payments/{paymentId}/cancel)
 *
 * 이 프로젝트에서 필요한 핵심 필드만 선언했습니다.
 *
 * 전체 요청 필드는 PortOne V2 API 문서를 참고하세요.
 * https://developers.portone.io/api/rest-v2/payment
 *
 * 주요 생략 필드:
 * - amount: 취소 총 금액 (값을 입력하지 않으면 전액 취소됩니다.)
 * - taxFreeAmount: 취소 금액 중 면세 금액 (값을 입력하지 않으면 전액 과세 취소됩니다.)
 * - vatAmount: 취소 금액 중 부가세액 (값을 입력하지 않으면 자동 계산됩니다.)
 * - requester: 결제 취소 요청 주체, CancelRequester(CUSTOMER / ADMIN)
 * - currentCancellableAmount: 결제 건의 취소 가능 잔액 (본 취소 요청 이전의 취소 가능 잔액으로써, 값을 입력하면 잔액이 일치하는 경우에만 취소가 진행됩니다. 값을 입력하지 않으면 별도의 검증 처리를 수행하지 않습니다.)
 * - refundAccount: 고객 정보 입력 형식 CancelPaymentBodyRefundAccount(bank, number, holderName)
 */
public record PortOneCancelRequest(
        String reason,   // [필수] 취소 사유
        String storeId   // [조건부] 하위 상점 사용 시 필수
) {}
