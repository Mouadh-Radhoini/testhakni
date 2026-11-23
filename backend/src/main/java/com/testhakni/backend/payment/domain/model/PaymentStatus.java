package com.testhakni.backend.payment.domain.model;

import java.util.EnumSet;
import java.util.Set;

public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    CANCELLED,
    EXPIRED;

    public boolean canTransitionTo(PaymentStatus target) {
        if (target == null) {
            return false;
        }
        if (this == PAID) {
            return false;
        }
        if (this == FAILED || this == CANCELLED || this == EXPIRED) {
            return false;
        }
        return PENDING_TARGETS.contains(target);
    }

    private static final Set<PaymentStatus> PENDING_TARGETS =
            EnumSet.of(PAID, FAILED, CANCELLED, EXPIRED);
}
