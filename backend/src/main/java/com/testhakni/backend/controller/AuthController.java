package com.testhakni.backend.controller;

import com.testhakni.backend.dto.AuthResponse;
import com.testhakni.backend.dto.LoginRequest;
import com.testhakni.backend.dto.RefreshTokenRequest;
import com.testhakni.backend.dto.RegisterCustomerRequest;
import com.testhakni.backend.dto.RegisterProviderRequest;
import com.testhakni.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Inscription, connexion, renouvellement du token")
@SecurityRequirements // endpoints publics : pas de cadenas dans Swagger
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/customer")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscription client (AUTH-02)")
    @ApiResponse(responseCode = "201", description = "Compte cree, tokens renvoyes")
    @ApiResponse(responseCode = "400", description = "Donnees invalides (mot de passe faible, email invalide...)")
    @ApiResponse(responseCode = "409", description = "Email deja utilise")
    public AuthResponse registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
        return authService.registerCustomer(request);
    }

    @PostMapping("/register/provider")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inscription prestataire (AUTH-03)",
            description = "Cree le compte avec le role PROVIDER et son profil professionnel initial")
    @ApiResponse(responseCode = "201", description = "Compte cree, tokens renvoyes")
    @ApiResponse(responseCode = "400", description = "Donnees invalides ou champs prestataire manquants")
    @ApiResponse(responseCode = "409", description = "Email deja utilise")
    public AuthResponse registerProvider(@Valid @RequestBody RegisterProviderRequest request) {
        return authService.registerProvider(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion (AUTH-04)")
    @ApiResponse(responseCode = "200", description = "Connexion reussie, tokens renvoyes")
    @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect")
    @ApiResponse(responseCode = "403", description = "Compte suspendu")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Nouveau couple de tokens a partir du refresh token (AUTH-05)")
    @ApiResponse(responseCode = "200", description = "Tokens renouveles")
    @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expire")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request.refreshToken());
    }
}
