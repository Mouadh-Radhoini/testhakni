package com.testhakni.backend.booking.domain.model;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * Cycle de vie d'une reservation : BOOKED -> IN_PROGRESS -> (AWAITING_CONFIRMATION) -> COMPLETED,
 * ou CANCELLED tant que la prestation n'est pas terminee. Le client peut faire avancer la
 * reservation lui-meme ; le prestataire peut la demarrer et declarer le travail termine.
 */
public enum BookingStatus {
    BOOKED, IN_PROGRESS, AWAITING_CONFIRMATION, COMPLETED, CANCELLED;

    /** Statuts depuis lesquels le client peut declarer la prestation en cours. */
    public static final Set<BookingStatus> CUSTOMER_CAN_START_FROM = EnumSet.of(BOOKED);
    /** Statuts depuis lesquels le client peut la declarer terminee. */
    public static final Set<BookingStatus> CUSTOMER_CAN_COMPLETE_FROM = EnumSet.of(BOOKED, IN_PROGRESS, AWAITING_CONFIRMATION);
    /** Statuts depuis lesquels le client peut l'annuler (jamais une fois terminee). */
    public static final Set<BookingStatus> CUSTOMER_CAN_CANCEL_FROM = EnumSet.of(BOOKED, IN_PROGRESS, AWAITING_CONFIRMATION);

    /** Statuts de depart autorises pour que le client atteigne {@code target} (vide si impossible). */
    public static Set<BookingStatus> customerSourcesFor(BookingStatus target) {
        return switch (target) {
            case IN_PROGRESS -> CUSTOMER_CAN_START_FROM;
            case COMPLETED -> CUSTOMER_CAN_COMPLETE_FROM;
            case CANCELLED -> CUSTOMER_CAN_CANCEL_FROM;
            default -> Set.of();
        };
    }

    /** Regle du domaine : statut requis avant d'atteindre celui-ci (vide pour l'etat initial). */
    public Optional<BookingStatus> expectedPrevious() {
        return switch (this) {
            case IN_PROGRESS -> Optional.of(BOOKED);
            case AWAITING_CONFIRMATION -> Optional.of(IN_PROGRESS);
            case COMPLETED -> Optional.of(AWAITING_CONFIRMATION);
            case BOOKED, CANCELLED -> Optional.empty();
        };
    }
}
