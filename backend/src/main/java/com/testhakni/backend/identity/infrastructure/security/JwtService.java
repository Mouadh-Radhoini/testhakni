package com.testhakni.backend.identity.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import com.testhakni.backend.identity.domain.model.Role;
import com.testhakni.backend.identity.application.port.TokenProvider;
import com.testhakni.backend.identity.domain.model.AuthenticatedUser;
import com.testhakni.backend.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

/**
 * Creation et verification des JWT (HMAC-SHA256). AUTH-05
 * - access token : courte duree, envoye dans "Authorization: Bearer ..."
 * - refresh token : longue duree, sert uniquement a obtenir un nouvel access token
 */
@Service
public class JwtService implements TokenProvider {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_EMAIL = "email";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
    }

    @Override
    public String generateAccessToken(User user) {
        return generate(user, TYPE_ACCESS, props.accessTokenTtl());
    }

    @Override
    public String generateRefreshToken(User user) {
        return generate(user, TYPE_REFRESH, props.refreshTokenTtl());
    }

    @Override
    public long accessTokenTtlSeconds() {
        return props.accessTokenTtl().toSeconds();
    }

    /** Renvoie les claims si la signature est valide, le token non expire et du bon type. */
    public Optional<Claims> parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            if (!expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> refreshTokenSubject(String refreshToken) {
        return parse(refreshToken, TYPE_REFRESH).map(Claims::getSubject);
    }

    public AuthenticatedUser toPrincipal(Claims claims) {
        return new AuthenticatedUser(
                claims.getSubject(),
                claims.get(CLAIM_EMAIL, String.class),
                Role.valueOf(claims.get(CLAIM_ROLE, String.class)));
    }

    private String generate(User user, String type, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId())
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }
}
