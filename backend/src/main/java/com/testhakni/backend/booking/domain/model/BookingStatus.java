package com.testhakni.backend.booking.domain.model;

import java.util.Optional;

/** Cycle de vie d'une reservation : BOOKED -> IN_PROGRESS -> AWAITING_CONFIRMATION -> COMPLETED. */
public enum BookingStatus {
    BOOKED, IN_PROGRESS, AWAITING_CONFIRMATION, COMPLETED;

    /** Regle du domaine : statut requis avant d'atteindre celui-ci (vide pour l'etat initial). */
    public Optional<BookingStatus> expectedPrevious() {
        return switch (this) {
            case IN_PROGRESS -> Optional.of(BOOKED);
            case AWAITING_CONFIRMATION -> Optional.of(IN_PROGRESS);
            case COMPLETED -> Optional.of(AWAITING_CONFIRMATION);
            case BOOKED -> Optional.empty();
        };
    }
}
