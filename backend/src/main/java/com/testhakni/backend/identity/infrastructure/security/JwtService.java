package com.testhakni.backend.identity.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import com.testhakni.backend.identity.application.port.TokenProvider;
import com.testhakni.backend.identity.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

/**
 * Creation et verification des JWT (HMAC-SHA256). AUTH-05, SEC-05
 * - access token : courte duree, envoye dans "Authorization: Bearer ..."
 * - refresh token : longue duree, sert uniquement a obtenir un nouveau couple de tokens ;
 *   il porte un identifiant unique (jti) et sa chaine de rotation (fam), enregistres cote serveur.
 * Chaque token porte emetteur (iss), audience (aud) et identifiant unique (jti), tous verifies.
 * Aucune donnee personnelle (email) n'est placee dans le token : il n'est que signe, pas chiffre.
 */
@Service
public class JwtService implements TokenProvider {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_FAMILY = "fam";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    /** Tolerance d'horloge entre serveurs pour exp / iat. */
    private static final long CLOCK_SKEW_SECONDS = 30;

    private final SecretKey key;
    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
    }

    @Override
    public String generateAccessToken(User user) {
        return builder(user, TYPE_ACCESS, UUID.randomUUID().toString(), Instant.now(), props.accessTokenTtl())
                .claim(CLAIM_ROLE, user.getRole().name())
                .compact();
    }

    @Override
    public IssuedRefreshToken generateRefreshToken(User user, String familyId) {
        String tokenId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        String value = builder(user, TYPE_REFRESH, tokenId, now, props.refreshTokenTtl())
                .claim(CLAIM_FAMILY, familyId)
                .compact();
        return new IssuedRefreshToken(value, tokenId, familyId, now.plus(props.refreshTokenTtl()));
    }

    @Override
    public long accessTokenTtlSeconds() {
        return props.accessTokenTtl().toSeconds();
    }

    @Override
    public Optional<RefreshClaims> verifyRefreshToken(String refreshToken) {
        return parse(refreshToken, TYPE_REFRESH)
                .filter(c -> c.getId() != null && c.get(CLAIM_FAMILY, String.class) != null)
                .map(c -> new RefreshClaims(c.getId(), c.getSubject(), c.get(CLAIM_FAMILY, String.class)));
    }

    /**
     * Renvoie les claims si la signature est valide, l'emetteur et l'audience attendus,
     * le token non expire et du bon type.
     */
    public Optional<Claims> parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(props.issuer())
                    .requireAudience(props.audience())
                    .clockSkewSeconds(CLOCK_SKEW_SECONDS)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private io.jsonwebtoken.JwtBuilder builder(User user, String type, String tokenId, Instant now, Duration ttl) {
        return Jwts.builder()
                .id(tokenId)
                .issuer(props.issuer())
                .audience().add(props.audience()).and()
                .subject(user.getId())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key);
    }
}
