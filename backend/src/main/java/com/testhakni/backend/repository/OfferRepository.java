package com.testhakni.backend.repository;

import com.testhakni.backend.model.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OfferRepository extends MongoRepository<Offer, String> {
    List<Offer> findByRequestIdOrderByPriceAsc(String requestId);
    List<Offer> findByProviderIdOrderByCreatedAtDesc(String providerId);
}
