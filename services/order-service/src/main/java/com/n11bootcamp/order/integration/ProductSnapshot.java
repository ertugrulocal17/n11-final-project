package com.n11bootcamp.order.integration;

import java.math.BigDecimal;

public record ProductSnapshot(
        Long id,
        String sku,
        String name,
        String description,
        String imageUrl,
        BigDecimal price,
        Integer stockQuantity) {}

