package com.testhakni.backend.review.application;

import com.testhakni.backend.catalog.application.ServiceOfferingService;
import com.testhakni.backend.provider.application.ProviderProfileService;
import com.testhakni.backend.review.application.dto.PublicReviews;
import com.testhakni.backend.review.domain.repository.ReviewRepository;
import org.springframework.stereotype.Service;

/** Cas d'usage : consultation publique des avis visibles. */
@Service
public class ReviewService {

    private final ReviewRepository reviews;
    private final ProviderProfileService profiles;
    private final ServiceOfferingService catalogue;

    public ReviewService(ReviewRepository reviews, ProviderProfileService profiles, ServiceOfferingService catalogue) {
        this.reviews = reviews;
        this.profiles = profiles;
        this.catalogue = catalogue;
    }

    /** 404 si le prestataire n'est pas public (inexistant ou suspendu). */
    public PublicReviews forProvider(String providerId) {
        profiles.getPublic(providerId);
        return PublicReviews.from(reviews.findVisibleByProviderId(providerId));
    }

    /** 404 si le service n'est pas visible publiquement. */
    public PublicReviews forService(String serviceId) {
        catalogue.getById(serviceId, null);
        return PublicReviews.from(reviews.findVisibleByServiceId(serviceId));
    }
}
