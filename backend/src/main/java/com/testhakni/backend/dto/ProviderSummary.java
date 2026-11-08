package com.testhakni.backend.dto;

/** Resume du prestataire affiche sur une carte de service. */
public record ProviderSummary(
        String providerId,
        String businessName,
        String city,
        Integer yearsOfExperience,
        boolean available) {
}
