package com.testhakni.backend.messaging.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Fil de discussion entre un client et un prestataire :
 * - lie a une demande (requestId) des qu'une offre existe, et il continue pendant la reservation ;
 * - ou direct (requestId null) entre deux contacts acceptes, sans demande de travail.
 * Un seul fil par triplet demande / client / prestataire.
 *
 * @param customerReadAt derniere lecture par le client (calcul des non-lus)
 * @param providerReadAt derniere lecture par le prestataire
 */
@Document("conversations")
@CompoundIndex(name = "request_pair_unique", def = "{'requestId': 1, 'customerId': 1, 'providerId': 1}", unique = true)
public record Conversation(
        @Id String id,
        String requestId,
        @Indexed String customerId,
        @Indexed String providerId,
        String title,
        Instant createdAt,
        Instant lastMessageAt,
        String lastMessagePreview,
        Instant customerReadAt,
        Instant providerReadAt) {

    public boolean hasParticipant(String userId) {
        return customerId.equals(userId) || providerId.equals(userId);
    }

    public String otherParticipant(String userId) {
        return customerId.equals(userId) ? providerId : customerId;
    }

    /** Derniere lecture de ce participant (null s'il n'a jamais ouvert le fil). */
    public Instant readAtOf(String userId) {
        return customerId.equals(userId) ? customerReadAt : providerReadAt;
    }

    public static Conversation open(String requestId, String customerId, String providerId, String title, Instant now) {
        return new Conversation(null, requestId, customerId, providerId, title, now, null, null, null, null);
    }
}
