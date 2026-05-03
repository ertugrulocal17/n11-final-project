package com.n11bootcamp.platform.jwt;

import com.n11bootcamp.platform.security.UserPrincipal;
import com.n11bootcamp.user.domain.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class JwtTokenSupport {

    private final JwtProperties properties;
    private final SecretKey signKey;

    public JwtTokenSupport(JwtProperties properties) {
        this.properties = properties;
        this.signKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public UserPrincipal parsePrincipal(String token) {
        Claims c = parseClaims(token);
        Number uidRaw = c.get("uid", Number.class);
        Long uid = uidRaw != null ? uidRaw.longValue() : null;
        String roleStr = c.get("role", String.class);
        Role role = Role.valueOf(roleStr != null ? roleStr : "USER");
        return UserPrincipal.fromJwtClaims(uid, c.getSubject(), role);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public JwtProperties properties() {
        return properties;
    }

    public SecretKey signKey() {
        return signKey;
    }
}
