package com.testhakni.backend.identity.application.port;

import java.time.Instant;
import java.util.Optional;

import com.testhakni.backend.identity.domain.model.User;

/** Port de sortie : emission et verification des tokens (implemente par JwtService). */
public interface TokenProvider {

    /** Refresh token emis : la valeur a renvoyer au client et ce qu'il faut enregistrer cote serveur. */
    record IssuedRefreshToken(String value, String tokenId, String familyId, Instant expiresAt) {
    }

    /** Contenu verifie d'un refresh token (signature, emetteur, audience, type et expiration valides). */
    record RefreshClaims(String tokenId, String userId, String familyId) {
    }

    String generateAccessToken(User user);

    /** Nouveau refresh token rattache a la chaine de rotation {@code familyId}. */
    IssuedRefreshToken generateRefreshToken(User user, String familyId);

    long accessTokenTtlSeconds();

    Optional<RefreshClaims> verifyRefreshToken(String refreshToken);
}
