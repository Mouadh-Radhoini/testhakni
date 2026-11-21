package com.testhakni.backend.request.domain.model;

/**
 * Cycle de vie d'une demande client.
 * OPEN -> IN_PROGRESS (offre acceptee, Sprint 3) -> COMPLETED ; ou OPEN -> CANCELLED.
 */
public enum RequestStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
