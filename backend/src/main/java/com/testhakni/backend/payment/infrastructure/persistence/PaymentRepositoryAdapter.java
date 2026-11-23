package com.testhakni.backend.payment.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;

import com.testhakni.backend.payment.domain.model.Payment;
import com.testhakni.backend.payment.domain.model.PaymentStatus;
import com.testhakni.backend.payment.domain.repository.PaymentRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepository {
    private final MongoTemplate mongo;

    public PaymentRepositoryAdapter(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Payment> findByBookingId(String bookingId) {
        return Optional.ofNullable(mongo.findOne(
                Query.query(Criteria.where("bookingId").is(bookingId)), Payment.class));
    }

    @Override
    public Optional<Payment> findByCheckoutSessionId(String checkoutSessionId) {
        return Optional.ofNullable(mongo.findOne(
                Query.query(Criteria.where("stripeCheckoutSessionId").is(checkoutSessionId)),
                Payment.class));
    }

    @Override
    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(mongo.findById(paymentId, Payment.class));
    }

    @Override
    public Payment save(Payment payment) {
        return mongo.insert(payment);
    }

    @Override
    public boolean markCheckoutSessionIfPending(String paymentId, String checkoutSessionId,
                                                String paymentIntentId) {
        Update update = Update.update("stripeCheckoutSessionId", checkoutSessionId)
                .set("updatedAt", Instant.now());
        if (paymentIntentId != null) {
            update.set("stripePaymentIntentId", paymentIntentId);
        }
        return mongo.findAndModify(
                Query.query(Criteria.where("_id").is(paymentId).and("status").is(PaymentStatus.PENDING)),
                update,
                FindAndModifyOptions.options().returnNew(true),
                Payment.class) != null;
    }

    @Override
    public boolean markPaidByCheckoutSession(String checkoutSessionId, String paymentIntentId,
                                             Instant paidAt) {
        Update update = Update.update("status", PaymentStatus.PAID)
                .set("paidAt", paidAt)
                .set("updatedAt", paidAt);
        if (paymentIntentId != null) {
            update.set("stripePaymentIntentId", paymentIntentId);
        }
        return mongo.findAndModify(
                Query.query(Criteria.where("stripeCheckoutSessionId").is(checkoutSessionId)
                        .and("status").is(PaymentStatus.PENDING)),
                update,
                FindAndModifyOptions.options().returnNew(true),
                Payment.class) != null;
    }

    @Override
    public boolean markFailedByCheckoutSession(String checkoutSessionId, Instant updatedAt) {
        return markTerminalBySession(checkoutSessionId, PaymentStatus.FAILED, updatedAt);
    }

    @Override
    public boolean markCancelledByCheckoutSession(String checkoutSessionId, Instant updatedAt) {
        return markTerminalBySession(checkoutSessionId, PaymentStatus.CANCELLED, updatedAt);
    }

    @Override
    public boolean markExpiredByCheckoutSession(String checkoutSessionId, Instant updatedAt) {
        return markTerminalBySession(checkoutSessionId, PaymentStatus.EXPIRED, updatedAt);
    }

    @Override
    public boolean markStatusIfAllowed(String paymentId, PaymentStatus target, Instant updatedAt,
                                       Instant paidAt) {
        Payment payment = mongo.findById(paymentId, Payment.class);
        if (payment == null || !payment.status().canTransitionTo(target)) {
            return false;
        }
        Update update = Update.update("status", target).set("updatedAt", updatedAt);
        if (paidAt != null) {
            update.set("paidAt", paidAt);
        }
        return mongo.findAndModify(
                Query.query(Criteria.where("_id").is(paymentId).and("status").is(payment.status())),
                update,
                FindAndModifyOptions.options().returnNew(true),
                Payment.class) != null;
    }

    private boolean markTerminalBySession(String checkoutSessionId, PaymentStatus target,
                                          Instant updatedAt) {
        return mongo.findAndModify(
                Query.query(Criteria.where("stripeCheckoutSessionId").is(checkoutSessionId)
                        .and("status").is(PaymentStatus.PENDING)),
                Update.update("status", target).set("updatedAt", updatedAt),
                FindAndModifyOptions.options().returnNew(true),
                Payment.class) != null;
    }
}
