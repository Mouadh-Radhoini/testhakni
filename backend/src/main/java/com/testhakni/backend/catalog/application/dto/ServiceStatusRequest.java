package com.testhakni.backend.catalog.application.dto;

import com.testhakni.backend.catalog.domain.model.ServiceStatus;
import jakarta.validation.constraints.NotNull;

/** Activer / desactiver un service (ACTIVE ou INACTIVE uniquement). SERV-03 */
public record ServiceStatusRequest(@NotNull(message = "Le statut est obligatoire") ServiceStatus status) {
}
