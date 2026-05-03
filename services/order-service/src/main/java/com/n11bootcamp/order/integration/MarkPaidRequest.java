package com.n11bootcamp.order.integration;


import java.math.BigDecimal;

public record MarkPaidRequest(String conversationId, BigDecimal paidAmount, String paymentStatus) {}
