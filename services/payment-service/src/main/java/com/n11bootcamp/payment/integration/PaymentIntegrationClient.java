package com.n11bootcamp.payment.integration;

import com.n11bootcamp.common.internal.InternalServiceAuthFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentIntegrationClient {

    private final RestClient orderRestClient;
    private final RestClient authRestClient;

    @Value("${commerce.internal.service-token}")
    private String internalServiceToken;

    public PaymentIntegrationClient(
            @Qualifier("orderRestClient") RestClient orderRestClient,
            @Qualifier("authRestClient") RestClient authRestClient) {
        this.orderRestClient = orderRestClient;
        this.authRestClient = authRestClient;
    }

    public OrderDetailDto getOrder(Long orderId, String authorizationHeaderValue) {
        OrderDetailDto body = orderRestClient
                .get()
                .uri("/api/v1/orders/{id}", orderId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderValue)
                .retrieve()
                .body(OrderDetailDto.class);
        if (body == null) {
            throw new IllegalStateException("Empty order response");
        }
        return body;
    }

    public UserProfileDto getMe(String authorizationHeaderValue) {
        UserProfileDto body = authRestClient
                .get()
                .uri("/api/v1/me")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeaderValue)
                .retrieve()
                .body(UserProfileDto.class);
        if (body == null) {
            throw new IllegalStateException("Empty profile response");
        }
        return body;
    }

    public void patchCheckoutMetadata(Long orderId, Long userId, String conversationId, String checkoutToken) {
        orderRestClient
                .patch()
                .uri("/internal/v1/orders/{orderId}/checkout-metadata", orderId)
                .header(InternalServiceAuthFilter.HEADER, internalServiceToken)
                .header("X-Internal-User-Id", String.valueOf(userId))
                .body(new CheckoutMetadataBody(conversationId, checkoutToken))
                .retrieve()
                .toBodilessEntity();
    }

    public void markPaid(MarkPaidBody body) {
        orderRestClient
                .post()
                .uri("/internal/v1/orders/mark-paid")
                .header(InternalServiceAuthFilter.HEADER, internalServiceToken)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}

