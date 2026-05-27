package com.sparta.paymentsystem.domain.order.entity;

/**
 * 주문 상태 머신
 * - PENDING_PAYMENT → CONFIRMED: 결제 성공 → 주문 확정
 * - PENDING_PAYMENT → CANCELLED: 결제 실패 또는 취소
 * - CONFIRMED → CANCELLED: 결제 취소·환불
 */
public enum OrderStatus {
    PENDING_PAYMENT {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return target == CONFIRMED || target == CANCELLED;
        }
    },
    CONFIRMED {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return target == CANCELLED;
        }
    },
    CANCELLED {
        @Override
        public boolean canTransitTo(OrderStatus target) {
            return false;
        }
    };

    public abstract boolean canTransitTo(OrderStatus target);
}

