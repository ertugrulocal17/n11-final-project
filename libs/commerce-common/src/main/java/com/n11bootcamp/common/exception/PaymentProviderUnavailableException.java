package com.n11bootcamp.common.exception;

public class PaymentProviderUnavailableException extends RuntimeException {
    public PaymentProviderUnavailableException(String message) {
        super(message);
    }
}
