package com.testhakni.backend.identity.application;

import java.time.Instant;
import java.util.UUID;

import com.testhakni.backend.identity.application.dto.AuthResponse;
import com.testhakni.backend.identity.application.dto.LoginRequest;
import com.testhakni.backend.identity.application.dto.RegisterCustomerRequest;
import com.testhakni.backend.identity.application.dto.RegisterProviderRequest;
import com.testhakni.backend.identity.application.dto.UserResponse;
import com.testhakni.backend.identity.application.port.LoginThrottle;
import com.testhakni.backend.identity.application.port.PasswordHasher;
import com.testhakni.backend.identity.application.port.TokenProvider;
import com.testhakni.backend.identity.application.port.TokenProvider.IssuedRefreshToken;
import com.testhakni.backend.identity.application.port.TokenProvider.RefreshClaims;
import com.testhakni.backend.identity.domain.model.RefreshToken;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.RefreshTokenRepository;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.shared.domain.Text;
import com.testhakni.backend.shared.domain.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Cas d'usage : inscription, connexion, renouvellement et deconnexion. AUTH-02 a AUTH-05, SEC-03, SEC-04
 *
 * Rotation des refresh tokens : chaque refresh consomme le token presente et en emet un nouveau
 * dans la meme chaine. Rejouer un token deja consomme revele un vol probable : toute la chaine
 * est alors revoquee et l'utilisateur doit se reconnecter.
 */
@Service
@Slf4j
public class AuthService {

    static final String BAD_CREDENTIALS = "Email ou mot de passe incorrect";
    static final String INVALID_REFRESH = "Refresh token invalide ou expire";

    private final UserRepository userRepository;
    private final ProviderProfileRepository profileRepository;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final LoginThrottle loginThrottle;

    public AuthService(UserRepository userRepository, ProviderProfileRepository profileRepository,
                       RefreshTokenRepository refreshTokens, PasswordHasher passwordHasher,
                       TokenProvider tokenProvider, LoginThrottle loginThrottle) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.refreshTokens = refreshTokens;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
        this.loginThrottle = loginThrottle;
    }

    public AuthResponse registerCustomer(RegisterCustomerRequest req) {
        User user = User.builder()
                .email(Text.normalizeEmail(req.email()))
                .passwordHash(passwordHasher.hash(req.password()))
                .firstName(req.firstName().trim())
                .lastName(req.lastName().trim())
                .phone(Text.trimToNull(req.phone()))
                .city(Text.trimToNull(req.city()))
                .role(Role.CUSTOMER)
                .build();
        return startSession(saveNewUser(user));
    }

    public AuthResponse registerProvider(RegisterProviderRequest req) {
        User user = User.builder()
                .email(Text.normalizeEmail(req.email()))
                .passwordHash(passwordHasher.hash(req.password()))
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
        return startSession(saved);
    }

    public AuthResponse login(LoginRequest req) {
        String email = Text.normalizeEmail(req.email());
        loginThrottle.checkAllowed(email);
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordHasher.matches(req.password(), u.getPasswordHash()))
                .orElse(null);
        if (user == null) {
            loginThrottle.recordFailure(email);
            log.warn("security event=login_failed");
            throw DomainException.unauthorized(BAD_CREDENTIALS);
        }
        if (!user.isActive()) {
            throw DomainException.forbidden("Compte suspendu. Contactez l'administrateur.");
        }
        loginThrottle.recordSuccess(email);
        log.info("security event=login actor={} role={}", user.getId(), user.getRole());
        return startSession(user);
    }

    public AuthResponse refresh(String refreshToken) {
        RefreshClaims claims = tokenProvider.verifyRefreshToken(refreshToken)
                .orElseThrow(() -> DomainException.unauthorized(INVALID_REFRESH));
        Instant now = Instant.now();
        if (!refreshTokens.consume(claims.tokenId(), now)) {
            if (refreshTokens.findById(claims.tokenId()).isPresent()) {
                // Token authentique mais deja utilise ou revoque : rejeu, on coupe toute la chaine.
                refreshTokens.revokeFamily(claims.familyId(), now);
                log.warn("security event=refresh_reuse actor={} family={}", claims.userId(), claims.familyId());
            }
            throw DomainException.unauthorized(INVALID_REFRESH);
        }
        User user = userRepository.findById(claims.userId())
                .filter(User::isActive)
                .orElseThrow(() -> DomainException.unauthorized(INVALID_REFRESH));
        return issueTokens(user, claims.familyId());
    }

    /** Deconnexion : revoque la chaine du refresh token presente. Idempotent, sans erreur si deja invalide. */
    public void logout(String refreshToken) {
        tokenProvider.verifyRefreshToken(refreshToken).ifPresent(claims -> {
            refreshTokens.revokeFamily(claims.familyId(), Instant.now());
            log.info("security event=logout actor={}", claims.userId());
        });
    }

    private User saveNewUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw DomainException.conflict("Un compte existe deja avec cet email");
        }
        return userRepository.save(user);
    }

    /** Nouvelle connexion : nouvelle chaine de rotation. */
    private AuthResponse startSession(User user) {
        return issueTokens(user, UUID.randomUUID().toString());
    }

    private AuthResponse issueTokens(User user, String familyId) {
        IssuedRefreshToken refresh = tokenProvider.generateRefreshToken(user, familyId);
        refreshTokens.save(new RefreshToken(refresh.tokenId(), user.getId(), familyId, Instant.now(),
                refresh.expiresAt(), null));
        return AuthResponse.bearer(
                tokenProvider.generateAccessToken(user),
                refresh.value(),
                tokenProvider.accessTokenTtlSeconds(),
                UserResponse.from(user));
    }
}
