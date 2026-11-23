package com.testhakni.backend.payment.application;

import java.time.Instant;

import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.payment.domain.model.Payment;
import com.testhakni.backend.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentWebhookService {
    private final PaymentRepository payments;
    private final BookingRepository bookings;

    public PaymentWebhookService(PaymentRepository payments, BookingRepository bookings) {
        this.payments = payments;
        this.bookings = bookings;
    }

    public boolean completed(CheckoutSessionEvent event) {
        return processEvent(event, PaymentEventType.COMPLETED);
    }

    public boolean asyncSuccess(CheckoutSessionEvent event) {
        return processEvent(event, PaymentEventType.ASYNC_SUCCESS);
    }

    public boolean asyncFailure(CheckoutSessionEvent event) {
        return processEvent(event, PaymentEventType.ASYNC_FAILURE);
    }

    public boolean expired(CheckoutSessionEvent event) {
        if (event == null || event.sessionId() == null) {
            return false;
        }
        return payments.markExpiredByCheckoutSession(event.sessionId(), Instant.now());
    }

    private boolean processEvent(CheckoutSessionEvent event, PaymentEventType type) {
        if (event == null || event.sessionId() == null || event.sessionId().isBlank()) {
            return false;
        }
        if ((type == PaymentEventType.COMPLETED || type == PaymentEventType.ASYNC_SUCCESS)
                && !"paid".equalsIgnoreCase(event.paymentStatus())) {
            return false;
        }

        Payment payment = findPayment(event);
        if (payment == null) {
            return false;
        }
        if (!event.sessionId().equals(payment.stripeCheckoutSessionId())
                && payment.stripeCheckoutSessionId() != null) {
            return false;
        }
        if (event.bookingId() == null
                || !event.bookingId().equals(payment.bookingId())
                || event.paymentId() == null
                || !event.paymentId().equals(payment.id())) {
            return false;
        }
        if (event.amountMinor() == null
                || event.amountMinor() != PaymentService.toMinorUnits(payment.amount(), payment.currency())
                || event.currency() == null
                || !event.currency().equalsIgnoreCase(payment.currency())) {
            return false;
        }
        if (bookings.findById(payment.bookingId()).isEmpty()) {
            return false;
        }
        if (type == PaymentEventType.ASYNC_FAILURE) {
            return payments.markFailedByCheckoutSession(event.sessionId(), Instant.now());
        }
        if (payments.markPaidByCheckoutSession(event.sessionId(),
                event.paymentIntentId(), Instant.now())) {
            return true;
        }
        return payments.findByCheckoutSessionId(event.sessionId())
                .map(Payment::status)
                .map(status -> status.name().equals("PAID"))
                .orElse(false);
    }

    private Payment findPayment(CheckoutSessionEvent event) {
        if (event.paymentId() != null) {
            Payment byId = payments.findById(event.paymentId()).orElse(null);
            if (byId != null) {
                return byId;
            }
        }
        return payments.findByCheckoutSessionId(event.sessionId()).orElse(null);
    }

    public enum PaymentEventType {
        COMPLETED,
        ASYNC_SUCCESS,
        ASYNC_FAILURE
    }

    public record CheckoutSessionEvent(
            String sessionId,
            String paymentIntentId,
            String paymentStatus,
            Long amountMinor,
            String currency,
            String bookingId,
            String paymentId) {}
}
