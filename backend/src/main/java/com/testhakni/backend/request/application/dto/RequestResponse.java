package com.testhakni.backend.request.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.testhakni.backend.request.domain.model.RequestStatus;
import com.testhakni.backend.catalog.domain.model.ServiceCategory;
import com.testhakni.backend.request.domain.model.ServiceRequest;

public record RequestResponse(
        String id,
        String customerId,
        String serviceId,
        String title,
        String description,
        ServiceCategory category,
        String categoryLabel,
        String city,
        String address,
        LocalDate preferredDate,
        BigDecimal budget,
        RequestStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static RequestResponse from(ServiceRequest r) {
        return new RequestResponse(r.getId(), r.getCustomerId(), r.getServiceId(), r.getTitle(),
                r.getDescription(), r.getCategory(), r.getCategory() == null ? null : r.getCategory().getLabel(),
                r.getCity(), r.getAddress(), r.getPreferredDate(), r.getBudget(), r.getStatus(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
