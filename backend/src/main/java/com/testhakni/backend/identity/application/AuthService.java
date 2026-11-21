package com.testhakni.backend.identity.application;

import com.testhakni.backend.identity.application.dto.AuthResponse;
import com.testhakni.backend.identity.application.dto.LoginRequest;
import com.testhakni.backend.identity.application.dto.RegisterCustomerRequest;
import com.testhakni.backend.identity.application.dto.RegisterProviderRequest;
import com.testhakni.backend.identity.application.dto.UserResponse;
import com.testhakni.backend.identity.application.port.PasswordHasher;
import com.testhakni.backend.identity.application.port.TokenProvider;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.provider.domain.model.ProviderProfile;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.shared.domain.Text;
import com.testhakni.backend.shared.domain.exception.DomainException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Cas d'usage : inscription, connexion et renouvellement de token. AUTH-02 a AUTH-05 */
@Service
@Slf4j
public class AuthService {

    static final String BAD_CREDENTIALS = "Email ou mot de passe incorrect";

    private final UserRepository userRepository;
    private final ProviderProfileRepository profileRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, ProviderProfileRepository profileRepository,
                       PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
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
        return issueTokens(saveNewUser(user));
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
        return issueTokens(saved);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(Text.normalizeEmail(req.email()))
                .filter(u -> passwordHasher.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> DomainException.unauthorized(BAD_CREDENTIALS));
        if (!user.isActive()) {
            throw DomainException.forbidden("Compte suspendu. Contactez l'administrateur.");
        }
        log.info("security event=login actor={} role={}", user.getId(), user.getRole());
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        User user = tokenProvider.refreshTokenSubject(refreshToken)
                .flatMap(userRepository::findById)
                .filter(User::isActive)
                .orElseThrow(() -> DomainException.unauthorized("Refresh token invalide ou expire"));
        return issueTokens(user);
    }

    private User saveNewUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw DomainException.conflict("Un compte existe deja avec cet email");
        }
        return userRepository.save(user);
    }

    private AuthResponse issueTokens(User user) {
        return AuthResponse.bearer(
                tokenProvider.generateAccessToken(user),
                tokenProvider.generateRefreshToken(user),
                tokenProvider.accessTokenTtlSeconds(),
                UserResponse.from(user));
    }
}
