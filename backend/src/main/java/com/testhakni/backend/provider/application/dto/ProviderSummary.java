package com.testhakni.backend.provider.application.dto;

import com.testhakni.backend.provider.domain.model.ProviderProfile;

/** Resume du prestataire affiche sur une carte de service. */
public record ProviderSummary(
        String providerId,
        String businessName,
        String city,
        Integer yearsOfExperience,
        boolean available) {

    public static ProviderSummary from(ProviderProfile p) {
        return new ProviderSummary(p.getUserId(), p.getBusinessName(), p.getCity(),
                p.getYearsOfExperience(), p.isAvailable());
    }
}
