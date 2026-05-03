package com.n11bootcamp.payment.integration;

import java.math.BigDecimal;

public record MarkPaidBody(String conversationId, BigDecimal paidAmount, String paymentStatus) {}
