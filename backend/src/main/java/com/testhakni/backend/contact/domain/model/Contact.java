package com.testhakni.backend.contact.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Lien de contact entre un client et un prestataire, a la maniere d'un abonnement :
 * l'un envoie une demande, l'autre l'accepte ; une fois ACCEPTED, ils peuvent discuter
 * librement, meme sans demande de travail en cours.
 *
 * @param pairKey identifiant de la paire, independant du sens ("idA|idB" trie) : un seul lien par paire
 */
@Document("contacts")
public record Contact(
        @Id String id,
        @Indexed(unique = true) String pairKey,
        @Indexed String requesterId,
        @Indexed String targetId,
        ContactStatus status,
        Instant createdAt,
        Instant respondedAt) {

    public static String pairKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
    }

    public static Contact request(String requesterId, String targetId, Instant now) {
        return new Contact(null, pairKey(requesterId, targetId), requesterId, targetId, ContactStatus.PENDING, now, null);
    }

    public boolean involves(String userId) {
        return requesterId.equals(userId) || targetId.equals(userId);
    }

    public String other(String userId) {
        return requesterId.equals(userId) ? targetId : requesterId;
    }

    public boolean isAccepted() {
        return status == ContactStatus.ACCEPTED;
    }
}
