package com.testhakni.backend.review.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.review.domain.model.RatingSummary;
import com.testhakni.backend.review.domain.model.Review;
import com.testhakni.backend.review.domain.repository.ReviewRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link ReviewRepository}. */
@Repository
class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewMongoRepository repository;
    private final MongoTemplate mongo;

    ReviewRepositoryAdapter(ReviewMongoRepository repository, MongoTemplate mongo) {
        this.repository = repository;
        this.mongo = mongo;
    }

    @Override
    public Review insert(Review review) {
        return repository.insert(review);
    }

    @Override
    public Optional<Review> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Review> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<RatingSummary> rating(String providerId) {
        return repository.rating(providerId);
    }

    @Override
    public List<Review> findVisibleByProviderId(String providerId) {
        return repository.findByProviderIdAndHiddenFalseOrderByCreatedAtDesc(providerId);
    }

    @Override
    public List<Review> findVisibleByServiceId(String serviceId) {
        return repository.findByServiceIdAndHiddenFalseOrderByCreatedAtDesc(serviceId);
    }

    @Override
    public void setHidden(String id, boolean hidden) {
        mongo.updateFirst(Query.query(Criteria.where("_id").is(id)), Update.update("hidden", hidden), Review.class);
    }

    @Override
    public long count() {
        return repository.count();
    }
}
