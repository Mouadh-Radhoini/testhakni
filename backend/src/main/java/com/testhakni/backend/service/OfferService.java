package com.testhakni.backend.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import com.testhakni.backend.dto.MarketplacePayloads.*;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.model.*;
import com.testhakni.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OfferService {
    private final OfferRepository offers;
    private final BookingRepository bookings;
    private final ProviderProfileRepository profiles;
    private final UserRepository users;
    private final ServiceRequestRepository requests;
    private final ServiceOfferingRepository services;
    private final MongoTemplate mongo;
    private final NotificationService notifications;

    private ProviderProfile profile(String provider) {
        return profiles.findByUserId(provider).orElseThrow(() -> ApiException.notFound("Profil introuvable"));
    }

    private Criteria eligible(ProviderProfile p) {
        return Criteria.where("status").is(RequestStatus.OPEN).and("category").in(p.getCategories())
                .and("city").regex(Pattern.compile("^" + Pattern.quote(p.getCity()) + "$",
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
    }

    public List<OpenRequest> discover(String provider) {
        var p = profile(provider);
        if (!p.isAvailable()) return List.of();
        return mongo.find(Query.query(eligible(p)).with(Sort.by("createdAt").descending()), ServiceRequest.class)
                .stream().map(OpenRequest::from).toList();
    }

    @Transactional
    public Offer submit(String provider, String id, Proposal proposal) {
        var p = profile(provider);
        if (!p.isAvailable()) throw ApiException.conflict("Profil indisponible");
        // Every submission writes the request so acceptance and cancellation cannot miss a late offer.
        var request = mongo.findAndModify(Query.query(eligible(p).and("_id").is(id)),
                new Update().inc("offerRevision", 1), ServiceRequest.class);
        if (request == null) {
            if (!requests.existsById(id)) throw ApiException.notFound("Demande introuvable");
            throw ApiException.conflict("Demande indisponible ou hors de votre zone");
        }
        var offer = offers.insert(new Offer(null, id, provider, p.getBusinessName(), request.getTitle(), proposal.price(),
                proposal.proposedDate(), proposal.description().trim(), OfferStatus.SUBMITTED, Instant.now()));
        notifications.send(request.getCustomerId(), "OFFER_RECEIVED", id, "Nouvelle offre pour " + request.getTitle());
        return offer;
    }

    public List<Offer> mine(String provider) {
        return offers.findByProviderIdOrderByCreatedAtDesc(provider);
    }

    private ServiceRequest owned(String customer, String id) {
        var request = requests.findById(id).orElseThrow(() -> ApiException.notFound("Demande introuvable"));
        if (!customer.equals(request.getCustomerId())) throw ApiException.forbidden("Demande privée");
        return request;
    }

    public List<Offer> received(String customer, String id) {
        owned(customer, id);
        return offers.findByRequestIdOrderByPriceAsc(id);
    }

    @Transactional
    public void withdraw(String provider, String id) {
        var offer = offers.findById(id).orElseThrow(() -> ApiException.notFound("Offre introuvable"));
        if (!provider.equals(offer.providerId())) throw ApiException.forbidden("Offre privée");
        change(offer, OfferStatus.CANCELLED);
        var request = requests.findById(offer.requestId()).orElseThrow();
        notifications.send(request.getCustomerId(), "OFFER_CANCELLED", request.getId(), "Une offre a été retirée");
    }

    private void change(Offer offer, OfferStatus status) {
        var result = mongo.updateFirst(Query.query(Criteria.where("_id").is(offer.id())
                .and("status").is(OfferStatus.SUBMITTED)), Update.update("status", status), Offer.class);
        if (result.getModifiedCount() != 1) throw ApiException.conflict("Offre déjà traitée");
    }

    void closeRemaining(String requestId, OfferStatus status) {
        for (var other : offers.findByRequestIdOrderByPriceAsc(requestId)) {
            if (other.status() != OfferStatus.SUBMITTED) continue;
            change(other, status);
            notifications.send(other.providerId(), "OFFER_" + status, other.id(),
                    status == OfferStatus.REJECTED ? "Une autre offre a été retenue" : "La demande a été annulée");
        }
    }

    @Transactional
    public Booking accept(String customer, String requestId, String offerId) {
        var r = owned(customer, requestId);
        var offer = offers.findById(offerId).filter(o -> requestId.equals(o.requestId()))
                .orElseThrow(() -> ApiException.notFound("Offre introuvable"));
        if (r.getStatus() == RequestStatus.IN_PROGRESS) {
            return bookings.findByRequestId(requestId).filter(b -> offerId.equals(b.offerId()))
                    .orElseThrow(() -> ApiException.conflict("Demande déjà traitée"));
        }
        if (r.getStatus() != RequestStatus.OPEN) throw ApiException.conflict("Demande clôturée");
        if (offer.proposedDate().isBefore(LocalDate.now()) ||
                !users.findById(offer.providerId()).map(User::isActive).orElse(false)) {
            throw ApiException.conflict("Offre expirée ou prestataire suspendu");
        }
        var claim = mongo.updateFirst(Query.query(Criteria.where("_id").is(requestId).and("status").is(RequestStatus.OPEN)),
                Update.update("status", RequestStatus.IN_PROGRESS).currentDate("updatedAt"), ServiceRequest.class);
        if (claim.getModifiedCount() != 1) throw ApiException.conflict("Demande déjà traitée");
        change(offer, OfferStatus.ACCEPTED);
        closeRemaining(requestId, OfferStatus.REJECTED);
        String serviceId = r.getServiceId() == null ? null : services.findById(r.getServiceId())
                .filter(s -> offer.providerId().equals(s.getProviderId())).map(ServiceOffering::getId).orElse(null);
        var booking = bookings.insert(new Booking(null, requestId, offerId, customer, offer.providerId(), serviceId,
                r.getTitle(), r.getAddress(), offer.price(), offer.proposedDate(), BookingStatus.BOOKED, false,
                List.of(new Booking.Transition(BookingStatus.BOOKED, customer, Instant.ofEpochMilli(System.currentTimeMillis())))));
        notifications.send(offer.providerId(), "OFFER_ACCEPTED", booking.id(), "Votre offre a été acceptée");
        return booking;
    }
}
