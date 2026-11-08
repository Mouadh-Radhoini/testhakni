package com.testhakni.backend.dto;

import java.util.List;

import com.testhakni.backend.model.ServiceCategory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Inscription prestataire. Champs specifiques obligatoires : telephone, ville,
 * nom commercial et au moins une categorie. Cree aussi le profil professionnel. AUTH-03
 */
public record RegisterProviderRequest(
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

        @NotBlank(message = "Le telephone est obligatoire pour un prestataire")
        @Pattern(regexp = ValidationRules.PHONE_REGEX, message = ValidationRules.PHONE_MESSAGE)
        String phone,

        @NotBlank(message = "La ville est obligatoire pour un prestataire")
        @Size(max = 80)
        String city,

        @NotBlank(message = "Le nom commercial est obligatoire")
        @Size(max = 100)
        String businessName,

        @NotEmpty(message = "Choisissez au moins une categorie")
        List<@NotNull ServiceCategory> categories) {
}
