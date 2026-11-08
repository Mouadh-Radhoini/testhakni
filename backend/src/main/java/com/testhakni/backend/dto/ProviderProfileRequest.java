package com.testhakni.backend.dto;

import java.util.List;

import com.testhakni.backend.model.ServiceCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Creation / modification du profil professionnel. PROV-01 / PROV-02 */
public record ProviderProfileRequest(
        @NotBlank(message = "Le nom commercial est obligatoire") @Size(max = 100) String businessName,
        @Size(max = 1000, message = "1000 caracteres maximum") String bio,
        @NotEmpty(message = "Choisissez au moins une categorie") List<@NotNull ServiceCategory> categories,
        @Size(max = 20, message = "20 competences maximum") List<@NotBlank @Size(max = 50) String> skills,
        @NotBlank(message = "La ville est obligatoire") @Size(max = 80) String city,
        @Size(max = 120) String serviceArea,
        @Min(value = 0, message = "Doit etre positif") @Max(value = 60, message = "60 ans maximum") Integer yearsOfExperience,
        Boolean available) {
}
