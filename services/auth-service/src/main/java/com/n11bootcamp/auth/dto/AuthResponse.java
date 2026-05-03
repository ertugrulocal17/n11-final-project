package com.n11bootcamp.auth.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {}
