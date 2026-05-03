package com.n11bootcamp.config;

import com.n11bootcamp.cart.CommerceServicesProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(CommerceServicesProperties properties) {
        return RestClient.builder().baseUrl(properties.productBaseUrl()).build();
    }
}
