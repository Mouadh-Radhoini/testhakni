package com.testhakni.backend.identity.infrastructure.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Limites anti force brute (app.security.rate-limit.*). SEC-04
 *
 * @param enabled         permet de desactiver la limitation (jamais en production)
 * @param login           requetes POST /api/auth/login par adresse IP
 * @param register        inscriptions par adresse IP
 * @param refresh         renouvellements de token par adresse IP
 * @param failedLogins    echecs de connexion tolérés par compte (email) avant blocage temporaire
 */
@ConfigurationProperties(prefix = "app.security.rate-limit")
public record RateLimitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue Rule login,
        @DefaultValue Rule register,
        @DefaultValue Rule refresh,
        @DefaultValue Rule failedLogins) {

    /** Au plus {@code capacity} requetes par fenetre glissante {@code period}. */
    public record Rule(@DefaultValue("10") int capacity, @DefaultValue("1m") Duration period) {
    }
}
