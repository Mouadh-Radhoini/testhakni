package com.testhakni.backend.review.application.dto;

import java.time.Instant;
import java.util.List;
import com.testhakni.backend.review.domain.model.Review;

public record PublicReviews(long count, Double average, List<Entry> reviews) {
    public record Entry(String id, int rating, String comment, Instant createdAt) {}

    public static PublicReviews from(List<Review> reviews) {
        var average = reviews.stream().mapToInt(Review::rating).average();
        return new PublicReviews(reviews.size(), average.isPresent() ? average.getAsDouble() : null,
                reviews.stream().map(r -> new Entry(r.id(), r.rating(), r.comment(), r.createdAt())).toList());
    }
}
