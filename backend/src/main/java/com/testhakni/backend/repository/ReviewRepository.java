package com.testhakni.backend.repository;

import com.testhakni.backend.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    @org.springframework.data.mongodb.repository.Aggregation(pipeline = {
            "{'$match': {'providerId': ?0, 'hidden': false}}",
            "{'$group': {'_id': null, 'average': {'$avg': '$rating'}, 'count': {'$sum': 1}}}"
    })
    java.util.Optional<com.testhakni.backend.dto.RatingSummary> rating(String providerId);

    List<Review> findByProviderIdAndHiddenFalseOrderByCreatedAtDesc(String providerId);
    List<Review> findByServiceIdAndHiddenFalseOrderByCreatedAtDesc(String serviceId);
}
