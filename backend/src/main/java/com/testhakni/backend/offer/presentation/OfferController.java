package com.testhakni.backend.offer.presentation;

import java.util.List;

import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.offer.application.OfferService;
import com.testhakni.backend.offer.application.dto.OpenRequest;
import com.testhakni.backend.offer.application.dto.Proposal;
import com.testhakni.backend.offer.domain.model.Offer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Parcours de réservation")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService offers;

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
}
