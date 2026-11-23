package com.testhakni.backend.payment.presentation;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.testhakni.backend.payment.application.PaymentWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/stripe")
public class StripeWebhookController {
    private final PaymentWebhookService paymentWebhooks;
    private final String webhookSecret;

    public StripeWebhookController(PaymentWebhookService paymentWebhooks,
                                   @Value("${app.stripe.webhook-secret:}") String webhookSecret) {
        this.paymentWebhooks = paymentWebhooks;
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(
            HttpServletRequest request,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        if (webhookSecret.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("received", false));
        }
        if (signature == null || signature.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("received", false));
        }

        final byte[] body;
        try {
            body = request.getInputStream().readAllBytes();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("received", false));
        }

        try {
            Event event = Webhook.constructEvent(
                    new String(body, StandardCharsets.UTF_8), signature, webhookSecret);
            boolean processed = process(event);
            return ResponseEntity.ok(Map.of("received", true, "processed", processed));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("received", false));
        }
    }

    private boolean process(Event event) throws Exception {
        if (event == null) {
            return false;
        }
        StripeObject object = event.getDataObjectDeserializer().deserializeUnsafe();
        if (!(object instanceof Session session)) {
            return false;
        }
        String bookingId = session.getMetadata() == null ? null : session.getMetadata().get("bookingId");
        String paymentId = session.getMetadata() == null ? null : session.getMetadata().get("paymentId");
        PaymentWebhookService.CheckoutSessionEvent data =
                new PaymentWebhookService.CheckoutSessionEvent(
                        session.getId(),
                        session.getPaymentIntent(),
                        session.getPaymentStatus(),
                        session.getAmountTotal() == null ? null : session.getAmountTotal().longValue(),
                        session.getCurrency(),
                        bookingId,
                        paymentId);
        return switch (event.getType()) {
            case "checkout.session.completed" -> paymentWebhooks.completed(data);
            case "checkout.session.async_payment_succeeded" -> paymentWebhooks.asyncSuccess(data);
            case "checkout.session.async_payment_failed" -> paymentWebhooks.asyncFailure(data);
            case "checkout.session.expired" -> paymentWebhooks.expired(data);
            default -> false;
        };
    }
}
