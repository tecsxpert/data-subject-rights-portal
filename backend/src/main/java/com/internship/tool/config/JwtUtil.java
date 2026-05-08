package com.internship.tool.config;

import com.internship.tool.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiry-ms}")
    private long expiryMs;

    private SecretKey key() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        // Ensure key is at least 32 bytes (256 bits) for HS256
        if (bytes.length < 32) {
            bytes = Arrays.copyOf(bytes, 32);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    /** Generate a signed JWT for the given user. */
    public String generate(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role",   user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(key())
                .compact();
    }

    /** Extract all claims from a token. Throws on invalid/expired token. */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        return parse(token).getSubject();
    }

    /** Extract user ID from a UserPrincipal (avoids extra DB lookup). */
    public Long getUserId(UserDetails principal) {
        if (principal instanceof UserPrincipal up) {
            return up.getId();
        }
        return null;
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }
}
