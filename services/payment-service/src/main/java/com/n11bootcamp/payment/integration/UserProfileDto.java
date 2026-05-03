package com.n11bootcamp.payment.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserProfileDto(Long id, String email, String role) {}
