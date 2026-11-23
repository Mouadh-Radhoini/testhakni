package com.testhakni.backend.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.model.BookingStatus;
import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.payment.application.PaymentService;
import com.testhakni.backend.payment.application.port.CheckoutGateway;
import com.testhakni.backend.payment.domain.model.Payment;
import com.testhakni.backend.payment.domain.model.PaymentStatus;
import com.testhakni.backend.payment.domain.repository.PaymentRepository;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {
    private final BookingRepository bookings = mock(BookingRepository.class);
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final CheckoutGateway gateway = mock(CheckoutGateway.class);
    private final PaymentService service =
            new PaymentService(bookings, payments, gateway, "tnd");

    @Test
    void rejectsCheckoutWithoutAuthenticatedCustomer() {
        assertThrows(DomainException.class, () -> service.checkout(null, "booking-1"));
    }

    @Test
    void convertsTndToThreeDecimalMinorUnits() {
        assertEquals(1250L, PaymentService.toMinorUnits(new BigDecimal("1.250"), "tnd"));
        assertEquals(125L, PaymentService.toMinorUnits(new BigDecimal("1.25"), "usd"));
    }

    @Test
    void createsCheckoutFromServerSideBookingAmount() {
        Booking booking = booking(new BigDecimal("125.500"), "customer-1");
        AuthenticatedUser user = new AuthenticatedUser("customer-1", "c@test.dev", Role.CUSTOMER);
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking));
        when(payments.findByBookingId("booking-1")).thenReturn(Optional.empty());
        when(payments.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(payments.markCheckoutSessionIfPending(any(), any(), any())).thenReturn(true);
        when(gateway.createCheckout(any(), any()))
                .thenReturn(new CheckoutGateway.CheckoutSession("cs_test", "https://checkout.test", "pi_test"));

        PaymentService.CheckoutResponse response = service.checkout(user, "booking-1");

        assertEquals("cs_test", response.checkoutSessionId());
        assertEquals(PaymentStatus.PENDING, response.status());
        verify(gateway).createCheckout(any(), any());
    }

    @Test
    void rejectsAnotherCustomersBooking() {
        Booking booking = booking(new BigDecimal("10.000"), "customer-2");
        AuthenticatedUser user = new AuthenticatedUser("customer-1", "c@test.dev", Role.CUSTOMER);
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking));
        assertThrows(DomainException.class, () -> service.checkout(user, "booking-1"));
    }

    @Test
    void rejectsZeroAmount() {
        Booking booking = booking(BigDecimal.ZERO, "customer-1");
        AuthenticatedUser user = new AuthenticatedUser("customer-1", "c@test.dev", Role.CUSTOMER);
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking));
        assertThrows(DomainException.class, () -> service.checkout(user, "booking-1"));
    }

    @Test
    void rejectsAlreadyPaidPayment() {
        Booking booking = booking(new BigDecimal("10.000"), "customer-1");
        AuthenticatedUser user = new AuthenticatedUser("customer-1", "c@test.dev", Role.CUSTOMER);
        Payment payment = new Payment("p1", "booking-1", "customer-1", new BigDecimal("10.000"),
                "tnd", PaymentStatus.PAID, "cs1", "pi1", Instant.now(), Instant.now(), Instant.now());
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking));
        when(payments.findByBookingId("booking-1")).thenReturn(Optional.of(payment));
        assertThrows(DomainException.class, () -> service.checkout(user, "booking-1"));
    }

    @Test
    void rejectsDuplicatePendingCheckout() {
        Booking booking = booking(new BigDecimal("10.000"), "customer-1");
        AuthenticatedUser user = new AuthenticatedUser("customer-1", "c@test.dev", Role.CUSTOMER);
        Payment payment = new Payment("p1", "booking-1", "customer-1", new BigDecimal("10.000"),
                "tnd", PaymentStatus.PENDING, "cs1", null, Instant.now(), Instant.now(), null);
        when(bookings.findById("booking-1")).thenReturn(Optional.of(booking));
        when(payments.findByBookingId("booking-1")).thenReturn(Optional.of(payment));
        assertThrows(DomainException.class, () -> service.checkout(user, "booking-1"));
    }

    private Booking booking(BigDecimal price, String customerId) {
        return new Booking("booking-1", "request-1", "offer-1", customerId, "provider-1",
                null, "Service", "Tunis", price, null, BookingStatus.BOOKED, false, java.util.List.of());
    }
}
