package com.testhakni.backend.identity.application.port;

import java.util.Optional;

import com.testhakni.backend.identity.domain.model.User;

/** Port de sortie : emission et verification des tokens (implemente par JwtService). */
public interface TokenProvider {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    long accessTokenTtlSeconds();

    /** Id de l'utilisateur si le refresh token est valide, non expire et du bon type. */
    Optional<String> refreshTokenSubject(String refreshToken);
}
