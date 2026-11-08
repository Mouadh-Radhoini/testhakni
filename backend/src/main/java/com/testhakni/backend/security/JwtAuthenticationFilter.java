package com.testhakni.backend.security;

import java.io.IOException;
import java.util.List;

import com.testhakni.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lit "Authorization: Bearer <token>", verifie le JWT et place l'utilisateur
 * dans le SecurityContext. Un token absent/invalide laisse la requete anonyme :
 * les regles de SecurityConfig renverront alors 401 sur les routes protegees. AUTH-06
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String INVALID_TOKEN_ATTR = "jwt.invalid";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            jwtService.parse(token, JwtService.TYPE_ACCESS)
                    .map(jwtService::toPrincipal)
                    // Le compte doit toujours exister et etre actif (ex. suspendu entre-temps)
                    .filter(p -> userRepository.findById(p.id()).map(u -> u.isActive()).orElse(false))
                    .ifPresentOrElse(principal -> {
                        var auth = new UsernamePasswordAuthenticationToken(principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name())));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }, () -> request.setAttribute(INVALID_TOKEN_ATTR, Boolean.TRUE));
        }
        chain.doFilter(request, response);
    }
}
