package com.testhakni.backend.identity.infrastructure.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Securite de l'API : stateless + JWT. Matrice d'autorisation par role. AUTH-06 / AUTH-08
 *
 * Les opérations du parcours de réservation sont limitées aux participants.
 * Les contrôleurs utilisent les identités JWT et les services vérifient la propriété.
 * Les routes /api/admin/** exigent toujours le rôle ADMIN.
 * L'ordre des regles compte : la premiere qui correspond s'applique.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter,
                                                   JsonSecurityHandlers handlers) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(handlers).accessDeniedHandler(handlers))
            .authorizeHttpRequests(auth -> auth
                // Technique / documentation
                .requestMatchers("/error", "/api/health", "/actuator/health", "/actuator/info",
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Authentification
                .requestMatchers("/api/auth/**").permitAll()
                // Prestataire : son profil et ses services
                .requestMatchers("/api/providers/me/**").hasRole("PROVIDER")
                .requestMatchers(HttpMethod.GET, "/api/services/mine").hasRole("PROVIDER")
                .requestMatchers(HttpMethod.POST, "/api/services/**").hasRole("PROVIDER")
                .requestMatchers(HttpMethod.PUT, "/api/services/**").hasRole("PROVIDER")
                .requestMatchers(HttpMethod.PATCH, "/api/services/**").hasRole("PROVIDER")
                .requestMatchers(HttpMethod.DELETE, "/api/services/**").hasRole("PROVIDER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/requests/*/offers").hasRole("PROVIDER")
                .requestMatchers("/api/bookings/*/status").hasRole("PROVIDER")
                .requestMatchers("/api/bookings/*/confirm", "/api/bookings/*/review").hasRole("CUSTOMER")
                .requestMatchers("/api/bookings/**").hasAnyRole("CUSTOMER", "PROVIDER")
                .requestMatchers(HttpMethod.GET, "/api/providers/*/reviews").permitAll()
                // Catalogue public
                .requestMatchers(HttpMethod.GET, "/api/services/**", "/api/categories", "/api/providers/*").permitAll()
                // Client : ses demandes
                .requestMatchers("/api/requests/**").hasRole("CUSTOMER")
                // Tout le reste (ex. /api/users/me) : connecte, quel que soit le role
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(b -> b.disable())
            .formLogin(f -> f.disable());
        return http.build();
    }

    /** Empeche Spring Boot d'enregistrer le filtre JWT une 2e fois hors de la chaine de securite. */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    /** BCrypt : hachage a sens unique avec sel aleatoire. AUTH-07 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
