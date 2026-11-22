package com.testhakni.backend.booking.presentation;

import java.util.List;

import com.testhakni.backend.booking.application.BookingService;
import com.testhakni.backend.booking.application.dto.Progress;
import com.testhakni.backend.booking.domain.model.Booking;
import com.testhakni.backend.booking.domain.model.BookingStatus;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.review.application.dto.Feedback;
import com.testhakni.backend.review.domain.model.Review;
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
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookings;

    @Operation(summary = "Participant · mes réservations")
    @GetMapping
    public List<Booking> bookings(@AuthenticationPrincipal AuthenticatedUser user) {
        return bookings.mine(user);
    }

    @Operation(summary = "Participant · détail de réservation")
    @GetMapping("/{id}")
    public Booking detail(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        return bookings.detail(user, id);
    }

    @Operation(summary = "Prestataire affecté · avancer la prestation")
    @PatchMapping("/{id}/status")
    public Booking progress(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id, @Valid @RequestBody Progress body) {
        return bookings.transition(user, id, body.status());
    }

    @Operation(summary = "Client propriétaire · confirmer la fin")
    @PostMapping("/{id}/confirm")
    public Booking confirm(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id) {
        return bookings.transition(user, id, BookingStatus.COMPLETED);
    }

    @Operation(summary = "Client propriétaire · noter une réservation confirmée")
    @PostMapping("/{id}/review")
    @ResponseStatus(HttpStatus.CREATED)
    public Review review(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String id, @Valid @RequestBody Feedback body) {
        return bookings.review(user, id, body);
    }
}
