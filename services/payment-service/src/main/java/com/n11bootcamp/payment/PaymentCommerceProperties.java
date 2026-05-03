package com.n11bootcamp.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commerce.services")
public record PaymentCommerceProperties(String orderBaseUrl, String authBaseUrl) {}
