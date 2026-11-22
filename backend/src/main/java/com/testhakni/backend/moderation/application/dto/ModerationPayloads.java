package com.testhakni.backend.moderation.application.dto;

import com.testhakni.backend.moderation.domain.model.ContentType;
import com.testhakni.backend.moderation.domain.model.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps de requete de la moderation et des signalements. */
public final class ModerationPayloads {
    private ModerationPayloads() {
    }

    /** Signalement d'un contenu par un utilisateur connecte. */
    public record Signalement(@NotNull ContentType targetType, @NotBlank String targetId,
                              @NotBlank @Size(max = 1000) String reason) {
    }

    /** Decision d'un admin : rendre visible / actif (true) ou masquer / suspendre (false). */
    public record Decision(@NotNull Boolean enabled, @NotBlank @Size(max = 1000) String reason) {
    }

    /** Traitement d'un signalement. */
    public record Resolution(@NotNull ReportStatus status, @NotBlank @Size(max = 1000) String reason) {
    }
}
