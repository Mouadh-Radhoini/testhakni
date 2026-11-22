package com.testhakni.backend.provider.application.dto;

import com.testhakni.backend.shared.application.MediaUrls;

import java.time.Instant;
import java.util.List;

import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.catalog.domain.model.ServiceCategory;

/** Profil vu par son proprietaire. */
public record ProviderProfileResponse(
        String id,
        String userId,
        String businessName,
        String bio,
        List<ServiceCategory> categories,
        List<String> skills,
        String city,
        String serviceArea,
        Integer yearsOfExperience,
        boolean available,
        Instant createdAt,
        Instant updatedAt,
        List<String> photoUrls) {

    public static ProviderProfileResponse from(ProviderProfile p) {
        return new ProviderProfileResponse(p.getId(), p.getUserId(), p.getBusinessName(), p.getBio(),
                p.getCategories(), p.getSkills(), p.getCity(), p.getServiceArea(), p.getYearsOfExperience(),
                p.isAvailable(), p.getCreatedAt(), p.getUpdatedAt(), MediaUrls.of(p.getPhotoIds()));
    }
}
