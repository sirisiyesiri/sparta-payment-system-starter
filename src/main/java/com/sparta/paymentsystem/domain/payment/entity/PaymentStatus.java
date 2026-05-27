package com.sparta.paymentsystem.domain.payment.entity;

/**
 * 결제 상태 머신
 * - IN_PROGRESS → PAID     : 결제 성공
 * - IN_PROGRESS → FAILED   : 결제 미완료 (PG 실패, 금액 불일치 등)
 * - PAID        → CANCELLED: 성공한 결제의 사후 취소 (환불)
 *
 * - FAILED  = 결제가 성공적으로 완료되지 못한 모든 경우
 * - CANCELLED = 성공한 결제를 사후에 취소한 경우
 */
public enum PaymentStatus {
    IN_PROGRESS {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == PAID || target == FAILED;
        }
    },
    PAID {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return target == CANCELLED;
        }
    },
    FAILED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    },
    CANCELLED {
        @Override
        public boolean canTransitTo(PaymentStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(PaymentStatus target);
}

