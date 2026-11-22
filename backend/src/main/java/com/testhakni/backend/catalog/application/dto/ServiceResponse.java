package com.testhakni.backend.catalog.application.dto;

import com.testhakni.backend.provider.application.dto.ProviderSummary;

import java.math.BigDecimal;
import java.time.Instant;

import com.testhakni.backend.catalog.domain.model.PriceType;
import com.testhakni.backend.catalog.domain.model.ServiceCategory;
import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.model.ServiceStatus;

public record ServiceResponse(
        String id,
        String providerId,
        String title,
        String description,
        ServiceCategory category,
        String categoryLabel,
        String city,
        BigDecimal price,
        PriceType priceType,
        ServiceStatus status,
        Instant createdAt,
        Instant updatedAt,
        ProviderSummary provider,
        /** true pour un visiteur non connecte : prix, description et prestataire masques. */
        boolean locked) {

    public static ServiceResponse from(ServiceOffering s, ProviderSummary provider) {
        return new ServiceResponse(s.getId(), s.getProviderId(), s.getTitle(), s.getDescription(),
                s.getCategory(), s.getCategory() == null ? null : s.getCategory().getLabel(),
                s.getCity(), s.getPrice(), s.getPriceType(), s.getStatus(),
                s.getCreatedAt(), s.getUpdatedAt(), provider, false);
    }

    /** Apercu pour visiteur anonyme : titre, categorie et ville seulement (prix reserve aux inscrits). */
    public static ServiceResponse teaser(ServiceOffering s) {
        return new ServiceResponse(s.getId(), s.getProviderId(), s.getTitle(), null,
                s.getCategory(), s.getCategory() == null ? null : s.getCategory().getLabel(),
                s.getCity(), null, null, s.getStatus(), s.getCreatedAt(), s.getUpdatedAt(), null, true);
    }
}
