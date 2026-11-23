package com.testhakni.backend.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;

import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.payment.application.PaymentService;
import com.testhakni.backend.payment.domain.model.PaymentStatus;
import com.testhakni.backend.payment.presentation.PaymentController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PaymentControllerTest {
    private final PaymentService payments = mock(PaymentService.class);
    private final PaymentController controller = new PaymentController(payments);

    @Test
    void checkoutReturns201() {
        AuthenticatedUser user = new AuthenticatedUser("customer-1", "c@test.dev", Role.CUSTOMER);
        PaymentService.CheckoutResponse response =
                new PaymentService.CheckoutResponse("payment-1", "booking-1",
                        "https://checkout.test", "cs_test", PaymentStatus.PENDING);
        when(payments.checkout(user, "booking-1")).thenReturn(response);

        ResponseEntity<PaymentService.CheckoutResponse> result =
                controller.checkout(user, "booking-1");

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    void statusReturnsPaymentMetadata() {
        AuthenticatedUser user = new AuthenticatedUser("customer-1", "c@test.dev", Role.CUSTOMER);
        PaymentService.PaymentStatusResponse response =
                new PaymentService.PaymentStatusResponse("payment-1", "booking-1",
                        new BigDecimal("12.500"), "tnd", PaymentStatus.PAID, Instant.now());
        when(payments.status(user, "booking-1")).thenReturn(response);

        assertEquals(PaymentStatus.PAID, controller.status(user, "booking-1").status());
    }

    @Test
    void gatewayUnavailableReturns503() {
        AuthenticatedUser user = new AuthenticatedUser("customer-1", "c@test.dev", Role.CUSTOMER);
        when(payments.checkout(user, "booking-1"))
                .thenThrow(new PaymentService.PaymentUnavailableException("Stripe unavailable"));

        ResponseEntity<PaymentService.CheckoutResponse> result =
                controller.checkout(user, "booking-1");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
    }

    @Test
    void nullPrincipalDoesNotReachCreatedResponse() {
        when(payments.checkout(null, "booking-1"))
                .thenThrow(com.testhakni.backend.shared.domain.exception.DomainException
                        .unauthorized("Authentification requise"));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.testhakni.backend.shared.domain.exception.DomainException.class,
                () -> controller.checkout(null, "booking-1"));
    }
}
