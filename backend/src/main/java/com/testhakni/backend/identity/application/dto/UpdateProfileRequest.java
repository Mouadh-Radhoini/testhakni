package com.testhakni.backend.identity.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Champs modifiables par l'utilisateur. Email, role et statut ne le sont pas. AUTH-09 */
public record UpdateProfileRequest(
        @NotBlank(message = "Le prenom est obligatoire") @Size(max = 50) String firstName,
        @NotBlank(message = "Le nom est obligatoire") @Size(max = 50) String lastName,
        @Pattern(regexp = ValidationRules.PHONE_REGEX, message = ValidationRules.PHONE_MESSAGE) String phone,
        @Size(max = 80) String city) {
}
