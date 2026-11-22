package com.testhakni.backend.offer.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.testhakni.backend.catalog.domain.model.ServiceCategory;
import com.testhakni.backend.request.domain.model.ServiceRequest;

/** Demande ouverte vue par un prestataire (sans donnees personnelles du client). */
public record OpenRequest(String id, String title, String description, ServiceCategory category,
                          String city, BigDecimal budget, LocalDate preferredDate) {

    public static OpenRequest from(ServiceRequest r) {
        return new OpenRequest(r.getId(), r.getTitle(), r.getDescription(), r.getCategory(),
                r.getCity(), r.getBudget(), r.getPreferredDate());
    }
}
