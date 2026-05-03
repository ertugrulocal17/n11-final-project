package com.n11bootcamp.cart.client;

import com.n11bootcamp.common.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
public class ProductCatalogClient {

    private final RestClient productRestClient;

    public ProductCatalogClient(RestClient productRestClient) {
        this.productRestClient = productRestClient;
    }

    public record ProductDto(
            Long id,
            String sku,
            String name,
            String description,
            String imageUrl,
            BigDecimal price,
            Integer stockQuantity) {}

    public ProductDto getProduct(Long productId) {
        try {
            ProductDto body = productRestClient
                    .get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(ProductDto.class);
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
}
