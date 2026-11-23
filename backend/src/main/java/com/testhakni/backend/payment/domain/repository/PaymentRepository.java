package com.testhakni.backend.payment.domain.repository;

import java.time.Instant;
import java.util.Optional;

import com.testhakni.backend.payment.domain.model.Payment;
import com.testhakni.backend.payment.domain.model.PaymentStatus;

public interface PaymentRepository {
    Optional<Payment> findByBookingId(String bookingId);

    Optional<Payment> findByCheckoutSessionId(String checkoutSessionId);

    Optional<Payment> findById(String paymentId);

    Payment save(Payment payment);

    boolean markCheckoutSessionIfPending(String paymentId, String checkoutSessionId,
                                         String paymentIntentId);

    boolean markPaidByCheckoutSession(String checkoutSessionId, String paymentIntentId,
                                      Instant paidAt);

    boolean markFailedByCheckoutSession(String checkoutSessionId, Instant updatedAt);

    boolean markCancelledByCheckoutSession(String checkoutSessionId, Instant updatedAt);

    boolean markExpiredByCheckoutSession(String checkoutSessionId, Instant updatedAt);

    boolean markStatusIfAllowed(String paymentId, PaymentStatus target, Instant updatedAt,
                                Instant paidAt);
}
