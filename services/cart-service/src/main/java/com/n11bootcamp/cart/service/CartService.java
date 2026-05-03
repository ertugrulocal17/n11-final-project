package com.n11bootcamp.cart.service;

import com.n11bootcamp.cart.client.ProductCatalogClient;
import com.n11bootcamp.cart.client.ProductCatalogClient.ProductDto;
import com.n11bootcamp.cart.domain.CartItem;
import com.n11bootcamp.cart.domain.CartItemRepository;
import com.n11bootcamp.cart.dto.AddToCartRequest;
import com.n11bootcamp.cart.dto.CartLineResponse;
import com.n11bootcamp.cart.dto.CartResponse;
import com.n11bootcamp.cart.dto.UpdateCartItemRequest;
import com.n11bootcamp.common.exception.CartItemNotFoundException;
import com.n11bootcamp.common.exception.InsufficientStockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductCatalogClient productCatalogClient;

    public CartService(CartItemRepository cartItemRepository, ProductCatalogClient productCatalogClient) {
        this.cartItemRepository = cartItemRepository;
        this.productCatalogClient = productCatalogClient;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findAllByUserIdOrderByIdAsc(userId);
        return toResponse(items);
    }

    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest request) {
        Long productId = request.productId();
        int quantity = request.quantity();
        ProductDto product = productCatalogClient.getProduct(productId);

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, productId);
        int existingQty = existing.map(CartItem::getQuantity).orElse(0);
        int newQty = existingQty + quantity;
        assertStock(product, newQty);

        if (existingQty == 0) {
            CartItem line = new CartItem();
            line.setUserId(userId);
            line.setProductId(productId);
            line.setQuantity(quantity);
            cartItemRepository.save(line);
        } else {
            CartItem line = existing.orElseThrow();
            line.setQuantity(newQty);
        }
        return getCart(userId);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long productId, UpdateCartItemRequest request) {
        CartItem line = cartItemRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new CartItemNotFoundException(userId, productId));
        ProductDto product = productCatalogClient.getProduct(productId);
        assertStock(product, request.quantity());
        line.setQuantity(request.quantity());
        return getCart(userId);
    }

    @Transactional
    public void removeItem(Long userId, Long productId) {
        if (cartItemRepository.findByUserIdAndProductId(userId, productId).isEmpty()) {
            throw new CartItemNotFoundException(userId, productId);
        }
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private static void assertStock(ProductDto product, int requestedQty) {
        if (requestedQty > product.stockQuantity()) {
            throw new InsufficientStockException(
                    "Yetersiz stok: ürün " + product.id() + " için en fazla " + product.stockQuantity() + " adet.");
        }
    }

    private CartResponse toResponse(List<CartItem> items) {
        List<CartLineResponse> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (CartItem item : items) {
            ProductDto p = productCatalogClient.getProduct(item.getProductId());
            BigDecimal unitPrice = p.price();
            BigDecimal lineTotal =
                    unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);
            totalQuantity += item.getQuantity();
            lines.add(new CartLineResponse(
                    p.id(), p.sku(), p.name(), p.imageUrl(), unitPrice, item.getQuantity(), lineTotal));
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        return new CartResponse(lines, subtotal, totalQuantity);
    }
}
