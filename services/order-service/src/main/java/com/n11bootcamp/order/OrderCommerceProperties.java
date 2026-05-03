package com.n11bootcamp.order;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commerce.services")
public record OrderCommerceProperties(String cartBaseUrl, String productBaseUrl) {}

