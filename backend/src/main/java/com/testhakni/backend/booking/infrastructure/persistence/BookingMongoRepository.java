package com.testhakni.backend.booking.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.booking.domain.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Spring Data : detail technique, utilise uniquement par l'adaptateur. */
interface BookingMongoRepository extends MongoRepository<Booking, String> {

    Optional<Booking> findByRequestId(String requestId);

    List<Booking> findByCustomerId(String customerId);

    List<Booking> findByProviderId(String providerId);
}
