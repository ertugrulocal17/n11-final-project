package com.n11bootcamp.order.integration;

import java.math.BigDecimal;
import java.util.List;

public record CartSnapshot(List<CartLineSnapshot> lines, BigDecimal subtotal, int totalQuantity) {

    public record CartLineSnapshot(
            Long productId,
            String sku,
            String name,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal) {}
}
