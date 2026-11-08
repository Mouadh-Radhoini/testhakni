package com.testhakni.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** AUTH-04 */
public record LoginRequest(
        @NotBlank(message = "L'email est obligatoire") String email,
        @NotBlank(message = "Le mot de passe est obligatoire") String password) {
}
