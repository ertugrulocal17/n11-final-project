package com.n11bootcamp.order.integration;

import com.n11bootcamp.common.exception.ProductNotFoundException;
import com.n11bootcamp.common.internal.InternalServiceAuthFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Component
public class OrderFulfillmentClient {

    private final RestClient cartRestClient;
    private final RestClient productRestClient;

    @Value("${commerce.internal.service-token}")
    private String internalServiceToken;

    public OrderFulfillmentClient(
            @Qualifier("cartRestClient") RestClient cartRestClient,
            @Qualifier("productRestClient") RestClient productRestClient) {
        this.cartRestClient = cartRestClient;
        this.productRestClient = productRestClient;
    }

    public CartSnapshot getCart(String authorizationHeaderValue) {
        try {
            CartSnapshot body = cartRestClient
                    .get()
                    .uri("/api/v1/cart")
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeaderValue)
                    .retrieve()
                    .body(CartSnapshot.class);
            if (body == null) {
                throw new IllegalStateException("Empty cart response");
            }
            return body;
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("Cart service error: " + e.getStatusCode(), e);
        }
    }

    public void clearCart(Long userId) {
        cartRestClient
                .delete()
                .uri("/internal/v1/users/{userId}/cart", userId)
                .header(InternalServiceAuthFilter.HEADER, internalServiceToken)
                .retrieve()
                .toBodilessEntity();
    }

    public ProductSnapshot getProduct(Long productId) {
        try {
            ProductSnapshot body = productRestClient
                    .get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(ProductSnapshot.class);
            if (body == null) {
                throw new ProductNotFoundException(productId);
            }
            return body;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                throw new ProductNotFoundException(productId);
            }
            throw e;
        }
    }

    public void adjustStock(Long productId, int delta) {
        productRestClient
                .patch()
                .uri("/internal/v1/products/{id}/stock", productId)
                .header(InternalServiceAuthFilter.HEADER, internalServiceToken)
                .body(Map.of("delta", delta))
                .retrieve()
                .toBodilessEntity();
    }
}

