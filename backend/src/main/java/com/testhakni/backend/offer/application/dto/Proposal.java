package com.testhakni.backend.offer.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Offre proposee par un prestataire sur une demande ouverte. */
public record Proposal(@NotNull @Positive @Digits(integer = 9, fraction = 2) BigDecimal price,
                       @NotNull @FutureOrPresent LocalDate proposedDate,
                       @NotBlank @Size(max = 2000) String description) {
}
