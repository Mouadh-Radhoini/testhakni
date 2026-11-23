package com.testhakni.backend.payment.infrastructure.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;
import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.payment.application.PaymentService;
import com.testhakni.backend.payment.application.port.CheckoutGateway;
import com.testhakni.backend.payment.domain.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeCheckoutGateway implements CheckoutGateway {
    private final String secretKey;
    private final String currency;
    private final String frontendBaseUrl;

    public StripeCheckoutGateway(
            @Value("${app.stripe.secret-key:}") String secretKey,
            @Value("${app.stripe.currency:tnd}") String currency,
            @Value("${app.stripe.frontend-base-url:http://localhost:4200}") String frontendBaseUrl) {
        this.secretKey = secretKey == null ? "" : secretKey.trim();
        this.currency = currency == null ? "tnd" : currency.trim().toLowerCase();
        this.frontendBaseUrl = frontendBaseUrl == null
                ? "http://localhost:4200"
                : frontendBaseUrl.replaceAll("/+$", "");
    }

    @Override
    public CheckoutSession createCheckout(Booking booking, Payment payment) {
        if (secretKey.isBlank()) {
            throw new GatewayUnavailableException("Stripe is not configured");
        }
        if (payment.amount() == null || payment.amount().signum() <= 0) {
            throw new GatewayUnavailableException("Invalid payment amount");
        }
        if (!payment.currency().equalsIgnoreCase(currency)) {
            throw new GatewayUnavailableException("Payment currency is not configured");
        }

        long minorUnits;
        try {
            minorUnits = PaymentService.toMinorUnits(payment.amount(), currency);
        } catch (RuntimeException ex) {
            throw new GatewayUnavailableException("Invalid payment amount or currency");
        }
        if (minorUnits <= 0) {
            throw new GatewayUnavailableException("Invalid payment amount");
        }

        String successUrl = frontendBaseUrl + "/payments/success"
                + "?bookingId=" + booking.id() + "&session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = frontendBaseUrl + "/payments/cancel"
                + "?bookingId=" + booking.id();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setClientReferenceId(booking.id())
                .putMetadata("bookingId", booking.id())
                .putMetadata("paymentId", payment.id())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount(minorUnits)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("TestHakni booking " + booking.id())
                                        .build())
                                .build())
                        .build())
                .build();

        try {
            Stripe.apiKey = secretKey;
            Session session = Session.create(params,
                    RequestOptions.builder()
                            .setIdempotencyKey("testhakni-payment-" + payment.id())
                            .build());
            return new CheckoutSession(session.getId(), session.getUrl(),
                    session.getPaymentIntent());
        } catch (StripeException ex) {
            throw new GatewayUnavailableException("Stripe Checkout creation failed");
        }
    }
}
