package com.testhakni.backend.review.presentation;

import com.testhakni.backend.review.application.ReviewService;
import com.testhakni.backend.review.application.dto.PublicReviews;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Parcours de réservation")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviews;

    @Operation(summary = "Public · avis visibles du prestataire")
    @SecurityRequirements
    @GetMapping("/providers/{id}/reviews")
    public PublicReviews providerReviews(@PathVariable String id) {
        return reviews.forProvider(id);
    }

    @Operation(summary = "Public · avis visibles du service")
    @SecurityRequirements
    @GetMapping("/services/{id}/reviews")
    public PublicReviews serviceReviews(@PathVariable String id) {
        return reviews.forService(id);
    }
}
