package com.testhakni.backend.service;

import java.time.Instant;
import java.util.List;
import com.testhakni.backend.model.*;
import com.testhakni.backend.dto.MarketplacePayloads.Feedback;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.repository.*;
import com.testhakni.backend.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookings;
    private final ReviewRepository reviews;
    private final NotificationService notifications;
    private final MongoTemplate mongo;

    public List<Booking> mine(AuthenticatedUser user) {
        return user.role() == Role.CUSTOMER ? bookings.findByCustomerId(user.id()) : bookings.findByProviderId(user.id());
    }

    public Booking detail(AuthenticatedUser user, String id) {
        var booking = bookings.findById(id).orElseThrow(() -> ApiException.notFound("Réservation introuvable"));
        if (!user.id().equals(booking.customerId()) && !user.id().equals(booking.providerId())) {
            throw ApiException.forbidden("Réservation privée");
        }
        return booking;
    }

    @Transactional
    public Booking transition(AuthenticatedUser user, String id, BookingStatus target) {
        var booking = detail(user, id);
        var expected = switch (target) {
            case IN_PROGRESS -> BookingStatus.BOOKED;
            case AWAITING_CONFIRMATION -> BookingStatus.IN_PROGRESS;
            case COMPLETED -> BookingStatus.AWAITING_CONFIRMATION;
            default -> throw ApiException.conflict("Transition impossible");
        };
        boolean confirmation = target == BookingStatus.COMPLETED;
        if (!user.id().equals(confirmation ? booking.customerId() : booking.providerId())) {
            throw ApiException.forbidden("Action réservée à l’autre participant");
        }
        var updated = mongo.findAndModify(Query.query(Criteria.where("_id").is(id).and("status").is(expected)),
                Update.update("status", target).push("history", new Booking.Transition(target, user.id(), Instant.now())),
                FindAndModifyOptions.options().returnNew(true), Booking.class);
        if (updated == null) throw ApiException.conflict("Transition impossible depuis le statut actuel");
        if (confirmation) {
            var result = mongo.updateFirst(Query.query(Criteria.where("_id").is(booking.requestId()).and("status")
                    .is(RequestStatus.IN_PROGRESS)), Update.update("status", RequestStatus.COMPLETED).currentDate("updatedAt"),
                    ServiceRequest.class);
            if (result.getModifiedCount() != 1) throw ApiException.conflict("État de la demande incompatible");
        }
        notifications.send(confirmation ? booking.providerId() : booking.customerId(),
                confirmation ? "BOOKING_COMPLETED" : "BOOKING_UPDATED", id, "Réservation mise à jour : " + booking.title());
        return updated;
    }

    @Transactional
    public Review review(AuthenticatedUser user, String id, Feedback feedback) {
        var booking = detail(user, id);
        if (!booking.customerId().equals(user.id())) throw ApiException.forbidden("Avis réservé au client");
        if (booking.status() != BookingStatus.COMPLETED) throw ApiException.conflict("Fin de prestation non confirmée");
        var review = reviews.insert(new Review(null, id, booking.providerId(), booking.serviceId(), feedback.rating(),
                feedback.comment().trim(), false, Instant.now()));
        mongo.updateFirst(Query.query(Criteria.where("_id").is(id)), Update.update("reviewed", true), Booking.class);
        notifications.send(booking.providerId(), "REVIEW_RECEIVED", review.id(), "Vous avez reçu un avis");
        return review;
    }
}
