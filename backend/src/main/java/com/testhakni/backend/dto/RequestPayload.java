package com.testhakni.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.testhakni.backend.model.ServiceCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Creation d'une demande de service par un client. REQ-02 */
public record RequestPayload(
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

        @Size(max = 200)
        String address,

        @FutureOrPresent(message = "La date souhaitee ne peut pas etre dans le passe")
        LocalDate preferredDate,

        @DecimalMin(value = "0.0", message = "Le budget doit etre positif")
        @DecimalMax(value = "100000", message = "Budget trop eleve")
        BigDecimal budget,

        /** Optionnel : service consulte a l'origine de la demande. */
        @Size(max = 64)
        String serviceId) {
}
