package com.testhakni.backend.identity.infrastructure.security;

import com.testhakni.backend.identity.application.port.LoginThrottle;
import com.testhakni.backend.shared.domain.exception.DomainException;
import org.springframework.stereotype.Component;

/**
 * Adaptateur Bucket4j du port {@link LoginThrottle} : chaque echec consomme un jeton du compte,
 * une connexion reussie remet le compteur a zero. SEC-04
 * Le message ne dit pas si le compte existe, pour ne pas aider l'enumeration des emails.
 */
@Component
public class BucketLoginThrottle implements LoginThrottle {

    private final boolean enabled;
    private final RateLimiter failures;

    public BucketLoginThrottle(RateLimitProperties props) {
        this.enabled = props.enabled();
        this.failures = new RateLimiter(props.failedLogins());
    }

    @Override
    public void checkAllowed(String email) {
        if (enabled && failures.isExhausted(email)) {
            throw DomainException.tooManyRequests(
                    "Trop de tentatives de connexion pour ce compte. Réessayez dans quelques minutes.");
        }
    }

    @Override
    public void recordFailure(String email) {
        if (enabled) failures.tryConsume(email);
    }

    @Override
    public void recordSuccess(String email) {
        failures.reset(email);
    }
}
