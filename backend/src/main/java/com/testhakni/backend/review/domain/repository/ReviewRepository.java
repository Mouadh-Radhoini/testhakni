package com.testhakni.backend.review.domain.repository;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.review.domain.model.RatingSummary;
import com.testhakni.backend.review.domain.model.Review;

/** Port du domaine : avis clients. */
public interface ReviewRepository {

    Review insert(Review review);

    Optional<Review> findById(String id);

    List<Review> findAll();

    /** Moyenne et nombre des avis visibles d'un prestataire (vide s'il n'en a aucun). */
    Optional<RatingSummary> rating(String providerId);

    /** Avis visibles, plus recents d'abord. */
    List<Review> findVisibleByProviderId(String providerId);

    /** Avis visibles, plus recents d'abord. */
    List<Review> findVisibleByServiceId(String serviceId);

    void setHidden(String id, boolean hidden);

    long count();
}
