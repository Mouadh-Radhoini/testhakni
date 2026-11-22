package com.testhakni.backend.contact.presentation;

import java.util.List;

import com.testhakni.backend.contact.application.ContactService;
import com.testhakni.backend.contact.application.ContactService.ContactState;
import com.testhakni.backend.contact.application.ContactService.ContactView;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Contacts", description = "Demandes de contact entre clients et prestataires")
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    private final ContactService contacts;

    public record ContactRequest(@NotBlank(message = "Destinataire obligatoire") String targetId) {
    }

    public record PendingCount(long incoming) {
    }

    @Operation(summary = "Mes contacts et demandes en attente")
    @GetMapping
    public List<ContactView> list(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return contacts.list(me);
    }

    @Operation(summary = "Nombre de demandes de contact recues en attente")
    @GetMapping("/pending")
    public PendingCount pending(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me) {
        return new PendingCount(contacts.incomingPending(me));
    }

    @Operation(summary = "Ma relation avec un utilisateur (NONE, PENDING_OUT, PENDING_IN, ACCEPTED, DECLINED)")
    @GetMapping("/with/{userId}")
    public ContactState state(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                              @PathVariable String userId) {
        return contacts.stateWith(me, userId);
    }

    @Operation(summary = "Envoyer une demande de contact (client vers prestataire ou l'inverse)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContactView request(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                               @Valid @RequestBody ContactRequest body) {
        return contacts.request(me, body.targetId());
    }

    @Operation(summary = "Accepter une demande recue")
    @PostMapping("/{id}/accept")
    public ContactView accept(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                              @PathVariable String id) {
        return contacts.accept(me, id);
    }

    @Operation(summary = "Refuser une demande recue")
    @PostMapping("/{id}/decline")
    public ContactView decline(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me,
                               @PathVariable String id) {
        return contacts.decline(me, id);
    }

    @Operation(summary = "Annuler ma demande ou retirer un contact")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser me, @PathVariable String id) {
        contacts.remove(me, id);
    }
}
