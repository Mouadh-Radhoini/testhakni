package com.testhakni.backend.identity.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** AUTH-07 : la politique de mot de passe s'applique aussi a la modification. */
public record ChangePasswordRequest(
        @NotBlank(message = "Le mot de passe actuel est obligatoire") String currentPassword,
        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Pattern(regexp = ValidationRules.PASSWORD_REGEX, message = ValidationRules.PASSWORD_MESSAGE)
        String newPassword) {
}
