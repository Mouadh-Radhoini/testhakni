package com.testhakni.backend.payment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.model.BookingStatus;
import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.payment.application.PaymentWebhookService;
import com.testhakni.backend.payment.domain.model.Payment;
import com.testhakni.backend.payment.domain.model.PaymentStatus;
import com.testhakni.backend.payment.domain.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

class PaymentWebhookServiceTest {
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final PaymentWebhookService service =
            new PaymentWebhookService(payments, bookings);

    @Test
    void acceptsSuccessfulVerifiedSessionData() {
        Payment payment = pendingPayment(PaymentStatus.PENDING);
        Booking booking = booking();
        when(payments.findById("payment-1")).thenReturn(Optional.of(payment));
        when(payments.findByCheckoutSessionId("cs_test")).thenReturn(Optional.of(payment));
        when(payments.markPaidByCheckoutSession(eq("cs_test"), eq("pi_test"),
                any(Instant.class))).thenReturn(true);
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking));

        assertTrue(service.completed(event("paid")));
    }

    @Test
    void rejectsWrongAmount() {
        Payment payment = pendingPayment(PaymentStatus.PENDING);
        when(payments.findById("payment-1")).thenReturn(Optional.of(payment));
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking()));

        PaymentWebhookService.CheckoutSessionEvent wrongAmount =
                new PaymentWebhookService.CheckoutSessionEvent(
                        "cs_test", "pi_test", "paid", 999L, "tnd", "booking-1", "payment-1");
        assertFalse(service.completed(wrongAmount));
    }

    @Test
    void failedEventCannotRevertPaidStatus() {
        Payment paid = pendingPayment(PaymentStatus.PAID);
        when(payments.findById("payment-1")).thenReturn(Optional.of(paid));
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking()));

        assertFalse(service.asyncFailure(event("failed")));
    }

    @Test
    void duplicateSuccessfulEventIsIdempotent() {
        Payment paid = pendingPayment(PaymentStatus.PAID);
        when(payments.findById("payment-1")).thenReturn(Optional.of(paid));
        when(payments.findByCheckoutSessionId("cs_test")).thenReturn(Optional.of(paid));
        when(payments.markPaidByCheckoutSession(eq("cs_test"), eq("pi_test"),
                any(Instant.class))).thenReturn(false);
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking()));

        assertTrue(service.completed(event("paid")));
    }

    @Test
    void expiredSessionMarksPaymentExpired() {
        when(payments.markExpiredByCheckoutSession(eq("cs_test"), any(Instant.class))).thenReturn(true);
        assertTrue(service.expired(event("expired")));
    }

    @Test
    void asyncFailureMarksPendingSessionFailed() {
        Payment pending = pendingPayment(PaymentStatus.PENDING);
        when(payments.findById("payment-1")).thenReturn(Optional.of(pending));
        when(payments.markFailedByCheckoutSession(eq("cs_test"), any(Instant.class))).thenReturn(true);
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking()));

        assertTrue(service.asyncFailure(event("failed")));
    }

    @Test
    void paidEventCannotBeRevertedByLaterFailure() {
        Payment paid = pendingPayment(PaymentStatus.PAID);
        when(payments.findById("payment-1")).thenReturn(Optional.of(paid));
        when(payments.markFailedByCheckoutSession(eq("cs_test"), any(Instant.class))).thenReturn(false);
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking()));

        assertFalse(service.asyncFailure(event("failed")));
    }

    private PaymentWebhookService.CheckoutSessionEvent event(String paymentStatus) {
        return new PaymentWebhookService.CheckoutSessionEvent(
                "cs_test", "pi_test", paymentStatus, 12500L, "tnd", "booking-1", "payment-1");
    }

    private Payment pendingPayment(PaymentStatus status) {
        return new Payment("payment-1", "booking-1", "customer-1",
                new BigDecimal("12.500"), "tnd", status, "cs_test", "pi_test",
                Instant.now(), Instant.now(), status == PaymentStatus.PAID ? Instant.now() : null);
    }

    private Booking booking() {
        return new Booking("booking-1", "request-1", "offer-1", "customer-1", "provider-1",
                null, "Service", "Tunis", new BigDecimal("12.500"), null,
                BookingStatus.BOOKED, false, java.util.List.of());
    }
}
