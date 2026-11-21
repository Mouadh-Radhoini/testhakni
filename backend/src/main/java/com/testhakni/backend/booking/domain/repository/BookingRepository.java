package com.testhakni.backend.booking.domain.repository;

import java.util.List;
import java.util.Optional;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.model.BookingStatus;

/** Port du domaine : reservations nees d'une offre acceptee. */
public interface BookingRepository {

    Booking insert(Booking booking);

    Optional<Booking> findById(String id);

    Optional<Booking> findByRequestId(String requestId);

    List<Booking> findByCustomerId(String customerId);

    List<Booking> findByProviderId(String providerId);

    /**
     * Transition atomique : applique {@code transition} seulement si la reservation est encore
     * dans le statut {@code expected}. Renvoie la reservation mise a jour, ou vide sinon.
     */
    Optional<Booking> transition(String id, BookingStatus expected, Booking.Transition transition);

    void markReviewed(String id);

    long count();
}
