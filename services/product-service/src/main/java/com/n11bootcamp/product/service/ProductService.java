package com.n11bootcamp.product.service;

import com.n11bootcamp.common.api.PageResponse;
import com.n11bootcamp.common.exception.InsufficientStockException;
import com.n11bootcamp.common.exception.ProductNotFoundException;
import com.n11bootcamp.product.domain.Product;
import com.n11bootcamp.product.domain.ProductRepository;
import com.n11bootcamp.product.dto.ProductResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> list(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findById(id).map(this::toResponse).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public void applyStockDelta(Long productId, int delta) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        int next = product.getStockQuantity() + delta;
        if (next < 0) {
            throw new InsufficientStockException("Yetersiz stok: ürün " + productId + " için işlem sonrası stok negatif olur.");
        }
        product.setStockQuantity(next);
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getSku(),
                p.getName(),
                p.getDescription(),
                p.getImageUrl(),
                p.getPrice(),
                p.getStockQuantity());
    }
}
