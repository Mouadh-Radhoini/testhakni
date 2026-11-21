package com.testhakni.backend.booking.application;

import java.time.Instant;
import java.util.List;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.model.BookingStatus;
import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.notification.application.NotificationService;
import com.testhakni.backend.request.domain.model.RequestStatus;
import com.testhakni.backend.request.domain.repository.ServiceRequestRepository;
import com.testhakni.backend.review.application.dto.Feedback;
import com.testhakni.backend.review.domain.model.Review;
import com.testhakni.backend.review.domain.repository.ReviewRepository;
import com.testhakni.backend.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'usage : suivi des reservations par leurs participants, puis avis du client. */
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookings;
    private final ReviewRepository reviews;
    private final ServiceRequestRepository requests;
    private final NotificationService notifications;

    public List<Booking> mine(AuthenticatedUser user) {
        return user.role() == Role.CUSTOMER ? bookings.findByCustomerId(user.id()) : bookings.findByProviderId(user.id());
    }

    public Booking detail(AuthenticatedUser user, String id) {
        var booking = bookings.findById(id).orElseThrow(() -> DomainException.notFound("Réservation introuvable"));
        if (!user.id().equals(booking.customerId()) && !user.id().equals(booking.providerId())) {
            throw DomainException.forbidden("Réservation privée");
        }
        return booking;
    }

    @Transactional
    public Booking transition(AuthenticatedUser user, String id, BookingStatus target) {
        var booking = detail(user, id);
        var expected = target.expectedPrevious()
                .orElseThrow(() -> DomainException.conflict("Transition impossible"));
        boolean confirmation = target == BookingStatus.COMPLETED;
        if (!user.id().equals(confirmation ? booking.customerId() : booking.providerId())) {
            throw DomainException.forbidden("Action réservée à l’autre participant");
        }
        var updated = bookings.transition(id, expected, new Booking.Transition(target, user.id(), Instant.now()))
                .orElseThrow(() -> DomainException.conflict("Transition impossible depuis le statut actuel"));
        if (confirmation && !requests.changeStatus(booking.requestId(), RequestStatus.IN_PROGRESS, RequestStatus.COMPLETED)) {
            throw DomainException.conflict("État de la demande incompatible");
        }
        notifications.send(confirmation ? booking.providerId() : booking.customerId(),
                confirmation ? "BOOKING_COMPLETED" : "BOOKING_UPDATED", id, "Réservation mise à jour : " + booking.title());
        return updated;
    }

    @Transactional
    public Review review(AuthenticatedUser user, String id, Feedback feedback) {
        var booking = detail(user, id);
        if (!booking.customerId().equals(user.id())) throw DomainException.forbidden("Avis réservé au client");
        if (booking.status() != BookingStatus.COMPLETED) throw DomainException.conflict("Fin de prestation non confirmée");
        var review = reviews.insert(new Review(null, id, booking.providerId(), booking.serviceId(), feedback.rating(),
                feedback.comment().trim(), false, Instant.now()));
        bookings.markReviewed(id);
        notifications.send(booking.providerId(), "REVIEW_RECEIVED", review.id(), "Vous avez reçu un avis");
        return review;
    }
}
