package com.testhakni.backend.booking.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.model.BookingStatus;
import com.testhakni.backend.booking.domain.repository.BookingRepository;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/** Adaptateur MongoDB du port {@link BookingRepository}. */
@Repository
class BookingRepositoryAdapter implements BookingRepository {

    private final BookingMongoRepository repository;
    private final MongoTemplate mongo;

    BookingRepositoryAdapter(BookingMongoRepository repository, MongoTemplate mongo) {
        this.repository = repository;
        this.mongo = mongo;
    }

    @Override
    public Booking insert(Booking booking) {
        return repository.insert(booking);
    }

    @Override
    public Optional<Booking> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Optional<Booking> findByRequestId(String requestId) {
        return repository.findByRequestId(requestId);
    }

    @Override
    public List<Booking> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    @Override
    public List<Booking> findByProviderId(String providerId) {
        return repository.findByProviderId(providerId);
    }

    @Override
    public Optional<Booking> transition(String id, BookingStatus expected, Booking.Transition transition) {
        return Optional.ofNullable(mongo.findAndModify(
                Query.query(Criteria.where("_id").is(id).and("status").is(expected)),
                Update.update("status", transition.status()).push("history", transition),
                FindAndModifyOptions.options().returnNew(true), Booking.class));
    }

    @Override
    public void markReviewed(String id) {
        mongo.updateFirst(Query.query(Criteria.where("_id").is(id)), Update.update("reviewed", true), Booking.class);
    }

    @Override
    public long count() {
        return repository.count();
    }
}
