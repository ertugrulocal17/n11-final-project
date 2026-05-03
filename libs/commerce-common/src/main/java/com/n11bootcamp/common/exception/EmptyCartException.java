package com.n11bootcamp.common.exception;

public class EmptyCartException extends RuntimeException{
    public EmptyCartException() {
        super("Cart is empty");
    }
}
