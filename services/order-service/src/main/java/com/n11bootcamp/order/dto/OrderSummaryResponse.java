package com.n11bootcamp.order.dto;

import com.n11bootcamp.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(Long id, OrderStatus status, BigDecimal totalAmount, Instant createdAt) {}
