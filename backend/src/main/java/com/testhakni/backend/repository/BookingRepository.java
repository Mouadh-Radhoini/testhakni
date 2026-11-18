package com.testhakni.backend.repository;

import com.testhakni.backend.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {
    Optional<Booking> findByRequestId(String requestId);
    List<Booking> findByCustomerId(String customerId);
    List<Booking> findByProviderId(String providerId);
}
