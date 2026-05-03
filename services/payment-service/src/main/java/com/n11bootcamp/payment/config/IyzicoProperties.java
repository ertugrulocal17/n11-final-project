package com.n11bootcamp.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.iyzico")
public record IyzicoProperties(
        boolean enabled,
        String apiKey,
        String secretKey,
        String baseUrl,
        String callbackUrl,
        String testBuyerIdentityNumber
) {
    public IyzicoProperties {
        apiKey = apiKey != null ? apiKey : "";
        secretKey = secretKey != null ? secretKey : "";
        baseUrl = (baseUrl == null || baseUrl.isBlank()) ? "https://sandbox-api.iyzipay.com" : baseUrl;
        callbackUrl = callbackUrl != null ? callbackUrl : "";
        testBuyerIdentityNumber =
                (testBuyerIdentityNumber == null || testBuyerIdentityNumber.isBlank())
                        ? "74300864791"
                        : testBuyerIdentityNumber;
    }

    public boolean hasCredentials() {
        return !apiKey.isBlank() && !secretKey.isBlank();
    }
}
