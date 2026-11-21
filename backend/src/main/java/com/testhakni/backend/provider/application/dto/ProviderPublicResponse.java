package com.testhakni.backend.provider.application.dto;

import com.testhakni.backend.catalog.application.dto.ServiceResponse;

import java.time.Instant;
import java.util.List;

import com.testhakni.backend.catalog.domain.model.ServiceCategory;

/**
 * Profil public d'un prestataire + ses services actifs. PROV-03 / SEARCH-04
 * Exclut volontairement email, telephone et statut du compte.
 */
public record ProviderPublicResponse(
        String providerId,
        String displayName,
        String businessName,
        String bio,
        List<ServiceCategory> categories,
        List<String> skills,
        String city,
        String serviceArea,
        Integer yearsOfExperience,
        boolean available,
        Instant memberSince,
        List<ServiceResponse> services, Double averageRating, long reviewCount) {
}
