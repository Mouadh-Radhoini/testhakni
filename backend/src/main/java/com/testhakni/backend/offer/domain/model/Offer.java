package com.testhakni.backend.offer.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("offers")
@CompoundIndex(name = "request_provider_unique", def = "{'requestId':1,'providerId':1}", unique = true)
public record Offer(
        @Id String id,
        @Indexed String requestId,
        @Indexed String providerId,
        String businessName,
        String requestTitle,
        BigDecimal price,
        LocalDate proposedDate,
        String description,
        OfferStatus status,
        Instant createdAt) {
}
