package com.n11bootcamp.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        String imageUrl,
        BigDecimal price,
        Integer stockQuantity
) {
}
