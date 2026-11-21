package com.testhakni.backend.identity.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Trace serveur d'un refresh token emis (SEC-03). Le JWT reste la preuve signee ;
 * ce document permet de le revoquer, de le faire tourner et de detecter sa reutilisation.
 *
 * @param id        identifiant unique du token (claim "jti")
 * @param familyId  chaine de rotation issue d'une meme connexion : revoquee d'un bloc si un token est rejoue
 * @param expiresAt index TTL : MongoDB purge le document une fois le token expire
 * @param revokedAt null tant que le token est utilisable
 */
@Document("refresh_tokens")
public record RefreshToken(
        @Id String id,
        @Indexed String userId,
        @Indexed String familyId,
        Instant createdAt,
        @Indexed(expireAfter = "0s") Instant expiresAt,
        Instant revokedAt) {

    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
