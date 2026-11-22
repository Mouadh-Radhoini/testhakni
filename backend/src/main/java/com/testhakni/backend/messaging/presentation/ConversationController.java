package com.testhakni.backend.messaging.presentation;

import java.time.Instant;
import java.util.List;

import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.messaging.application.MessagingService;
import com.testhakni.backend.messaging.application.dto.MessagingViews.ConversationView;
import com.testhakni.backend.messaging.application.dto.MessagingViews.MessageView;
import com.testhakni.backend.messaging.application.dto.MessagingViews.SendMessage;
import com.testhakni.backend.messaging.application.dto.MessagingViews.UnreadCount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Messagerie", description = "Discussion entre un client et un prestataire (temps reel : STOMP /ws)")
@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final MessagingService messaging;

    @Operation(summary = "Mes conversations, les plus actives d'abord")
    @GetMapping
    public List<ConversationView> list(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return messaging.list(me);
    }

    @Operation(summary = "Nombre total de messages non lus")
    @GetMapping("/unread")
    public UnreadCount unread(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return new UnreadCount(messaging.unreadTotal(me));
    }

    @Operation(summary = "Ouvrir la conversation liee a une offre (client de la demande ou prestataire auteur)")
    @PostMapping("/by-offer/{offerId}")
    public ConversationView openForOffer(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                         @PathVariable String offerId) {
        return messaging.openForOffer(me, offerId);
    }

    @Operation(summary = "Ouvrir la conversation d'une reservation (participants uniquement)")
    @PostMapping("/by-booking/{bookingId}")
    public ConversationView openForBooking(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                           @PathVariable String bookingId) {
        return messaging.openForBooking(me, bookingId);
    }

    @Operation(summary = "Ouvrir une discussion directe avec un contact accepte")
    @PostMapping("/with/{userId}")
    public ConversationView openDirect(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                       @PathVariable String userId) {
        return messaging.openDirect(me, userId);
    }

    @Operation(summary = "Detail d'une conversation")
    @GetMapping("/{id}")
    public ConversationView get(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                @PathVariable String id) {
        return messaging.get(me, id);
    }

    @Operation(summary = "Messages, 50 par page ; ?before= pour remonter l'historique")
    @GetMapping("/{id}/messages")
    public List<MessageView> messages(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                                      @PathVariable String id,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before) {
        return messaging.messages(me, id, before);
    }

    @Operation(summary = "Envoyer un message (pousse en temps reel aux deux participants)")
    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageView send(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                            @PathVariable String id, @Valid @RequestBody SendMessage body) {
        return messaging.send(me, id, body.body());
    }

    @Operation(summary = "Marquer la conversation comme lue")
    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void read(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me, @PathVariable String id) {
        messaging.markRead(me, id);
    }
}
