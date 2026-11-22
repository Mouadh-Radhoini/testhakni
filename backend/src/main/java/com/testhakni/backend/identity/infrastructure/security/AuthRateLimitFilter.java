package com.testhakni.backend.identity.infrastructure.security;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testhakni.backend.shared.presentation.error.ApiError;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Limite par adresse IP les appels sensibles de /api/auth (SEC-04) : connexion, inscription, refresh.
 * Au-dela de la limite : 429 Too Many Requests, en-tete Retry-After et erreur JSON standard.
 *
 * L'adresse utilisee est celle vue par le serveur (getRemoteAddr). Derriere un reverse proxy,
 * activer server.forward-headers-strategy pour que X-Forwarded-For soit pris en compte de facon sure.
 */
@Slf4j
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final ObjectMapper objectMapper;
    private final RateLimiter login;
    private final RateLimiter register;
    private final RateLimiter refresh;

    public AuthRateLimitFilter(RateLimitProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.login = new RateLimiter(props.login());
        this.register = new RateLimiter(props.register());
        this.refresh = new RateLimiter(props.refresh());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !props.enabled() || !"POST".equals(request.getMethod()) || limiterFor(path(request)) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RateLimiter limiter = limiterFor(path(request));
        ConsumptionProbe probe = limiter.tryConsume(request.getRemoteAddr());
        if (probe.isConsumed()) {
            chain.doFilter(request, response);
            return;
        }
        long retryAfter = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));
        log.warn("security event=rate_limited path={} retryAfter={}s", path(request), retryAfter);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(429, "Too Many Requests",
                "Trop de tentatives. Réessayez dans " + retryAfter + " secondes.", request.getRequestURI()));
    }

    private RateLimiter limiterFor(String path) {
        if (path.equals("/api/auth/login")) return login;
        if (path.startsWith("/api/auth/register/")) return register;
        if (path.equals("/api/auth/refresh")) return refresh;
        return null;
    }

    private static String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
