package com.n11bootcamp.cart;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commerce.services")
public record CommerceServicesProperties(String productBaseUrl) {}
