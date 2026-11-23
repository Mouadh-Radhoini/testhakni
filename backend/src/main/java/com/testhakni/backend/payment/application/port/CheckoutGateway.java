package com.testhakni.backend.payment.application.port;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.payment.domain.model.Payment;

public interface CheckoutGateway {
    CheckoutSession createCheckout(Booking booking, Payment payment);

    record CheckoutSession(String sessionId, String url, String paymentIntentId) {}

    /** Etat d'une session Checkout deja creee (UNKNOWN par defaut : on ne reutilise rien). */
    enum SessionState { OPEN, COMPLETE, EXPIRED, UNKNOWN }

    record SessionInfo(SessionState state, String url) {}

    default SessionInfo inspect(String sessionId) {
        return new SessionInfo(SessionState.UNKNOWN, null);
    }

    class GatewayUnavailableException extends RuntimeException {
        public GatewayUnavailableException(String message) {
            super(message);
        }
    }
}
