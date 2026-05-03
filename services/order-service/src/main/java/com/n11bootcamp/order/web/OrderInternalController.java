package com.n11bootcamp.order.web;

import com.n11bootcamp.order.integration.CheckoutMetadataRequest;
import com.n11bootcamp.order.integration.MarkPaidRequest;
import com.n11bootcamp.order.service.OrderService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/v1/orders")
@Hidden
public class OrderInternalController {

    public static final String INTERNAL_USER_HEADER = "X-Internal-User-Id";

    private final OrderService orderService;

    public OrderInternalController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PatchMapping("/{orderId}/checkout-metadata")
    public void checkoutMetadata(
            @PathVariable Long orderId,
            @RequestHeader(INTERNAL_USER_HEADER) Long userId,
            @Valid @RequestBody CheckoutMetadataRequest body) {
        orderService.attachCheckoutMetadata(orderId, userId, body.conversationId(), body.checkoutToken());
    }

    @PostMapping("/mark-paid")
    public void markPaid(@Valid @RequestBody MarkPaidRequest body) {
        orderService.markPaidFromPaymentProvider(body);
    }
}
