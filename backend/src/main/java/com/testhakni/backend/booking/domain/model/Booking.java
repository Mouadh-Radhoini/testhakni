package com.testhakni.backend.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("bookings")
public record Booking(
        @Id String id,
        @Indexed(unique = true) String requestId,
        @Indexed(unique = true) String offerId,
        @Indexed String customerId,
        @Indexed String providerId,
        String serviceId,
        String title,
        String address,
        BigDecimal price,
        LocalDate proposedDate,
        BookingStatus status,
        boolean reviewed,
        List<Transition> history) {
    public record Transition(BookingStatus status, String actorId, Instant at) {}
}
