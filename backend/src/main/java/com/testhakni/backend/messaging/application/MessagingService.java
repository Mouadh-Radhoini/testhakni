package com.testhakni.backend.messaging.application;

import java.time.Instant;
import java.util.List;

import com.testhakni.backend.booking.domain.repository.BookingRepository;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.messaging.application.dto.MessagingViews.ConversationView;
import com.testhakni.backend.messaging.application.dto.MessagingViews.MessageView;
import com.testhakni.backend.messaging.application.port.MessagePublisher;
import com.testhakni.backend.messaging.domain.model.Conversation;
import com.testhakni.backend.messaging.domain.model.Message;
import com.testhakni.backend.messaging.domain.repository.ConversationRepository;
import com.testhakni.backend.messaging.domain.repository.MessageRepository;
import com.testhakni.backend.offer.domain.repository.OfferRepository;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.request.domain.repository.ServiceRequestRepository;
import com.testhakni.backend.shared.application.MediaUrls;
import com.testhakni.backend.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Cas d'usage : messagerie entre un client et un prestataire.
 * Un fil existe pour chaque couple demande / prestataire des qu'une offre a ete envoyee ;
 * seuls ces deux participants peuvent le lire ou y ecrire (SEC-02).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagingService {

    static final int PAGE_SIZE = 50;

    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final OfferRepository offers;
    private final ServiceRequestRepository requests;
    private final BookingRepository bookings;
    private final UserRepository users;
    private final ProviderProfileRepository profiles;
    private final MessagePublisher publisher;

    /** Ouvre (ou retrouve) le fil lie a une offre : le prestataire auteur ou le client de la demande. */
    public ConversationView openForOffer(AuthenticatedUser user, String offerId) {
        var offer = offers.findById(offerId).orElseThrow(() -> DomainException.notFound("Offre introuvable"));
        var request = requests.findById(offer.requestId())
                .orElseThrow(() -> DomainException.notFound("Demande introuvable"));
        if (!user.id().equals(offer.providerId()) && !user.id().equals(request.getCustomerId())) {
            throw DomainException.forbidden("Conversation réservée au client et au prestataire");
        }
        return view(user.id(), conversations.openIfAbsent(Conversation.open(request.getId(), request.getCustomerId(),
                offer.providerId(), request.getTitle(), Instant.now())));
    }

    /** Ouvre (ou retrouve) le fil d'une reservation, pour l'un de ses deux participants. */
    public ConversationView openForBooking(AuthenticatedUser user, String bookingId) {
        var booking = bookings.findById(bookingId).orElseThrow(() -> DomainException.notFound("Réservation introuvable"));
        if (!user.id().equals(booking.customerId()) && !user.id().equals(booking.providerId())) {
            throw DomainException.forbidden("Conversation réservée au client et au prestataire");
        }
        return view(user.id(), conversations.openIfAbsent(Conversation.open(booking.requestId(), booking.customerId(),
                booking.providerId(), booking.title(), Instant.now())));
    }

    public List<ConversationView> list(AuthenticatedUser user) {
        return conversations.findByParticipant(user.id()).stream().map(c -> view(user.id(), c)).toList();
    }

    public ConversationView get(AuthenticatedUser user, String conversationId) {
        return view(user.id(), owned(user, conversationId));
    }

    public List<MessageView> messages(AuthenticatedUser user, String conversationId, Instant before) {
        owned(user, conversationId);
        return messages.findLatest(conversationId, before, PAGE_SIZE).stream().map(MessageView::from).toList();
    }

    public MessageView send(AuthenticatedUser user, String conversationId, String body) {
        Conversation conversation = owned(user, conversationId);
        String text = body == null ? "" : body.strip();
        if (text.isEmpty()) throw DomainException.badRequest("Le message est vide");
        if (text.length() > Message.MAX_LENGTH) throw DomainException.badRequest("2000 caracteres maximum");

        Instant now = Instant.ofEpochMilli(System.currentTimeMillis()); // precision MongoDB
        Message saved = messages.insert(new Message(null, conversationId, user.id(), text, now));
        conversations.recordMessage(conversationId, saved.preview(), now);
        conversations.markRead(conversation, user.id(), now);

        MessageView view = MessageView.from(saved);
        try {
            publisher.deliver(List.of(conversation.customerId(), conversation.providerId()), view);
        } catch (RuntimeException e) {
            // Le message est enregistre : un echec du temps reel ne doit pas faire echouer l'envoi
            log.warn("messaging event=push_failed conversation={}", conversationId);
        }
        return view;
    }

    public void markRead(AuthenticatedUser user, String conversationId) {
        conversations.markRead(owned(user, conversationId), user.id(), Instant.now());
    }

    public long unreadTotal(AuthenticatedUser user) {
        return conversations.findByParticipant(user.id()).stream()
                .mapToLong(c -> messages.countUnread(c.id(), user.id(), c.readAtOf(user.id())))
                .sum();
    }

    /** SEC-02 : 404 si le fil n'existe pas, 403 si l'utilisateur n'y participe pas. */
    private Conversation owned(AuthenticatedUser user, String conversationId) {
        Conversation c = conversations.findById(conversationId)
                .orElseThrow(() -> DomainException.notFound("Conversation introuvable"));
        if (!c.hasParticipant(user.id())) throw DomainException.forbidden("Conversation privée");
        return c;
    }

    private ConversationView view(String me, Conversation c) {
        String otherId = c.otherParticipant(me);
        User other = users.findById(otherId).orElse(null);
        String name = otherId.equals(c.providerId())
                ? profiles.findByUserId(otherId).map(ProviderProfile::getBusinessName)
                        .orElse(other == null ? "Prestataire" : other.displayName())
                : other == null ? "Client" : other.displayName();
        return new ConversationView(c.id(), c.requestId(), c.title(), otherId, name,
                other == null ? null : MediaUrls.of(other.getAvatarId()),
                c.lastMessagePreview(), c.lastMessageAt(),
                messages.countUnread(c.id(), me, c.readAtOf(me)));
    }
}
