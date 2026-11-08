package com.testhakni.backend.dto;

import com.testhakni.backend.model.ServiceStatus;
import jakarta.validation.constraints.NotNull;

/** Activer / desactiver un service (ACTIVE ou INACTIVE uniquement). SERV-03 */
public record ServiceStatusRequest(@NotNull(message = "Le statut est obligatoire") ServiceStatus status) {
}
