package com.n11bootcamp.order.dto;


import com.n11bootcamp.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDetailResponse(
        Long id, OrderStatus status, BigDecimal totalAmount, Instant createdAt, List<OrderLineResponse> lines) {}
