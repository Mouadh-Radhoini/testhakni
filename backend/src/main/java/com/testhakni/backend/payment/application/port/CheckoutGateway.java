package com.testhakni.backend.payment.application.port;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.payment.domain.model.Payment;

public interface CheckoutGateway {
    CheckoutSession createCheckout(Booking booking, Payment payment);

    record CheckoutSession(String sessionId, String url, String paymentIntentId) {}

    class GatewayUnavailableException extends RuntimeException {
        public GatewayUnavailableException(String message) {
            super(message);
        }
    }
}
