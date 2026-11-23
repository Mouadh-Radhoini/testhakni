package com.testhakni.backend.payment.presentation;

import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.payment.application.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService payments;

    public PaymentController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping("/bookings/{bookingId}/checkout")
    public ResponseEntity<PaymentService.CheckoutResponse> checkout(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String bookingId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(payments.checkout(user, bookingId));
        } catch (PaymentService.PaymentUnavailableException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @GetMapping("/bookings/{bookingId}")
    public PaymentService.PaymentStatusResponse status(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String bookingId) {
        return payments.status(user, bookingId);
    }
}
