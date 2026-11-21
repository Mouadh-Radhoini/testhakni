package com.testhakni.backend.identity.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.identity.infrastructure.security.JwtService;
import com.testhakni.backend.support.IntegrationTest;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

/** Tests API : AUTH-02 a AUTH-09. */
class AuthControllerTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void registerCustomerCreatesAccountAndHashesPassword() throws Exception {
        mockMvc.perform(postJson("/api/auth/register/customer", Map.of(
                        "email", "Alice@Mail.com", "password", PASSWORD, "firstName", "Alice", "lastName", "B"), null))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("alice@mail.com"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.user.password").doesNotExist());

        User saved = userRepository.findByEmail("alice@mail.com").orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        registerCustomer("dup@mail.com");
        mockMvc.perform(postJson("/api/auth/register/customer", Map.of(
                        "email", "DUP@mail.com", "password", PASSWORD, "firstName", "A", "lastName", "B"), null))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("existe deja")));
    }

    @Test
    void weakPasswordAndInvalidEmailReturnFieldErrors() throws Exception {
        mockMvc.perform(postJson("/api/auth/register/customer", Map.of(
                        "email", "pas-un-email", "password", "abc", "firstName", "A", "lastName", "B"), null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void providerRegistrationRequiresProviderFieldsAndCreatesProfile() throws Exception {
        mockMvc.perform(postJson("/api/auth/register/provider", Map.of(
                        "email", "p@mail.com", "password", PASSWORD, "firstName", "P", "lastName", "B"), null))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.businessName").exists())
                .andExpect(jsonPath("$.fieldErrors.phone").exists())
                .andExpect(jsonPath("$.fieldErrors.categories").exists());

        JsonNode auth = registerProvider("p@mail.com");
        assertThat(auth.get("user").get("role").asText()).isEqualTo("PROVIDER");
        mockMvc.perform(withToken(get("/api/providers/me/profile"), token(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Presta p@mail.com"))
                .andExpect(jsonPath("$.categories[0]").value("PLOMBERIE"));
    }

    @Test
    void loginReturnsJwtWithRoleClaim() throws Exception {
        registerProvider("login@mail.com");
        String res = mockMvc.perform(postJson("/api/auth/login",
                        Map.of("email", "LOGIN@mail.com", "password", PASSWORD), null))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Claims claims = jwtService.parse(readJson(res).get("accessToken").asText(), JwtService.TYPE_ACCESS).orElseThrow();
        assertThat(claims.get("role", String.class)).isEqualTo("PROVIDER");
        assertThat(claims).doesNotContainKey("email"); // SEC-05 : pas de donnee personnelle dans le token
        assertThat(claims.getIssuer()).isEqualTo("testhakni-backend");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void badCredentialsReturn401() throws Exception {
        registerCustomer("bad@mail.com");
        mockMvc.perform(postJson("/api/auth/login", Map.of("email", "bad@mail.com", "password", "Wrong123!"), null))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Email ou mot de passe incorrect"));
        mockMvc.perform(postJson("/api/auth/login", Map.of("email", "nobody@mail.com", "password", PASSWORD), null))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshTokenGivesNewTokensButAccessTokenIsRefused() throws Exception {
        JsonNode auth = registerCustomer("refresh@mail.com");
        mockMvc.perform(postJson("/api/auth/refresh", Map.of("refreshToken", auth.get("refreshToken").asText()), null))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
        mockMvc.perform(postJson("/api/auth/refresh", Map.of("refreshToken", token(auth)), null))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointNeedsValidToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentification requise"));
        mockMvc.perform(withToken(get("/api/users/me"), "faux.token.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token invalide ou expire"));
    }

    @Test
    void userCanReadAndUpdateOwnProfileOnly() throws Exception {
        JsonNode auth = registerCustomer("me@mail.com");
        mockMvc.perform(withToken(get("/api/users/me"), token(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@mail.com"));

        mockMvc.perform(withToken(put("/api/users/me"), token(auth)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("firstName", "Nouveau", "lastName", "Nom", "city", "Sfax",
                                "role", "ADMIN", "email", "hack@mail.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Nouveau"))
                .andExpect(jsonPath("$.city").value("Sfax"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.email").value("me@mail.com"));
    }

    @Test
    void changePasswordEnforcesPolicyAndCurrentPassword() throws Exception {
        String t = token(registerCustomer("pwd@mail.com"));
        mockMvc.perform(withToken(put("/api/users/me/password"), t).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", PASSWORD, "newPassword", "faible"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(withToken(put("/api/users/me/password"), t).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", "Mauvais1!", "newPassword", "Nouveau123!"))))
                .andExpect(status().isBadRequest());
        mockMvc.perform(withToken(put("/api/users/me/password"), t).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", PASSWORD, "newPassword", "Nouveau123!"))))
                .andExpect(status().isNoContent());
        mockMvc.perform(postJson("/api/auth/login", Map.of("email", "pwd@mail.com", "password", "Nouveau123!"), null))
                .andExpect(status().isOk());
    }

    @Test
    void customerCannotAccessProviderRoutes() throws Exception {
        String t = token(registerCustomer("c@mail.com"));
        mockMvc.perform(withToken(get("/api/providers/me/profile"), t))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mockMvc.perform(postJson("/api/services", Map.of("title", "x"), t))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------ SEC-03 rotation / revocation

    private String refresh(String refreshToken, int expectedStatus) throws Exception {
        return mockMvc.perform(postJson("/api/auth/refresh", Map.of("refreshToken", refreshToken), null))
                .andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
    }

    @Test
    void refreshTokenIsSingleUseAndReplayRevokesTheSession() throws Exception {
        String first = registerCustomer("rotation@mail.com").get("refreshToken").asText();
        String second = readJson(refresh(first, 200)).get("refreshToken").asText();
        assertThat(second).isNotEqualTo(first);

        refresh(first, 401);   // rejeu de l'ancien token : vol suppose
        refresh(second, 401);  // toute la chaine est revoquee, meme le token le plus recent
    }

    @Test
    void logoutRevokesTheRefreshToken() throws Exception {
        String refreshToken = registerCustomer("logout@mail.com").get("refreshToken").asText();
        mockMvc.perform(postJson("/api/auth/logout", Map.of("refreshToken", refreshToken), null))
                .andExpect(status().isNoContent());
        refresh(refreshToken, 401);
        // Idempotent : une deuxieme deconnexion ne renvoie pas d'erreur
        mockMvc.perform(postJson("/api/auth/logout", Map.of("refreshToken", refreshToken), null))
                .andExpect(status().isNoContent());
    }

    @Test
    void changingPasswordClosesOtherSessions() throws Exception {
        JsonNode auth = registerCustomer("sessions@mail.com");
        mockMvc.perform(withToken(put("/api/users/me/password"), token(auth)).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", PASSWORD, "newPassword", "Nouveau123!"))))
                .andExpect(status().isNoContent());
        refresh(auth.get("refreshToken").asText(), 401);
    }

    // ------------------------------------------------------------ SEC-06 en-tetes

    @Test
    void responsesCarrySecurityHeaders() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Permissions-Policy"));
    }
}
