package com.testhakni.backend.offer.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.offer.domain.model.Offer;
import com.testhakni.backend.offer.domain.model.OfferStatus;
import com.testhakni.backend.offer.domain.repository.OfferRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link OfferRepository}. */
@Repository
class OfferRepositoryAdapter implements OfferRepository {

    private final OfferMongoRepository repository;
    private final MongoTemplate mongo;

    OfferRepositoryAdapter(OfferMongoRepository repository, MongoTemplate mongo) {
        this.repository = repository;
        this.mongo = mongo;
    }

    @Override
    public Offer insert(Offer offer) {
        return repository.insert(offer);
    }

    @Override
    public Optional<Offer> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Offer> findByRequestId(String requestId) {
        return repository.findByRequestIdOrderByPriceAsc(requestId);
    }

    @Override
    public List<Offer> findByProviderId(String providerId) {
        return repository.findByProviderIdOrderByCreatedAtDesc(providerId);
    }

    @Override
    public boolean changeStatusIfSubmitted(String id, OfferStatus target) {
        var result = mongo.updateFirst(Query.query(Criteria.where("_id").is(id)
                .and("status").is(OfferStatus.SUBMITTED)), Update.update("status", target), Offer.class);
        return result.getModifiedCount() == 1;
    }

    @Override
    public long count() {
        return repository.count();
    }
}
