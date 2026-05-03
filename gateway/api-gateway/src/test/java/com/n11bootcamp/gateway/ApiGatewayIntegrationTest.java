package com.n11bootcamp.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ApiGatewayIntegrationTest {

    private final WebTestClient webTestClient;
    private final GatewayProperties gatewayProperties;

    ApiGatewayIntegrationTest(WebTestClient webTestClient, GatewayProperties gatewayProperties) {
        this.webTestClient = webTestClient;
        this.gatewayProperties = gatewayProperties;
    }

    @Test
    void actuatorHealthIsOk() {
        webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    void routesIncludeConfiguredIds() {
        var ids = gatewayProperties.getRoutes().stream().map(rd -> rd.getId()).toList();
        assertThat(ids)
                .contains(
                        "auth-login-register",
                        "auth-me",
                        "payments-callback",
                        "order-payment-init",
                        "orders",
                        "cart",
                        "products");
    }
}
