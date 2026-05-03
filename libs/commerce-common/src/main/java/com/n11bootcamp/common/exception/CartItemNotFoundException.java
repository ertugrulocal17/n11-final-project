package com.n11bootcamp.common.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(Long userId,Long productId){
        super("Cart item not found for user " + userId + " and product " + productId);
    }
}
