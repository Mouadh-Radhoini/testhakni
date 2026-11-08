package com.testhakni.backend.security;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testhakni.backend.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/** Reponses JSON standardisees pour 401 (non authentifie) et 403 (role insuffisant). AUTH-06 / AUTH-08 */
@Component
public class JsonSecurityHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonSecurityHandlers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        String message = Boolean.TRUE.equals(request.getAttribute(JwtAuthenticationFilter.INVALID_TOKEN_ATTR))
                ? "Token invalide ou expire"
                : "Authentification requise";
        write(response, request, HttpStatus.UNAUTHORIZED, message);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        write(response, request, HttpStatus.FORBIDDEN, "Acces refuse pour votre role");
    }

    private void write(HttpServletResponse response, HttpServletRequest request, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(),
                ApiError.of(status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}
