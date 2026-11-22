package com.testhakni.backend.identity.infrastructure.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parametres JWT lus depuis app.jwt.* (application.yml / variables d'environnement). AUTH-05, SEC-05
 *
 * @param issuer   claim "iss" : identifie le serveur emetteur, verifie a la lecture
 * @param audience claim "aud" : identifie le client destinataire, verifie a la lecture
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl,
                            @DefaultValue("testhakni-backend") String issuer,
                            @DefaultValue("testhakni-web") String audience) {

    /** Fabrique pratique (tests) : emetteur et audience par defaut. */
    public static JwtProperties of(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {
        return new JwtProperties(secret, accessTokenTtl, refreshTokenTtl, "testhakni-backend", "testhakni-web");
    }
}
