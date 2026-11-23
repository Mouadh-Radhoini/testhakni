package com.testhakni.backend.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("payments")
@CompoundIndexes({
        @CompoundIndex(
                name = "unique_pending_payment_per_booking",
                def = "{'bookingId': 1}",
                unique = true,
                partialFilter = "{'status': 'PENDING'}"),
        @CompoundIndex(
                name = "unique_paid_payment_per_booking",
                def = "{'bookingId': 1}",
                unique = true,
                partialFilter = "{'status': 'PAID'}")
})
public record Payment(
        @Id String id,
        @Indexed String bookingId,
        @Indexed String customerId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        @Indexed(unique = true, sparse = true) String stripeCheckoutSessionId,
        String stripePaymentIntentId,
        Instant createdAt,
        Instant updatedAt,
        Instant paidAt) {
}
