package com.testhakni.backend.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;

import java.time.Duration;
import java.util.Base64;
import java.time.Instant;
import java.util.Optional;

import com.testhakni.backend.identity.application.dto.LoginRequest;
import com.testhakni.backend.identity.application.dto.RegisterCustomerRequest;
import com.testhakni.backend.shared.domain.exception.DomainException;
import com.testhakni.backend.identity.domain.model.AccountStatus;
import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import com.testhakni.backend.provider.domain.repository.ProviderProfileRepository;
import com.testhakni.backend.identity.application.port.LoginThrottle;
import com.testhakni.backend.identity.domain.model.RefreshToken;
import com.testhakni.backend.identity.domain.repository.RefreshTokenRepository;
import com.testhakni.backend.identity.domain.repository.UserRepository;
import com.testhakni.backend.identity.infrastructure.security.BCryptPasswordHasher;
import com.testhakni.backend.identity.infrastructure.security.JwtProperties;
import com.testhakni.backend.identity.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.testhakni.backend.shared.domain.exception.ErrorType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Tests unitaires (sans Spring ni MongoDB) de AuthService. TEST-01a */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProviderProfileRepository profileRepository;

    @Mock
    private RefreshTokenRepository refreshTokens;

    @Mock
    private LoginThrottle loginThrottle;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4); // rapide pour les tests
    private AuthService authService;
    private JwtService jwt;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString("unit-test-secret-unit-test-secret-1234".getBytes());
        jwt = new JwtService(JwtProperties.of(secret, Duration.ofMinutes(5), Duration.ofHours(1)));
        authService = new AuthService(userRepository, profileRepository, refreshTokens,
                new BCryptPasswordHasher(encoder), jwt, loginThrottle);
    }

    @Test
    void registerHashesPasswordAndNormalizesEmail() {
        when(userRepository.existsByEmail("bob@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("u1");
            return u;
        });

        var res = authService.registerCustomer(
                new RegisterCustomerRequest("  Bob@Mail.COM ", "Secret123!", "Bob", "B", null, " "));

        assertThat(res.user().email()).isEqualTo("bob@mail.com");
        assertThat(res.user().role()).isEqualTo(Role.CUSTOMER);
        assertThat(res.user().city()).isNull();
        assertThat(res.accessToken()).isNotBlank();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("dup@mail.com")).thenReturn(true);
        assertThatThrownBy(() -> authService.registerCustomer(
                new RegisterCustomerRequest("dup@mail.com", "Secret123!", "A", "B", null, null)))
                .isInstanceOf(DomainException.class)
                .extracting("type").isEqualTo(ErrorType.CONFLICT);
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() {
        User user = User.builder().id("u1").email("a@mail.com").passwordHash(encoder.encode("Secret123!"))
                .role(Role.CUSTOMER).build();
        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("a@mail.com", "Wrong123!")))
                .isInstanceOf(DomainException.class)
                .extracting("type").isEqualTo(ErrorType.UNAUTHORIZED);
    }

    @Test
    void loginOfSuspendedAccountIsForbidden() {
        User user = User.builder().id("u1").email("a@mail.com").passwordHash(encoder.encode("Secret123!"))
                .role(Role.CUSTOMER).status(AccountStatus.SUSPENDED).build();
        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("a@mail.com", "Secret123!")))
                .isInstanceOf(DomainException.class)
                .extracting("type").isEqualTo(ErrorType.FORBIDDEN);
    }

    @Test
    void refreshWithGarbageTokenIsUnauthorized() {
        assertThatThrownBy(() -> authService.refresh("n'importe.quoi"))
                .isInstanceOf(DomainException.class)
                .extracting("type").isEqualTo(ErrorType.UNAUTHORIZED);
    }

    @Test
    void registerStoresTheIssuedRefreshToken() {
        when(userRepository.existsByEmail("bob@mail.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("u1");
            return u;
        });
        var res = authService.registerCustomer(
                new RegisterCustomerRequest("bob@mail.com", "Secret123!", "Bob", "B", null, null));
        var claims = jwt.verifyRefreshToken(res.refreshToken()).orElseThrow();
        verify(refreshTokens).save(org.mockito.ArgumentMatchers.argThat((RefreshToken t) ->
                t.id().equals(claims.tokenId()) && t.userId().equals("u1") && t.revokedAt() == null));
    }

    @Test
    void refreshRotatesTokenInsideTheSameFamily() {
        User user = User.builder().id("u1").email("a@mail.com").role(Role.CUSTOMER).build();
        var old = jwt.generateRefreshToken(user, "fam-1");
        when(refreshTokens.consume(eq(old.tokenId()), any())).thenReturn(true);
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));

        var res = authService.refresh(old.value());

        var next = jwt.verifyRefreshToken(res.refreshToken()).orElseThrow();
        assertThat(next.tokenId()).isNotEqualTo(old.tokenId());
        assertThat(next.familyId()).isEqualTo("fam-1");
    }

    @Test
    void replayedRefreshTokenRevokesTheWholeFamily() {
        User user = User.builder().id("u1").email("a@mail.com").role(Role.CUSTOMER).build();
        var old = jwt.generateRefreshToken(user, "fam-1");
        when(refreshTokens.consume(eq(old.tokenId()), any())).thenReturn(false);
        when(refreshTokens.findById(old.tokenId())).thenReturn(Optional.of(
                new RefreshToken(old.tokenId(), "u1", "fam-1", Instant.now(), old.expiresAt(), Instant.now())));

        assertThatThrownBy(() -> authService.refresh(old.value()))
                .extracting("type").isEqualTo(ErrorType.UNAUTHORIZED);
        verify(refreshTokens).revokeFamily(eq("fam-1"), any());
    }

    @Test
    void logoutRevokesTheFamilyAndIgnoresGarbage() {
        User user = User.builder().id("u1").email("a@mail.com").role(Role.CUSTOMER).build();
        authService.logout(jwt.generateRefreshToken(user, "fam-9").value());
        verify(refreshTokens).revokeFamily(eq("fam-9"), any());

        authService.logout("n'importe.quoi");
        verify(refreshTokens, never()).revokeFamily(eq("n'importe.quoi"), any());
    }

    @Test
    void failedLoginIsCountedAndThrottledAccountIsRefused() {
        when(userRepository.findByEmail("a@mail.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.login(new LoginRequest("A@mail.com", "Wrong123!")))
                .extracting("type").isEqualTo(ErrorType.UNAUTHORIZED);
        verify(loginThrottle).recordFailure("a@mail.com");

        doThrow(DomainException.tooManyRequests("trop")).when(loginThrottle).checkAllowed("b@mail.com");
        assertThatThrownBy(() -> authService.login(new LoginRequest("b@mail.com", "Secret123!")))
                .extracting("type").isEqualTo(ErrorType.TOO_MANY_REQUESTS);
        verify(userRepository, never()).findByEmail("b@mail.com");
    }
}
