package com.testhakni.backend.booking.application.dto;

import com.testhakni.backend.booking.domain.model.BookingStatus;
import jakarta.validation.constraints.NotNull;

/** Avancement d'une prestation demande par le prestataire. */
public record Progress(@NotNull BookingStatus status) {
}
