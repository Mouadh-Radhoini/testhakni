package com.testhakni.backend.offer.domain.repository;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.offer.domain.model.Offer;
import com.testhakni.backend.offer.domain.model.OfferStatus;

/** Port du domaine : offres des prestataires sur les demandes. */
public interface OfferRepository {

    Offer insert(Offer offer);

    Optional<Offer> findById(String id);

    /** Offres d'une demande, de la moins chere a la plus chere. */
    List<Offer> findByRequestId(String requestId);

    /** Offres d'un prestataire, plus recentes d'abord. */
    List<Offer> findByProviderId(String providerId);

    /** Transition atomique depuis SUBMITTED : false si l'offre a deja ete traitee. */
    boolean changeStatusIfSubmitted(String id, OfferStatus target);

    long count();
}
