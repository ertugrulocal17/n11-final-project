package com.n11bootcamp.payment.service;

import com.n11bootcamp.common.exception.InvalidOrderStateException;
import com.n11bootcamp.common.exception.OrderNotFoundException;
import com.n11bootcamp.common.exception.PaymentProviderUnavailableException;
import com.n11bootcamp.payment.config.IyzicoProperties;
import com.n11bootcamp.payment.integration.OrderDetailDto;
import com.n11bootcamp.payment.integration.PaymentIntegrationClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static IyzicoProperties enabledProps() {
        return new IyzicoProperties(
                true,
                "sandbox-api-key",
                "sandbox-secret",
                "https://sandbox-api.iyzipay.com",
                "http://localhost:8080/api/v1/payments/iyzico/callback",
                "74300864791");
    }

    @Mock
    private PaymentIntegrationClient integration;

    @Test
    void initializeCheckout_iyzicoDisabled_throws() {
        PaymentService paymentService = new PaymentService(
                new IyzicoProperties(
                        false,
                        "k",
                        "s",
                        "https://sandbox-api.iyzipay.com",
                        "http://localhost/cb",
                        null),
                integration);

        assertThatThrownBy(() -> paymentService.initializeCheckout(1L, 2L, "Bearer t"))
                .isInstanceOf(PaymentProviderUnavailableException.class)
                .hasMessageContaining("devre dışı");
    }

    @Test
    void initializeCheckout_missingCredentials_throws() {
        PaymentService paymentService =
                new PaymentService(new IyzicoProperties(true, " ", "  ", "https://x", "http://cb", null), integration);

        assertThatThrownBy(() -> paymentService.initializeCheckout(1L, 2L, "Bearer t"))
                .isInstanceOf(PaymentProviderUnavailableException.class)
                .hasMessageContaining("anahtar");
    }

    @Test
    void initializeCheckout_orderNotFoundFromOrderService_throws() {
        PaymentService paymentService = new PaymentService(enabledProps(), integration);
        when(integration.getOrder(anyLong(), anyString()))
                .thenThrow(
                        new RestClientResponseException(
                                "nf",
                                HttpStatus.NOT_FOUND,
                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                HttpHeaders.EMPTY,
                                null,
                                StandardCharsets.UTF_8));

        assertThatThrownBy(() -> paymentService.initializeCheckout(1L, 404L, "Bearer t"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void initializeCheckout_nonCreatedOrder_throws() {
        PaymentService paymentService = new PaymentService(enabledProps(), integration);
        var line =
                new OrderDetailDto.OrderLineDto(1L, "S", "N", BigDecimal.ONE, 1, BigDecimal.ONE);
        when(integration.getOrder(anyLong(), anyString()))
                .thenReturn(new OrderDetailDto(9L, "PAID", BigDecimal.TEN, List.of(line)));

        assertThatThrownBy(() -> paymentService.initializeCheckout(1L, 9L, "Bearer tok"))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("PAID");
    }
}
