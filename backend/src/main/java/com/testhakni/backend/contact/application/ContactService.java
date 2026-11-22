package com.testhakni.backend.contact.application;

import java.time.Instant;
import java.util.List;

import com.testhakni.backend.contact.domain.model.Contact;
import com.testhakni.backend.contact.domain.model.ContactStatus;
import com.testhakni.backend.contact.domain.repository.ContactRepository;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.notification.application.NotificationService;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.shared.application.MediaUrls;
import com.testhakni.backend.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Cas d'usage : contacts entre clients et prestataires.
 * Demande -> acceptation (ou refus) ; un contact accepte permet de discuter sans demande de travail.
 */
@Service
@RequiredArgsConstructor
public class ContactService {

    /** Un lien tel que le voit l'utilisateur connecte. */
    public record ContactView(String id, String otherId, String otherName, String otherAvatarUrl, Role otherRole,
                              ContactStatus status, boolean incoming, Instant createdAt) {
    }

    /** Relation avec un utilisateur donne, pour afficher le bon bouton sur son profil. */
    public record ContactState(String contactId, String state) {
        static final ContactState NONE = new ContactState(null, "NONE");
    }

    private final ContactRepository contacts;
    private final UserRepository users;
    private final ProviderProfileRepository profiles;
    private final NotificationService notifications;

    public ContactView request(AuthenticatedUser me, String targetId) {
        if (me.id().equals(targetId)) throw DomainException.badRequest("Vous ne pouvez pas vous ajouter vous-même");
        User target = users.findById(targetId).filter(User::isActive)
                .orElseThrow(() -> DomainException.notFound("Utilisateur introuvable"));
        boolean pair = (me.role() == Role.CUSTOMER && target.getRole() == Role.PROVIDER)
                || (me.role() == Role.PROVIDER && target.getRole() == Role.CUSTOMER);
        if (!pair) throw DomainException.badRequest("Les contacts relient un client et un prestataire");

        Instant now = Instant.now();
        Contact existing = contacts.findByPair(me.id(), targetId).orElse(null);
        Contact saved;
        if (existing == null) {
            saved = contacts.save(Contact.request(me.id(), targetId, now));
            notifications.send(targetId, "CONTACT_REQUEST", saved.id(), "Nouvelle demande de contact");
        } else if (existing.isAccepted()
                || (existing.status() == ContactStatus.PENDING && existing.requesterId().equals(me.id()))) {
            saved = existing; // deja en contact ou deja demande : idempotent
        } else if (existing.status() == ContactStatus.PENDING) {
            saved = accept(existing, now); // l'autre m'avait deja demande : demande croisee = acceptation
        } else {
            // Ancienne demande refusee : nouvelle demande, dans mon sens
            saved = contacts.save(new Contact(existing.id(), existing.pairKey(), me.id(), targetId,
                    ContactStatus.PENDING, now, null));
            notifications.send(targetId, "CONTACT_REQUEST", saved.id(), "Nouvelle demande de contact");
        }
        return view(me.id(), saved);
    }

    public ContactView accept(AuthenticatedUser me, String contactId) {
        Contact c = incomingPending(me, contactId);
        return view(me.id(), accept(c, Instant.now()));
    }

    public ContactView decline(AuthenticatedUser me, String contactId) {
        Contact c = incomingPending(me, contactId);
        return view(me.id(), contacts.save(new Contact(c.id(), c.pairKey(), c.requesterId(), c.targetId(),
                ContactStatus.DECLINED, c.createdAt(), Instant.now())));
    }

    /** Annuler sa demande ou retirer un contact (l'un ou l'autre participant). */
    public void remove(AuthenticatedUser me, String contactId) {
        contacts.delete(owned(me, contactId));
    }

    /** Liens visibles : contacts acceptes et demandes en attente (les refus ne sont montres qu'au demandeur). */
    public List<ContactView> list(AuthenticatedUser me) {
        return contacts.findByUser(me.id()).stream()
                .filter(c -> c.status() != ContactStatus.DECLINED || c.requesterId().equals(me.id()))
                .map(c -> view(me.id(), c)).toList();
    }

    public long incomingPending(AuthenticatedUser me) {
        return contacts.countIncomingPending(me.id());
    }

    /** NONE, PENDING_OUT, PENDING_IN, ACCEPTED ou DECLINED, vu par l'utilisateur connecte. */
    public ContactState stateWith(AuthenticatedUser me, String otherId) {
        return contacts.findByPair(me.id(), otherId).map(c -> new ContactState(c.id(), switch (c.status()) {
            case ACCEPTED -> "ACCEPTED";
            case PENDING -> c.requesterId().equals(me.id()) ? "PENDING_OUT" : "PENDING_IN";
            case DECLINED -> c.requesterId().equals(me.id()) ? "DECLINED" : "NONE";
        })).orElse(ContactState.NONE);
    }

    /** Utilise par la messagerie : discussion libre seulement entre contacts acceptes. */
    public boolean areConnected(String a, String b) {
        return contacts.findByPair(a, b).map(Contact::isAccepted).orElse(false);
    }

    private Contact accept(Contact c, Instant now) {
        Contact accepted = contacts.save(new Contact(c.id(), c.pairKey(), c.requesterId(), c.targetId(),
                ContactStatus.ACCEPTED, c.createdAt(), now));
        notifications.send(c.requesterId(), "CONTACT_ACCEPTED", c.id(), "Votre demande de contact a été acceptée");
        return accepted;
    }

    private Contact owned(AuthenticatedUser me, String contactId) {
        Contact c = contacts.findById(contactId).orElseThrow(() -> DomainException.notFound("Contact introuvable"));
        if (!c.involves(me.id())) throw DomainException.forbidden("Contact privé");
        return c;
    }

    private Contact incomingPending(AuthenticatedUser me, String contactId) {
        Contact c = owned(me, contactId);
        if (!c.targetId().equals(me.id())) throw DomainException.forbidden("Seul le destinataire peut répondre");
        if (c.status() != ContactStatus.PENDING) throw DomainException.conflict("Demande déjà traitée");
        return c;
    }

    private ContactView view(String me, Contact c) {
        String otherId = c.other(me);
        User other = users.findById(otherId).orElse(null);
        String name = other == null ? "?" : other.getRole() == Role.PROVIDER
                ? profiles.findByUserId(otherId).map(ProviderProfile::getBusinessName).orElse(other.displayName())
                : other.displayName();
        return new ContactView(c.id(), otherId, name, other == null ? null : MediaUrls.of(other.getAvatarId()),
                other == null ? null : other.getRole(), c.status(), c.targetId().equals(me), c.createdAt());
    }
}
