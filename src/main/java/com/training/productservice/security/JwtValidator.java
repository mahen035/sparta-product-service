package com.training.productservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Validates JWTs issued by the API Gateway. This service only ever verifies
 * tokens - it never signs one - so it needs just the shared secret, not the
 * full issuance machinery that lives in api-gateway.
 */
@Component
public class JwtValidator {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";

    private final SecretKey key;

    public JwtValidator(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @throws JwtException if the token is malformed, expired, fails signature
     *                       verification, or isn't an access token.
     */
    public JwtPrincipal validate(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE))) {
            throw new io.jsonwebtoken.security.SecurityException("Not an access token");
        }
        return new JwtPrincipal(
                Long.valueOf(claims.getSubject()),
                String.valueOf(claims.get(CLAIM_EMAIL)),
                String.valueOf(claims.get(CLAIM_NAME)),
                String.valueOf(claims.get(CLAIM_ROLE)));
    }
}
