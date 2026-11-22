package com.testhakni.backend.identity.infrastructure.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

/**
 * Ensemble de "token buckets" Bucket4j, un par cle (adresse IP, email...), en memoire. SEC-04
 * Adapte a une instance unique ; pour plusieurs instances, Bucket4j sait stocker les buckets
 * dans Redis ou une base partagee sans changer ce code d'appel.
 */
class RateLimiter {

    /** Garde-fou memoire : au-dela, les compteurs sont reinitialises. */
    private static final int MAX_KEYS = 100_000;

    private final RateLimitProperties.Rule rule;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    RateLimiter(RateLimitProperties.Rule rule) {
        this.rule = rule;
    }

    /** Consomme un jeton ; le resultat indique s'il restait du credit et combien de temps attendre sinon. */
    ConsumptionProbe tryConsume(String key) {
        return bucket(key).tryConsumeAndReturnRemaining(1);
    }

    boolean isExhausted(String key) {
        Bucket bucket = buckets.get(key);
        return bucket != null && bucket.getAvailableTokens() <= 0;
    }

    void reset(String key) {
        buckets.remove(key);
    }

    private Bucket bucket(String key) {
        if (buckets.size() > MAX_KEYS) {
            buckets.clear();
        }
        return buckets.computeIfAbsent(key, k -> Bucket.builder()
                .addLimit(limit -> limit.capacity(rule.capacity()).refillGreedy(rule.capacity(), rule.period()))
                .build());
    }
}
