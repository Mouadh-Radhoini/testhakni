package com.testhakni.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Base64;

import com.testhakni.backend.model.Role;
import com.testhakni.backend.model.User;
import org.junit.jupiter.api.Test;

/** AUTH-05 : signature, expiration et type de token. */
class JwtServiceTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("unit-test-secret-unit-test-secret-1234".getBytes());

    private final User user = User.builder().id("u1").email("a@mail.com").role(Role.PROVIDER).build();

    @Test
    void accessTokenContainsIdentityAndRole() {
        JwtService jwt = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        String token = jwt.generateAccessToken(user);

        AuthenticatedUser principal = jwt.toPrincipal(jwt.parse(token, JwtService.TYPE_ACCESS).orElseThrow());
        assertThat(principal).isEqualTo(new AuthenticatedUser("u1", "a@mail.com", Role.PROVIDER));
    }

    @Test
    void tokenTypeIsChecked() {
        JwtService jwt = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        assertThat(jwt.parse(jwt.generateRefreshToken(user), JwtService.TYPE_ACCESS)).isEmpty();
        assertThat(jwt.parse(jwt.generateAccessToken(user), JwtService.TYPE_REFRESH)).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(new JwtProperties(SECRET, Duration.ofSeconds(-10), Duration.ofHours(1)));
        assertThat(jwt.parse(jwt.generateAccessToken(user), JwtService.TYPE_ACCESS)).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String otherSecret = Base64.getEncoder().encodeToString("another-secret-another-secret-987654".getBytes());
        JwtService attacker = new JwtService(new JwtProperties(otherSecret, Duration.ofMinutes(5), Duration.ofHours(1)));
        JwtService server = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1)));
        assertThat(server.parse(attacker.generateAccessToken(user), JwtService.TYPE_ACCESS)).isEmpty();
    }
}
