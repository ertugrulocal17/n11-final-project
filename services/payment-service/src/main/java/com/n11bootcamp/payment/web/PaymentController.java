package com.n11bootcamp.payment.web;

import com.n11bootcamp.payment.dto.PaymentInitializeResponse;
import com.n11bootcamp.payment.service.PaymentService;
import com.n11bootcamp.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/payment")
@Tag(name = "Payments", description = "İyzico ödeme başlatma")
@SecurityRequirement(name = "bearer-jwt")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/iyzico/initialize")
    @Operation(summary = "İyzico Checkout Form başlat")
    public PaymentInitializeResponse initializeIyzico(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long orderId,
            HttpServletRequest request) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        return paymentService.initializeCheckout(principal.getId(), orderId, auth);
    }
}
