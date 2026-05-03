package com.n11bootcamp.order.integration;


/** Ödeme servisi sipariş satırlarını İyzico için saklar. */
public record CheckoutMetadataRequest(String conversationId, String checkoutToken) {}
