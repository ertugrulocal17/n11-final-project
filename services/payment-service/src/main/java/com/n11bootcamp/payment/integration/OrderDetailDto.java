package com.n11bootcamp.payment.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDetailDto(Long id, String status, BigDecimal totalAmount, List<OrderLineDto> lines) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderLineDto(
            Long productId,
            String productSku,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal) {}
}
