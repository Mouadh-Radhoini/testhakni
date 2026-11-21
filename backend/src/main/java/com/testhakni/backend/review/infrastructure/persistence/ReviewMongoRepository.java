package com.testhakni.backend.review.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.review.domain.model.RatingSummary;
import com.testhakni.backend.review.domain.model.Review;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface ReviewMongoRepository extends MongoRepository<Review, String> {

    @Aggregation(pipeline = {
            "{'$match': {'providerId': ?0, 'hidden': false}}",
            "{'$group': {'_id': null, 'average': {'$avg': '$rating'}, 'count': {'$sum': 1}}}"
    })
    Optional<RatingSummary> rating(String providerId);

    List<Review> findByProviderIdAndHiddenFalseOrderByCreatedAtDesc(String providerId);

    List<Review> findByServiceIdAndHiddenFalseOrderByCreatedAtDesc(String serviceId);
}
