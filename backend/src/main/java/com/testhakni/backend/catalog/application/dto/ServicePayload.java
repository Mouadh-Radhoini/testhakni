package com.testhakni.backend.catalog.application.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.testhakni.backend.catalog.domain.model.PriceType;
import com.testhakni.backend.catalog.domain.model.ServiceCategory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Creation / modification d'un service par un prestataire. SERV-02 / SERV-03 */
public record ServicePayload(
        @NotBlank(message = "Le titre est obligatoire")
        @Size(min = 3, max = 100, message = "Le titre doit faire entre 3 et 100 caracteres")
        String title,

        @NotBlank(message = "La description est obligatoire")
        @Size(min = 10, max = 2000, message = "La description doit faire entre 10 et 2000 caracteres")
        String description,

        @NotNull(message = "La categorie est obligatoire")
        ServiceCategory category,

        @NotBlank(message = "La ville est obligatoire")
        @Size(max = 80)
        String city,

        @DecimalMin(value = "0.0", message = "Le prix doit etre positif")
        @DecimalMax(value = "100000", message = "Prix trop eleve")
        @Digits(integer = 6, fraction = 2, message = "2 decimales maximum")
        BigDecimal price,

        @NotNull(message = "Le type de prix est obligatoire")
        PriceType priceType) {

    /** Un prix est obligatoire sauf pour un service "sur devis". */
    @JsonIgnore
    @AssertTrue(message = "Le prix est obligatoire sauf pour un service sur devis")
    public boolean isPriceProvided() {
        return priceType == null || priceType == PriceType.ON_QUOTE || price != null;
    }
}
