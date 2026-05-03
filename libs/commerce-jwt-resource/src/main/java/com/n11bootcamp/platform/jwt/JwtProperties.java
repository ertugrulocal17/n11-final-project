package com.n11bootcamp.platform.jwt;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(
        @NotBlank
        @Size(min = 32, message = "JWT secret must be at least 32 characters (256-bit for HS256)")
        String secret,
        @Min(60_000) long expirationMs
) {}