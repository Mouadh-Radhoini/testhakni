package com.testhakni.backend.offer.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.model.BookingStatus;
import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.catalog.domain.model.ServiceOffering;
import com.testhakni.backend.catalog.domain.repository.ServiceOfferingRepository;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.notification.application.NotificationService;
import com.testhakni.backend.offer.application.dto.OpenRequest;
import com.testhakni.backend.offer.application.dto.Proposal;
import com.testhakni.backend.offer.domain.model.Offer;
import com.testhakni.backend.offer.domain.model.OfferStatus;
import com.testhakni.backend.offer.domain.repository.OfferRepository;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.request.domain.model.RequestStatus;
import com.testhakni.backend.request.domain.model.ServiceRequest;
import com.testhakni.backend.request.domain.repository.ServiceRequestRepository;
import com.testhakni.backend.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cas d'usage : decouverte des demandes, offres, et acceptation (creation de la reservation). */
@Service
@RequiredArgsConstructor
public class OfferService {
    private final OfferRepository offers;
    private final BookingRepository bookings;
    private final ProviderProfileRepository profiles;
    private final UserRepository users;
    private final ServiceRequestRepository requests;
    private final ServiceOfferingRepository services;
    private final NotificationService notifications;

    private ProviderProfile profile(String provider) {
        return profiles.findByUserId(provider).orElseThrow(() -> DomainException.notFound("Profil introuvable"));
    }

    public List<OpenRequest> discover(String provider) {
        var p = profile(provider);
        if (!p.isAvailable()) return List.of();
        return requests.findOpenMatching(p.getCategories(), p.getCity()).stream().map(OpenRequest::from).toList();
    }

    @Transactional
    public Offer submit(String provider, String id, Proposal proposal) {
        var p = profile(provider);
        if (!p.isAvailable()) throw DomainException.conflict("Profil indisponible");
        // Every submission writes the request so acceptance and cancellation cannot miss a late offer.
        var request = requests.touchIfOpenMatching(id, p.getCategories(), p.getCity()).orElseThrow(() -> {
            if (!requests.existsById(id)) return DomainException.notFound("Demande introuvable");
            return DomainException.conflict("Demande indisponible ou hors de votre zone");
        });
        var offer = offers.insert(new Offer(null, id, provider, p.getBusinessName(), request.getTitle(), proposal.price(),
                proposal.proposedDate(), proposal.description().trim(), OfferStatus.SUBMITTED, Instant.now()));
        notifications.send(request.getCustomerId(), "OFFER_RECEIVED", id, "Nouvelle offre pour " + request.getTitle());
        return offer;
    }

    public List<Offer> mine(String provider) {
        return offers.findByProviderId(provider);
    }

    private ServiceRequest owned(String customer, String id) {
        var request = requests.findById(id).orElseThrow(() -> DomainException.notFound("Demande introuvable"));
        if (!customer.equals(request.getCustomerId())) throw DomainException.forbidden("Demande privée");
        return request;
    }

    public List<Offer> received(String customer, String id) {
        owned(customer, id);
        return offers.findByRequestId(id);
    }

    @Transactional
    public void withdraw(String provider, String id) {
        var offer = offers.findById(id).orElseThrow(() -> DomainException.notFound("Offre introuvable"));
        if (!provider.equals(offer.providerId())) throw DomainException.forbidden("Offre privée");
        change(offer, OfferStatus.CANCELLED);
        var request = requests.findById(offer.requestId()).orElseThrow();
        notifications.send(request.getCustomerId(), "OFFER_CANCELLED", request.getId(), "Une offre a été retirée");
    }

    private void change(Offer offer, OfferStatus status) {
        if (!offers.changeStatusIfSubmitted(offer.id(), status)) throw DomainException.conflict("Offre déjà traitée");
    }

    /** Clot les offres encore en attente d'une demande (acceptation d'une autre offre ou annulation). */
    public void closeRemaining(String requestId, OfferStatus status) {
        for (var other : offers.findByRequestId(requestId)) {
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
                .orElseThrow(() -> DomainException.notFound("Offre introuvable"));
        if (r.getStatus() == RequestStatus.IN_PROGRESS) {
            return bookings.findByRequestId(requestId).filter(b -> offerId.equals(b.offerId()))
                    .orElseThrow(() -> DomainException.conflict("Demande déjà traitée"));
        }
        if (r.getStatus() != RequestStatus.OPEN) throw DomainException.conflict("Demande clôturée");
        if (offer.proposedDate().isBefore(LocalDate.now()) ||
                !users.findById(offer.providerId()).map(User::isActive).orElse(false)) {
            throw DomainException.conflict("Offre expirée ou prestataire suspendu");
        }
        if (!requests.changeStatus(requestId, RequestStatus.OPEN, RequestStatus.IN_PROGRESS)) {
            throw DomainException.conflict("Demande déjà traitée");
        }
        change(offer, OfferStatus.ACCEPTED);
        closeRemaining(requestId, OfferStatus.REJECTED);
        String serviceId = r.getServiceId() == null ? null : services.findById(r.getServiceId())
                .filter(s -> offer.providerId().equals(s.getProviderId())).map(ServiceOffering::getId).orElse(null);
        var booking = bookings.insert(new Booking(null, requestId, offerId, customer, offer.providerId(), serviceId,
                r.getTitle(), r.getAddress(), offer.price(), offer.proposedDate(), BookingStatus.BOOKED, false,
                // Precision a la milliseconde, comme MongoDB : la reponse egale la reservation relue.
                List.of(new Booking.Transition(BookingStatus.BOOKED, customer, Instant.ofEpochMilli(System.currentTimeMillis())))));
        notifications.send(offer.providerId(), "OFFER_ACCEPTED", booking.id(), "Votre offre a été acceptée");
        return booking;
    }
}
