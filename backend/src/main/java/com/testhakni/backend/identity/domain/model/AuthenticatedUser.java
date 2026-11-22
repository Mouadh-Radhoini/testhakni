package com.testhakni.backend.identity.domain.model;

import com.testhakni.backend.identity.domain.model.Role;

/**
 * Utilisateur courant place dans le SecurityContext par le filtre JWT.
 * Dans un controleur : {@code @AuthenticationPrincipal AuthenticatedUser me}.
 */
public record AuthenticatedUser(String id, String email, Role role) {
}
