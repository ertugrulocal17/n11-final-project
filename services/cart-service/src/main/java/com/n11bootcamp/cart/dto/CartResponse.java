package com.n11bootcamp.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(List<CartLineResponse> lines, BigDecimal subtotal, int totalQuantity) {
}
