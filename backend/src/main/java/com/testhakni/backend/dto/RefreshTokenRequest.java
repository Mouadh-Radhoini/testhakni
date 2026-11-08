package com.testhakni.backend.dto;

import jakarta.validation.constraints.NotBlank;

/** AUTH-05 */
public record RefreshTokenRequest(@NotBlank(message = "Le refresh token est obligatoire") String refreshToken) {
}
