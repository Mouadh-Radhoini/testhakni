package com.testhakni.backend.offer.infrastructure.persistence;

import java.util.List;

import com.testhakni.backend.offer.domain.model.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface OfferMongoRepository extends MongoRepository<Offer, String> {

    List<Offer> findByRequestIdOrderByPriceAsc(String requestId);

    List<Offer> findByProviderIdOrderByCreatedAtDesc(String providerId);
}
