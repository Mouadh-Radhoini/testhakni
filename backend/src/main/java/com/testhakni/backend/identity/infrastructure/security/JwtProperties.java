package com.testhakni.backend.identity.infrastructure.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parametres JWT lus depuis app.jwt.* (application.yml / variables d'environnement). AUTH-05 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {
}
