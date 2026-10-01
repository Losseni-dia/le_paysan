package shop.le_paysan.backend.auth;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import shop.le_paysan.backend.user.User;

/**
 * Emission/validation des access et refresh tokens JWT.
 * MVP stateless : pas de table de revocation (cf. docs/04-architecture-technique.md).
 */
@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private static final String TYPE_PASSWORD_RESET = "password_reset";
    private static final String CLAIM_ROLES = "roles";
    private static final long PASSWORD_RESET_EXPIRATION_MS = 30 * 60 * 1000L;

    private final Key signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
            @Value("${app.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.signingKey = resolveKey(secret);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    private static Key resolveKey(String secret) {
        byte[] bytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            // Secret de dev trop court pour HS256 : on le derive a une longueur suffisante.
            bytes = java.util.Arrays.copyOf(bytes, 32);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getRoles().stream().map(shop.le_paysan.backend.user.Role::getName).toList();
        return buildToken(user.getUsername(), Map.of(CLAIM_TYPE, TYPE_ACCESS, CLAIM_ROLES, roles), accessTokenExpirationMs);
    }

    public String generateRefreshToken(User user) {
        return buildToken(user.getUsername(), Map.of(CLAIM_TYPE, TYPE_REFRESH), refreshTokenExpirationMs);
    }

    public String generatePasswordResetToken(User user) {
        return buildToken(user.getUsername(), Map.of(CLAIM_TYPE, TYPE_PASSWORD_RESET), PASSWORD_RESET_EXPIRATION_MS);
    }

    public boolean isPasswordResetToken(String token) {
        return TYPE_PASSWORD_RESET.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    private String buildToken(String subject, Map<String, Object> claims, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith((SecretKey) signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(parseClaims(token).get(CLAIM_TYPE, String.class));
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith((SecretKey) signingKey).build().parseSignedClaims(token).getPayload();
    }
}
