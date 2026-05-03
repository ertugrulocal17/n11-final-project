package com.n11bootcamp.common.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
@ConfigurationProperties(prefix = "commerce.internal")
@Validated
public record InternalServiceProperties(String serviceToken) {

    public boolean matchesHeader(String headerValue) {
        return serviceToken != null
                && !serviceToken.isBlank()
                && serviceToken.equals(headerValue);
    }
}
