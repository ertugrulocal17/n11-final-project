package com.n11bootcamp.order.dto;

import java.math.BigDecimal;

public record OrderLineResponse(
        Long productId,
        String productSku,
        String productName,
        String imageUrl,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {}