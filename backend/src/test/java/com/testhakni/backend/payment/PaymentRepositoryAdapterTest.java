package com.testhakni.backend.payment;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;

import com.testhakni.backend.payment.domain.model.Payment;
import com.testhakni.backend.payment.domain.model.PaymentStatus;
import com.testhakni.backend.payment.infrastructure.persistence.PaymentRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

class PaymentRepositoryAdapterTest {
    @Test
    void atomicPaidUpdateUsesCheckoutSessionAndPendingStatus() {
        MongoTemplate mongo = mock(MongoTemplate.class);
        PaymentRepositoryAdapter adapter = new PaymentRepositoryAdapter(mongo);
        Payment payment = new Payment("payment-1", "booking-1", "customer-1",
                new BigDecimal("12.500"), "tnd", PaymentStatus.PENDING,
                "cs_test", null, Instant.now(), Instant.now(), null);
        when(mongo.findAndModify(any(Query.class), any(Update.class),
                any(FindAndModifyOptions.class), eq(Payment.class))).thenReturn(payment);

        assertTrue(adapter.markPaidByCheckoutSession("cs_test", "pi_test", Instant.now()));
    }
}
