package com.n11bootcamp.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ApiGatewayProxyIntegrationTest {

    private static final MockWebServer PRODUCT_BACKEND = new MockWebServer();

    static {
        try {
            PRODUCT_BACKEND.start();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void registerProductDownstream(DynamicPropertyRegistry registry) {
        registry.add("PRODUCT_URI", () -> "http://127.0.0.1:" + PRODUCT_BACKEND.getPort());
    }

    private final WebTestClient webTestClient;

    ApiGatewayProxyIntegrationTest(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @AfterAll
    static void shutdownBackend() throws IOException {
        PRODUCT_BACKEND.shutdown();
    }

    @Test
    void routesProductsPathToProductUriDownstream() throws InterruptedException {
        PRODUCT_BACKEND.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"proxied\":true}"));

        webTestClient
                .get()
                .uri("/api/v1/products?page=0&size=5")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(res -> assertThat(res.getResponseBody()).contains("proxied"));

        RecordedRequest req = PRODUCT_BACKEND.takeRequest();
        assertThat(req.getPath()).startsWith("/api/v1/products");
    }
}
