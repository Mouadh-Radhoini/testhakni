package com.testhakni.backend.model;

/**
 * Cycle de vie d'un service.
 * ACTIVE : visible des clients. INACTIVE : masque par le prestataire.
 * SUSPENDED : masque par un admin (moderation, Sprint 3).
 */
public enum ServiceStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
