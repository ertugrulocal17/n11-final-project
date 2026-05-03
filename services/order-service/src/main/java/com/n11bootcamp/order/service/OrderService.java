package com.n11bootcamp.order.service;

import com.n11bootcamp.common.api.PageResponse;
import com.n11bootcamp.common.exception.*;
import com.n11bootcamp.order.domain.OrderStatus;
import com.n11bootcamp.order.domain.ShopOrder;
import com.n11bootcamp.order.domain.ShopOrderLine;
import com.n11bootcamp.order.domain.ShopOrderRepository;
import com.n11bootcamp.order.dto.OrderDetailResponse;
import com.n11bootcamp.order.dto.OrderLineResponse;
import com.n11bootcamp.order.dto.OrderSummaryResponse;
import com.n11bootcamp.order.integration.CartSnapshot;
import com.n11bootcamp.order.integration.CartSnapshot.CartLineSnapshot;
import com.n11bootcamp.order.integration.MarkPaidRequest;
import com.n11bootcamp.order.integration.OrderFulfillmentClient;
import com.n11bootcamp.order.integration.ProductSnapshot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final ShopOrderRepository shopOrderRepository;
    private final OrderFulfillmentClient fulfillmentClient;

    public OrderService(ShopOrderRepository shopOrderRepository, OrderFulfillmentClient fulfillmentClient) {
        this.shopOrderRepository = shopOrderRepository;
        this.fulfillmentClient = fulfillmentClient;
    }

    @Transactional
    public OrderDetailResponse placeOrder(Long userId, String authorizationHeader) {
        CartSnapshot cart = fulfillmentClient.getCart(authorizationHeader);
        if (cart.lines() == null || cart.lines().isEmpty()) {
            throw new EmptyCartException();
        }

        List<ShopOrderLine> lineEntities = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartSnapshot.CartLineSnapshot line : cart.lines()) {
            ProductSnapshot product = fulfillmentClient.getProduct(line.productId());
            if (product.stockQuantity() < line.quantity()) {
                throw new InsufficientStockException(
                        "Yetersiz stok: ürün "
                                + line.productId()
                                + " için en fazla "
                                + product.stockQuantity()
                                + " adet.");
            }
            BigDecimal unitPrice = product.price();
            int qty = line.quantity();
            BigDecimal lineTotal =
                    unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(2, RoundingMode.HALF_UP);
            total = total.add(lineTotal);

            ShopOrderLine ol = new ShopOrderLine();
            ol.setProductId(product.id());
            ol.setProductSku(product.sku());
            ol.setProductName(product.name());
            ol.setProductImageUrl(product.imageUrl());
            ol.setUnitPrice(unitPrice);
            ol.setQuantity(qty);
            ol.setLineTotal(lineTotal);
            lineEntities.add(ol);
        }

        ShopOrder order = new ShopOrder();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        order.setCreatedAt(Instant.now());
        for (ShopOrderLine line : lineEntities) {
            line.setOrder(order);
            order.getLines().add(line);
        }
        ShopOrder saved = shopOrderRepository.save(order);

        for (CartLineSnapshot line : cart.lines()) {
            fulfillmentClient.adjustStock(line.productId(), -line.quantity());
        }

        fulfillmentClient.clearCart(userId);

        return toDetail(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> listOrders(Long userId, Pageable pageable) {
        Page<ShopOrder> page = shopOrderRepository.findByUserId(userId, pageable);
        return PageResponse.from(page.map(OrderService::toSummary));
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(Long userId, Long orderId) {
        ShopOrder order = shopOrderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return toDetail(order);
    }

    @Transactional
    public void attachCheckoutMetadata(Long orderId, Long userId, String conversationId, String checkoutToken) {
        ShopOrder order = shopOrderRepository
                .findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Sipariş checkout için uygun değil: " + order.getStatus());
        }
        if (conversationId != null) {
            order.setPaymentConversationId(conversationId);
        }
        if (checkoutToken != null) {
            order.setIyzicoCheckoutToken(checkoutToken);
        }
        shopOrderRepository.save(order);
    }

    @Transactional
    public void markPaidFromPaymentProvider(MarkPaidRequest request) {
        ShopOrder order = shopOrderRepository
                .findByPaymentConversationId(request.conversationId())
                .orElseThrow(() -> new IyzicoIntegrationException(
                        "Sipariş bulunamadı: " + request.conversationId()));

        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new InvalidOrderStateException("Sipariş durumu ödeme için uygun değil: " + order.getStatus());
        }

        if (!"SUCCESS".equalsIgnoreCase(request.paymentStatus())) {
            return;
        }

        BigDecimal paid = request.paidAmount() != null ? request.paidAmount() : BigDecimal.ZERO;
        if (paid.setScale(2, RoundingMode.HALF_UP)
                .compareTo(order.getTotalAmount().setScale(2, RoundingMode.HALF_UP))
                != 0) {
            throw new IyzicoIntegrationException("Ödenen tutar sipariş tutarı ile eşleşmiyor");
        }

        order.setStatus(OrderStatus.PAID);
        shopOrderRepository.save(order);
    }

    private static OrderSummaryResponse toSummary(ShopOrder o) {
        return new OrderSummaryResponse(o.getId(), o.getStatus(), o.getTotalAmount(), o.getCreatedAt());
    }

    private static OrderDetailResponse toDetail(ShopOrder o) {
        List<OrderLineResponse> lines = o.getLines().stream()
                .map(l -> new OrderLineResponse(
                        l.getProductId(),
                        l.getProductSku(),
                        l.getProductName(),
                        l.getProductImageUrl(),
                        l.getUnitPrice(),
                        l.getQuantity(),
                        l.getLineTotal()))
                .toList();
        return new OrderDetailResponse(o.getId(), o.getStatus(), o.getTotalAmount(), o.getCreatedAt(), lines);
    }
}

