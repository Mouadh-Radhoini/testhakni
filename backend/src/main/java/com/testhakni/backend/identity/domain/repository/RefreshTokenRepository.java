package com.testhakni.backend.identity.domain.repository;

import java.time.Instant;
import java.util.Optional;

import com.testhakni.backend.identity.domain.model.RefreshToken;

/** Port du domaine : registre des refresh tokens emis (rotation, revocation). SEC-03 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findById(String id);

    /**
     * Consomme le token de facon atomique : true s'il etait encore actif et vient d'etre revoque,
     * false s'il avait deja servi (rejeu) ou n'existe pas.
     */
    boolean consume(String id, Instant now);

    /** Revoque toute une chaine de rotation (deconnexion, ou rejeu detecte). */
    void revokeFamily(String familyId, Instant now);

    /** Revoque toutes les sessions d'un compte (changement de mot de passe, suspension). */
    void revokeAllForUser(String userId, Instant now);
}
