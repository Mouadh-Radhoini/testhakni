package com.testhakni.backend.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Base64;

import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

/** AUTH-05 / SEC-05 : signature, expiration, type, emetteur, audience et contenu des tokens. */
class JwtServiceTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("unit-test-secret-unit-test-secret-1234".getBytes());

    private final User user = User.builder().id("u1").email("a@mail.com").role(Role.PROVIDER).build();

    private static JwtService jwt(Duration accessTtl) {
        return new JwtService(JwtProperties.of(SECRET, accessTtl, Duration.ofHours(1)));
    }

    @Test
    void accessTokenContainsIdentityRoleAndStandardClaimsButNoEmail() {
        Claims claims = jwt(Duration.ofMinutes(5))
                .parse(jwt(Duration.ofMinutes(5)).generateAccessToken(user), JwtService.TYPE_ACCESS).orElseThrow();
        assertThat(claims.getSubject()).isEqualTo("u1");
        assertThat(claims.get(JwtService.CLAIM_ROLE, String.class)).isEqualTo("PROVIDER");
        assertThat(claims.getIssuer()).isEqualTo("testhakni-backend");
        assertThat(claims.getAudience()).containsExactly("testhakni-web");
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims).doesNotContainKey("email");
    }

    @Test
    void eachRefreshTokenHasItsOwnIdAndKeepsItsFamily() {
        JwtService jwt = jwt(Duration.ofMinutes(5));
        var first = jwt.generateRefreshToken(user, "fam-1");
        var second = jwt.generateRefreshToken(user, "fam-1");
        assertThat(first.tokenId()).isNotEqualTo(second.tokenId());

        var claims = jwt.verifyRefreshToken(first.value()).orElseThrow();
        assertThat(claims.tokenId()).isEqualTo(first.tokenId());
        assertThat(claims.userId()).isEqualTo("u1");
        assertThat(claims.familyId()).isEqualTo("fam-1");
    }

    @Test
    void tokenTypeIsChecked() {
        JwtService jwt = jwt(Duration.ofMinutes(5));
        assertThat(jwt.parse(jwt.generateRefreshToken(user, "f").value(), JwtService.TYPE_ACCESS)).isEmpty();
        assertThat(jwt.parse(jwt.generateAccessToken(user), JwtService.TYPE_REFRESH)).isEmpty();
        assertThat(jwt.verifyRefreshToken(jwt.generateAccessToken(user))).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = jwt(Duration.ofMinutes(-5)); // au-dela de la tolerance d'horloge
        assertThat(jwt.parse(jwt.generateAccessToken(user), JwtService.TYPE_ACCESS)).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String otherSecret = Base64.getEncoder().encodeToString("another-secret-another-secret-987654".getBytes());
        JwtService attacker = new JwtService(JwtProperties.of(otherSecret, Duration.ofMinutes(5), Duration.ofHours(1)));
        assertThat(jwt(Duration.ofMinutes(5)).parse(attacker.generateAccessToken(user), JwtService.TYPE_ACCESS)).isEmpty();
    }

    @Test
    void tokenForAnotherIssuerOrAudienceIsRejected() {
        JwtService server = jwt(Duration.ofMinutes(5));
        JwtService otherIssuer = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1),
                "autre-serveur", "testhakni-web"));
        JwtService otherAudience = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(5), Duration.ofHours(1),
                "testhakni-backend", "autre-client"));
        assertThat(server.parse(otherIssuer.generateAccessToken(user), JwtService.TYPE_ACCESS)).isEmpty();
        assertThat(server.parse(otherAudience.generateAccessToken(user), JwtService.TYPE_ACCESS)).isEmpty();
    }
}
