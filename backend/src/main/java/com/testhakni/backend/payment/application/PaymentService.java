package com.testhakni.backend.payment.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.payment.application.port.CheckoutGateway;
import com.testhakni.backend.payment.domain.model.Payment;
import com.testhakni.backend.payment.domain.model.PaymentStatus;
import com.testhakni.backend.payment.domain.repository.PaymentRepository;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("tnd", "usd", "eur");

    private final BookingRepository bookings;
    private final PaymentRepository payments;
    private final CheckoutGateway checkoutGateway;
    private final String configuredCurrency;

    public PaymentService(BookingRepository bookings, PaymentRepository payments,
                          CheckoutGateway checkoutGateway,
                          @Value("${STRIPE_CURRENCY:tnd}") String configuredCurrency) {
        this.bookings = bookings;
        this.payments = payments;
        this.checkoutGateway = checkoutGateway;
        this.configuredCurrency = configuredCurrency;
    }

    public CheckoutResponse checkout(AuthenticatedUser user, String bookingId) {
        if (user == null) {
            throw DomainException.unauthorized("Authentification requise");
        }
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> DomainException.notFound("Réservation introuvable"));
        if (!booking.customerId().equals(user.id())) {
            throw DomainException.forbidden("Cette réservation ne vous appartient pas");
        }

        String currency = normalizeCurrency(configuredCurrency);
        BigDecimal amount = booking.price();
        if (amount == null || amount.signum() <= 0) {
            throw DomainException.badRequest("Montant de réservation invalide");
        }

        var existing = payments.findByBookingId(bookingId);
        if (existing.isPresent()) {
            Payment previous = existing.get();
            if (previous.status() == PaymentStatus.PAID) {
                throw DomainException.conflict("Cette réservation est déjà payée");
            }
            if (previous.status() == PaymentStatus.PENDING && previous.stripeCheckoutSessionId() != null) {
                CheckoutGateway.SessionInfo info = checkoutGateway.inspect(previous.stripeCheckoutSessionId());
                if (info == null) {
                    info = new CheckoutGateway.SessionInfo(CheckoutGateway.SessionState.UNKNOWN, null);
                }
                if (info.state() == CheckoutGateway.SessionState.OPEN && info.url() != null) {
                    // Session encore valable : on la reprend au lieu d'en creer une seconde
                    return new CheckoutResponse(previous.id(), bookingId, info.url(),
                            previous.stripeCheckoutSessionId(), PaymentStatus.PENDING);
                }
                if (info.state() == CheckoutGateway.SessionState.EXPIRED) {
                    // Session expiree chez Stripe : on la ferme, une nouvelle sera creee
                    payments.markStatusIfAllowed(previous.id(), PaymentStatus.EXPIRED, Instant.now(), null);
                } else {
                    // Paiement en cours de confirmation (ou etat inconnu) : ne rien dupliquer
                    throw DomainException.conflict("Une session Checkout est déjà active");
                }
            }
        }

        Instant now = Instant.now();
        Payment payment = new Payment(
                UUID.randomUUID().toString(),
                bookingId,
                booking.customerId(),
                amount,
                currency,
                PaymentStatus.PENDING,
                null,
                null,
                now,
                now,
                null);
        payment = payments.save(payment);

        try {
            CheckoutGateway.CheckoutSession session = checkoutGateway.createCheckout(booking, payment);
            if (session == null || session.sessionId() == null || session.sessionId().isBlank()) {
                throw new CheckoutGateway.GatewayUnavailableException(
                        "Stripe n'a pas renvoyé de session Checkout");
            }
            if (!payments.markCheckoutSessionIfPending(payment.id(), session.sessionId(),
                    session.paymentIntentId())) {
                throw DomainException.conflict("Une session Checkout est déjà active");
            }
            return new CheckoutResponse(payment.id(), bookingId, session.url(),
                    session.sessionId(), PaymentStatus.PENDING);
        } catch (CheckoutGateway.GatewayUnavailableException ex) {
            payments.markStatusIfAllowed(payment.id(), PaymentStatus.FAILED, Instant.now(), null);
            throw new PaymentUnavailableException("Paiement Stripe indisponible");
        }
    }

    public PaymentStatusResponse status(AuthenticatedUser user, String bookingId) {
        if (user == null) {
            throw DomainException.unauthorized("Authentification requise");
        }
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> DomainException.notFound("Réservation introuvable"));
        if (!booking.customerId().equals(user.id())) {
            throw DomainException.forbidden("Cette réservation ne vous appartient pas");
        }
        Payment payment = payments.findByBookingId(bookingId)
                .orElseThrow(() -> DomainException.notFound("Paiement introuvable"));
        return new PaymentStatusResponse(payment.id(), payment.bookingId(), payment.amount(),
                payment.currency(), payment.status(), payment.paidAt());
    }

    public static long toMinorUnits(BigDecimal amount, String currency) {
        int scale = switch (currency.toLowerCase(Locale.ROOT)) {
            case "tnd" -> 3;
            case "usd", "eur" -> 2;
            default -> throw DomainException.badRequest("Devise Stripe non prise en charge");
        };
        try {
            return amount.setScale(scale, RoundingMode.UNNECESSARY)
                    .movePointRight(scale)
                    .setScale(0)
                    .longValueExact();
        } catch (ArithmeticException ex) {
            throw DomainException.badRequest("Montant ou devise invalide");
        }
    }

    private static String normalizeCurrency(String currency) {
        String normalized = currency == null ? "" : currency.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_CURRENCIES.contains(normalized)) {
            throw DomainException.badRequest("Devise Stripe non prise en charge");
        }
        return normalized;
    }

    public record CheckoutResponse(String paymentId, String bookingId, String checkoutUrl,
                                   String checkoutSessionId, PaymentStatus status) {}

    public record PaymentStatusResponse(String paymentId, String bookingId, BigDecimal amount,
                                        String currency, PaymentStatus status, Instant paidAt) {}

    public static class PaymentUnavailableException extends RuntimeException {
        public PaymentUnavailableException(String message) {
            super(message);
        }
    }
}
