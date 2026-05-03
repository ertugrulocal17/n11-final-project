package com.n11bootcamp.cart.dto;

import java.math.BigDecimal;

public record CartLineResponse(
        Long productId,
        String sku,
        String name,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {
}
