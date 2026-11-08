package com.testhakni.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Inscription client. AUTH-02 */
public record RegisterCustomerRequest(
        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        @Size(max = 120)
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Pattern(regexp = ValidationRules.PASSWORD_REGEX, message = ValidationRules.PASSWORD_MESSAGE)
        String password,

        @NotBlank(message = "Le prenom est obligatoire")
        @Size(max = 50)
        String firstName,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 50)
        String lastName,

        @Pattern(regexp = ValidationRules.PHONE_REGEX, message = ValidationRules.PHONE_MESSAGE)
        String phone,

        @Size(max = 80)
        String city) {
}
