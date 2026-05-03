package com.n11bootcamp.cart.web;

import com.n11bootcamp.cart.dto.AddToCartRequest;
import com.n11bootcamp.cart.dto.CartResponse;
import com.n11bootcamp.cart.dto.UpdateCartItemRequest;
import com.n11bootcamp.cart.service.CartService;
import com.n11bootcamp.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@Tag(name = "Cart", description = "Sepet (oturum gerekli)")
@SecurityRequirement(name = "bearer-jwt")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @Operation(summary = "Sepeti listele")
    public CartResponse get(@AuthenticationPrincipal UserPrincipal principal) {
        return cartService.getCart(principal.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Sepete ürün ekle")
    public CartResponse add(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AddToCartRequest request) {
        return cartService.addItem(principal.getId(), request);
    }

    @PatchMapping("/items/{productId}")
    @Operation(summary = "Sepetteki ürün miktarını güncelle")
    public CartResponse update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(principal.getId(), productId, request);
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Ürünü sepetten çıkar")
    public void remove(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long productId) {
        cartService.removeItem(principal.getId(), productId);
    }
}

