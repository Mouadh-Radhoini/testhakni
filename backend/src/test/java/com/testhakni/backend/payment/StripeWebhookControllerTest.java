package com.testhakni.backend.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.testhakni.backend.payment.application.PaymentWebhookService;
import com.testhakni.backend.payment.presentation.StripeWebhookController;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class StripeWebhookControllerTest {
    @Test
    void rejectsMissingSignature() {
        PaymentWebhookService service = mock(PaymentWebhookService.class);
        StripeWebhookController controller =
                new StripeWebhookController(service, "whsec_test");
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<?> response = controller.webhook(request, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void rejectsInvalidSignature() throws Exception {
        PaymentWebhookService service = mock(PaymentWebhookService.class);
        StripeWebhookController controller =
                new StripeWebhookController(service, "whsec_test");
        HttpServletRequest request = requestWithBody("{}");

        ResponseEntity<?> response = controller.webhook(request, "t=1,v1=invalid");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void acceptsValidSignatureBeforeReturningBadRequestForUnknownPayload() throws Exception {
        PaymentWebhookService service = mock(PaymentWebhookService.class);
        StripeWebhookController controller =
                new StripeWebhookController(service, "whsec_test");
        long timestamp = Instant.now().getEpochSecond();
        String payload = "{\"id\":\"evt_test\",\"object\":\"event\",\"api_version\":\"2025-02-24.acacia\",\"created\":"
                + timestamp + ",\"livemode\":false,\"pending_webhooks\":1,\"request\":null,"
                + "\"type\":\"checkout.session.completed\",\"data\":{\"object\":{\"object\":\"checkout.session\","
                + "\"id\":\"cs_test\",\"amount_total\":12500,\"currency\":\"tnd\",\"payment_status\":\"paid\","
                + "\"payment_intent\":\"pi_test\",\"metadata\":{\"bookingId\":\"booking-1\",\"paymentId\":\"payment-1\"}}}}";
        HttpServletRequest request = requestWithBody(payload);
        String signedPayload = timestamp + "." + payload;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("whsec_test".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = "t=" + timestamp + ",v1=" + bytesToHex(mac.doFinal(
                signedPayload.getBytes(StandardCharsets.UTF_8)));

        ResponseEntity<?> response = controller.webhook(request, signature);

        assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
    }

    private HttpServletRequest requestWithBody(String body) throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ServletInputStream stream = new ServletInputStream() {
            private int index;
            @Override
            public int read() {
                return index >= bytes.length ? -1 : bytes[index++] & 0xff;
            }
            @Override
            public boolean isFinished() {
                return index >= bytes.length;
            }
            @Override
            public boolean isReady() {
                return true;
            }
            @Override
            public void setReadListener(jakarta.servlet.ReadListener readListener) {}
        };
        org.mockito.Mockito.when(request.getInputStream()).thenReturn(stream);
        return request;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte value : bytes) {
            result.append(String.format("%02x", value));
        }
        return result.toString();
    }
}
