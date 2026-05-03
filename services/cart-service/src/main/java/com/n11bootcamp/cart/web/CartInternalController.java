package com.n11bootcamp.cart.web;

import com.n11bootcamp.cart.service.CartService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/users")
@Hidden
public class CartInternalController {

    private final CartService cartService;

    public CartInternalController(CartService cartService) {
        this.cartService = cartService;
    }

    @DeleteMapping("/{userId}/cart")
    public void clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
    }
}
