package com.n11bootcamp.order.web;

import com.n11bootcamp.common.api.PageResponse;
import com.n11bootcamp.order.dto.OrderDetailResponse;
import com.n11bootcamp.order.dto.OrderSummaryResponse;
import com.n11bootcamp.order.service.OrderService;
import com.n11bootcamp.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Sipariş oluşturma ve listeleme")
@SecurityRequirement(name = "bearer-jwt")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Sepetten sipariş oluştur")
    public OrderDetailResponse place(
            @AuthenticationPrincipal UserPrincipal principal, HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        return orderService.placeOrder(principal.getId(), auth);
    }

    @GetMapping
    @Operation(summary = "Siparişlerimi listele (sayfalı)")
    public PageResponse<OrderSummaryResponse> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return orderService.listOrders(principal.getId(), pageable);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Sipariş detayı")
    public OrderDetailResponse get(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long orderId) {
        return orderService.getOrder(principal.getId(), orderId);
    }
}

