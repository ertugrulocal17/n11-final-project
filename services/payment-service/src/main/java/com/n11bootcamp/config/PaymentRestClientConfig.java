package com.n11bootcamp.config;


import com.n11bootcamp.payment.PaymentCommerceProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PaymentRestClientConfig {

    @Bean
    @Qualifier("orderRestClient")
    public RestClient orderRestClient(PaymentCommerceProperties properties) {
        return RestClient.builder().baseUrl(properties.orderBaseUrl()).build();
    }

    @Bean
    @Qualifier("authRestClient")
    public RestClient authRestClient(PaymentCommerceProperties properties) {
        return RestClient.builder().baseUrl(properties.authBaseUrl()).build();
    }
}
