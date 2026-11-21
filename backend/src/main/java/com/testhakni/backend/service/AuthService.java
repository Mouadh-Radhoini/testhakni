package com.testhakni.backend.service;

import java.util.Locale;

import com.testhakni.backend.dto.AuthResponse;
import com.testhakni.backend.dto.LoginRequest;
import com.testhakni.backend.dto.RegisterCustomerRequest;
import com.testhakni.backend.dto.RegisterProviderRequest;
import com.testhakni.backend.dto.UserResponse;
import com.testhakni.backend.exception.ApiException;
import com.testhakni.backend.model.ProviderProfile;
import com.testhakni.backend.model.Role;
import com.testhakni.backend.model.User;
import com.testhakni.backend.repository.ProviderProfileRepository;
import com.testhakni.backend.repository.UserRepository;
import com.testhakni.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Inscription, connexion et renouvellement de token. AUTH-02 a AUTH-05 */
@Service
@lombok.extern.slf4j.Slf4j
public class AuthService {

    static final String BAD_CREDENTIALS = "Email ou mot de passe incorrect";

    private final UserRepository userRepository;
    private final ProviderProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, ProviderProfileRepository profileRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse registerCustomer(RegisterCustomerRequest req) {
        User user = User.builder()
                .email(normalizeEmail(req.email()))
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName().trim())
                .lastName(req.lastName().trim())
                .phone(trimToNull(req.phone()))
                .city(trimToNull(req.city()))
                .role(Role.CUSTOMER)
                .build();
        return issueTokens(saveNewUser(user));
    }

    public AuthResponse registerProvider(RegisterProviderRequest req) {
        User user = User.builder()
                .email(normalizeEmail(req.email()))
                .passwordHash(passwordEncoder.encode(req.password()))
                .firstName(req.firstName().trim())
                .lastName(req.lastName().trim())
                .phone(req.phone().trim())
                .city(req.city().trim())
                .role(Role.PROVIDER)
                .build();
        User saved = saveNewUser(user);

        // Profil professionnel initial, complete ensuite via PUT /api/providers/me/profile
        ProviderProfile profile = ProviderProfile.builder()
                .userId(saved.getId())
                .businessName(req.businessName().trim())
                .categories(req.categories().stream().distinct().toList())
                .city(req.city().trim())
                .build();
        try {
            profileRepository.save(profile);
        } catch (RuntimeException e) {
            userRepository.delete(saved); // pas de compte prestataire sans profil
            throw e;
        }
        return issueTokens(saved);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(normalizeEmail(req.email()))
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized(BAD_CREDENTIALS));
        if (!user.isActive()) {
            throw ApiException.forbidden("Compte suspendu. Contactez l'administrateur.");
        }
        log.info("security event=login actor={} role={}", user.getId(), user.getRole());
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        User user = jwtService.parse(refreshToken, JwtService.TYPE_REFRESH)
                .flatMap(claims -> userRepository.findById(claims.getSubject()))
                .filter(User::isActive)
                .orElseThrow(() -> ApiException.unauthorized("Refresh token invalide ou expire"));
        return issueTokens(user);
    }

    private User saveNewUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw ApiException.conflict("Un compte existe deja avec cet email");
        }
        return userRepository.save(user);
    }

    private AuthResponse issueTokens(User user) {
        return AuthResponse.bearer(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                jwtService.accessTokenTtlSeconds(),
                UserResponse.from(user));
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
