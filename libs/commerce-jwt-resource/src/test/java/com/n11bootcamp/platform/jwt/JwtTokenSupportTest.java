package com.n11bootcamp.platform.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.n11bootcamp.user.domain.Role;
import io.jsonwebtoken.Jwts;

import java.util.Date;

import org.junit.jupiter.api.Test;

class JwtTokenSupportTest {

    private static final String SECRET = "test-jwt-secret-key-at-least-32-characters-long!!";

    private final JwtTokenSupport support = new JwtTokenSupport(new JwtProperties(SECRET, 120_000L));

    @Test
    void isValid_acceptsSignedToken() {
        String token = signedToken("u@x.com", 7L, "USER");
        assertThat(support.isValid(token)).isTrue();
    }

    @Test
    void isValid_rejectsGarbage() {
        assertThat(support.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void extractEmail_readsSubject() {
        String token = signedToken("Buyer@Example.com", 1L, "USER");
        assertThat(support.extractEmail(token)).isEqualTo("buyer@example.com");
    }

    @Test
    void parsePrincipal_mapsClaims() {
        String token = signedToken("a@b.co", 42L, "ADMIN");
        var p = support.parsePrincipal(token);
        assertThat(p.getId()).isEqualTo(42L);
        assertThat(p.getEmail()).isEqualTo("a@b.co");
        assertThat(p.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void parsePrincipal_defaultsRoleToUserWhenMissing() {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 60_000);
        String token = Jwts.builder()
                .subject("only@email.com")
                .claim("uid", 3L)
                .issuedAt(now)
                .expiration(exp)
                .signWith(support.signKey())
                .compact();
        assertThat(support.parsePrincipal(token).getRole()).isEqualTo(Role.USER);
    }

    @Test
    void parsePrincipal_unknownRole_throws() {
        String token = signedToken("x@y.z", 1L, "NOT_A_ROLE");
        assertThatThrownBy(() -> support.parsePrincipal(token)).isInstanceOf(Exception.class);
    }

    private String signedToken(String email, Long uid, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + 60_000);
        return Jwts.builder()
                .subject(email.toLowerCase())
                .claim("uid", uid)
                .claim("role", role)
                .issuedAt(now)
                .expiration(exp)
                .signWith(support.signKey())
                .compact();
    }
}
