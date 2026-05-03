package com.n11bootcamp.product.web;

import com.n11bootcamp.common.api.PageResponse;
import com.n11bootcamp.product.dto.ProductResponse;
import com.n11bootcamp.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Urun listeme ve detay")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Urunleri sayfali listeleme")
    public PageResponse<ProductResponse> list(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return productService.list(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Urun detay")
    public ProductResponse get(@PathVariable Long id) {
        return productService.getById(id);
    }
}
