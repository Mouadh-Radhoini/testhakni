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

    /**
     * Le client fait avancer sa reservation : la declarer en cours (le prestataire est au travail),
     * terminee, ou l'annuler. Reserve au client de la reservation ; la demande liee suit le meme sort.
     */
    @Transactional
    public Booking customerSet(AuthenticatedUser user, String id, BookingStatus target) {
        var booking = detail(user, id);
        if (!user.id().equals(booking.customerId())) {
            throw DomainException.forbidden("Action réservée au client");
        }
        var sources = BookingStatus.customerSourcesFor(target);
        if (sources.isEmpty()) throw DomainException.badRequest("Statut autorisé : IN_PROGRESS, COMPLETED ou CANCELLED");
        var updated = bookings.transitionFrom(id, sources, new Booking.Transition(target, user.id(), Instant.now()))
                .orElseThrow(() -> DomainException.conflict("Action impossible depuis le statut actuel"));
        // La demande liee : terminee ou annulee avec la reservation (reste "en cours" pour IN_PROGRESS)
        if (target == BookingStatus.COMPLETED
                && !requests.changeStatus(booking.requestId(), RequestStatus.IN_PROGRESS, RequestStatus.COMPLETED)) {
            throw DomainException.conflict("État de la demande incompatible");
        }
        if (target == BookingStatus.CANCELLED
                && !requests.changeStatus(booking.requestId(), RequestStatus.IN_PROGRESS, RequestStatus.CANCELLED)) {
            throw DomainException.conflict("État de la demande incompatible");
        }
        String type = switch (target) {
            case COMPLETED -> "BOOKING_COMPLETED";
            case CANCELLED -> "BOOKING_CANCELLED";
            default -> "BOOKING_UPDATED";
        };
        String text = switch (target) {
            case COMPLETED -> "Le client a confirmé la fin de la prestation : ";
            case CANCELLED -> "Le client a annulé la réservation : ";
            default -> "Le client indique que le travail a commencé : ";
        };
        notifications.send(booking.providerId(), type, id, text + booking.title());
        return updated;
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
