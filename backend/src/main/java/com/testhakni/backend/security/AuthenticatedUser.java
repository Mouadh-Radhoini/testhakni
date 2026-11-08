package com.testhakni.backend.security;

import com.testhakni.backend.model.Role;

/**
 * Utilisateur courant place dans le SecurityContext par le filtre JWT.
 * Dans un controleur : {@code @AuthenticationPrincipal AuthenticatedUser me}.
 */
public record AuthenticatedUser(String id, String email, Role role) {
}
