package com.n11bootcamp.payment.web;

import com.n11bootcamp.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/iyzico")
public class IyzicoCallbackController {

    private final PaymentService paymentService;

    public IyzicoCallbackController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Hidden
    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleCallback(@RequestParam("token") String token) {
        paymentService.handleCheckoutCallback(token);
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body("OK");
    }
}

