package com.testhakni.backend.identity.application.port;

/**
 * Port de sortie : freine la force brute sur un compte precis (SEC-04), en plus de la limite par IP.
 * Les echecs sont comptes par email ; au-dela du seuil, les tentatives sont refusees temporairement.
 */
public interface LoginThrottle {

    /** Leve une erreur 429 si ce compte a recu trop d'echecs recemment. */
    void checkAllowed(String email);

    void recordFailure(String email);

    void recordSuccess(String email);
}
