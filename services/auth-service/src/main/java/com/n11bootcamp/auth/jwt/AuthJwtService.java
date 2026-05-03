package com.n11bootcamp.auth.jwt;

import com.n11bootcamp.platform.jwt.JwtTokenSupport;
import com.n11bootcamp.user.domain.AppUser;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthJwtService {

    private final JwtTokenSupport jwtTokenSupport;

    public AuthJwtService(JwtTokenSupport jwtTokenSupport) {
        this.jwtTokenSupport = jwtTokenSupport;
    }

    public String createToken(AppUser user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtTokenSupport.properties().expirationMs());
        return Jwts.builder()
                .subject(user.getEmail().toLowerCase())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(exp)
                .signWith(jwtTokenSupport.signKey())
                .compact();
    }
}

