package com.n11bootcamp.cart.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull Long productId, @NotNull @Min(1) @Max(999) Integer quantity
) {}
