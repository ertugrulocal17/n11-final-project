package com.n11bootcamp.config;

import com.n11bootcamp.order.OrderCommerceProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OrderRestClientConfig {

    @Bean
    @Qualifier("cartRestClient")
    public RestClient cartRestClient(OrderCommerceProperties properties) {
        return RestClient.builder().baseUrl(properties.cartBaseUrl()).build();
    }

    @Bean
    @Qualifier("productRestClient")
    public RestClient productRestClient(OrderCommerceProperties properties) {
        return RestClient.builder().baseUrl(properties.productBaseUrl()).build();
    }
}