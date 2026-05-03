package com.n11bootcamp.product.web;

import com.n11bootcamp.product.service.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/products")
@Hidden
public class ProductInternalController {
    private final ProductService productService;

    public ProductInternalController(ProductService productService) {
        this.productService = productService;
    }

    public record StockDeltaRequest(@NotNull Integer delta) {}

    @PatchMapping("/{id}/stock")
    public void adjustStock(@PathVariable Long id, @Valid @RequestBody StockDeltaRequest body) {
        productService.applyStockDelta(id, body.delta());
    }
}
