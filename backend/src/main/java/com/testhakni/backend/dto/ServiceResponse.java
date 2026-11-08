package com.testhakni.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.testhakni.backend.model.PriceType;
import com.testhakni.backend.model.ServiceCategory;
import com.testhakni.backend.model.ServiceOffering;
import com.testhakni.backend.model.ServiceStatus;

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
        ProviderSummary provider) {

    public static ServiceResponse from(ServiceOffering s, ProviderSummary provider) {
        return new ServiceResponse(s.getId(), s.getProviderId(), s.getTitle(), s.getDescription(),
                s.getCategory(), s.getCategory() == null ? null : s.getCategory().getLabel(),
                s.getCity(), s.getPrice(), s.getPriceType(), s.getStatus(),
                s.getCreatedAt(), s.getUpdatedAt(), provider);
    }
}
