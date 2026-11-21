package com.testhakni.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import com.testhakni.backend.dto.*;
import com.testhakni.backend.dto.MarketplacePayloads.*;
import com.testhakni.backend.model.*;
import com.testhakni.backend.service.*;
import com.testhakni.backend.security.AuthenticatedUser;
import com.testhakni.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Parcours de réservation")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MarketplaceController {
    private final OfferService offers;
    private final BookingService bookings;
    private final NotificationService notifications;
    private final ReviewRepository reviews;
    private final ServiceOfferingService catalogue;
    private final ProviderProfileService profiles;

    @Operation(summary = "Prestataire · demandes ouvertes compatibles")
    @GetMapping("/providers/me/requests")
    public List<OpenRequest> open(@AuthenticationPrincipal AuthenticatedUser user) {
        return offers.discover(user.id());
    }

    @Operation(summary = "Prestataire · offres envoyées")
    @GetMapping("/providers/me/offers")
    public List<Offer> mine(@AuthenticationPrincipal AuthenticatedUser user) {
        return offers.mine(user.id());
    }

    @Operation(summary = "Prestataire · proposer une intervention")
    @PostMapping("/requests/{id}/offers")
    @ResponseStatus(HttpStatus.CREATED)
    public Offer submit(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id,
                        @Valid @RequestBody Proposal body) {
        return offers.submit(user.id(), id, body);
    }

    @Operation(summary = "Client propriétaire · comparer les offres")
    @GetMapping("/requests/{id}/offers")
    public List<Offer> received(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        return offers.received(user.id(), id);
    }

    @Operation(summary = "Prestataire · retirer une offre en attente")
    @PatchMapping("/providers/me/offers/{id}/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        offers.withdraw(user.id(), id);
    }

    @Operation(summary = "Client propriétaire · accepter une offre et réserver")
    @PostMapping("/requests/{id}/offers/{offerId}/accept")
    public Booking accept(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id, @PathVariable String offerId) {
        return offers.accept(user.id(), id, offerId);
    }

    @Operation(summary = "Participant · mes réservations")
    @GetMapping("/bookings")
    public List<Booking> bookings(@AuthenticationPrincipal AuthenticatedUser user) {
        return bookings.mine(user);
    }

    @Operation(summary = "Participant · détail de réservation")
    @GetMapping("/bookings/{id}")
    public Booking detail(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        return bookings.detail(user, id);
    }

    @Operation(summary = "Prestataire affecté · avancer la prestation")
    @PatchMapping("/bookings/{id}/status")
    public Booking progress(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id, @Valid @RequestBody Progress body) {
        return bookings.transition(user, id, body.status());
    }

    @Operation(summary = "Client propriétaire · confirmer la fin")
    @PostMapping("/bookings/{id}/confirm")
    public Booking confirm(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        return bookings.transition(user, id, BookingStatus.COMPLETED);
    }

    @Operation(summary = "Client propriétaire · noter une réservation confirmée")
    @PostMapping("/bookings/{id}/review")
    @ResponseStatus(HttpStatus.CREATED)
    public Review review(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id, @Valid @RequestBody Feedback body) {
        return bookings.review(user, id, body);
    }

    @Operation(summary = "Public · avis visibles du prestataire")
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @GetMapping("/providers/{id}/reviews")
    public PublicReviews providerReviews(@PathVariable String id) {
        profiles.getPublic(id);
        return PublicReviews.from(reviews.findByProviderIdAndHiddenFalseOrderByCreatedAtDesc(id));
    }

    @Operation(summary = "Public · avis visibles du service")
    @io.swagger.v3.oas.annotations.security.SecurityRequirements
    @GetMapping("/services/{id}/reviews")
    public PublicReviews serviceReviews(@PathVariable String id) {
        catalogue.getById(id, null);
        return PublicReviews.from(reviews.findByServiceIdAndHiddenFalseOrderByCreatedAtDesc(id));
    }

    @Operation(summary = "Mes notifications paginées")
    @GetMapping("/notifications")
    public PageResponse<Notification> notifications(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @RequestParam(defaultValue="0") @Min(0) int page) {
        return notifications.list(user.id(), page);
    }

    @Operation(summary = "Nombre de notifications non lues")
    @GetMapping("/notifications/unread")
    public long unread(@AuthenticationPrincipal AuthenticatedUser user) {
        return notifications.unread(user.id());
    }

    @Operation(summary = "Marquer ma notification comme lue")
    @PatchMapping("/notifications/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        notifications.read(user.id(), id);
    }
}
