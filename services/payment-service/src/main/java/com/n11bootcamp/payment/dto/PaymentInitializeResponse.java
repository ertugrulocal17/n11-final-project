package com.n11bootcamp.payment.dto;

public record PaymentInitializeResponse(
        String token, String paymentPageUrl, String checkoutFormContent, String conversationId) {}

