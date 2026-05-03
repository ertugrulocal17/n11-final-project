package com.n11bootcamp.order.web;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n11bootcamp.order.integration.CartSnapshot;
import com.n11bootcamp.order.integration.CartSnapshot.CartLineSnapshot;
import com.n11bootcamp.order.integration.OrderFulfillmentClient;
import com.n11bootcamp.order.integration.ProductSnapshot;
import com.n11bootcamp.platform.jwt.JwtProperties;
import com.n11bootcamp.platform.jwt.JwtTokenSupport;
import io.jsonwebtoken.Jwts;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OrderApiIntegrationTest {

    private static final String TEST_SECRET = "test-jwt-secret-key-at-least-32-characters-long!!";

    private final MockMvc mockMvc;

    @MockitoBean
    private OrderFulfillmentClient fulfillmentClient;

    OrderApiIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void placeOrder_emptyCart_returns400() throws Exception {
        when(fulfillmentClient.getCart(anyString()))
                .thenReturn(new CartSnapshot(List.of(), BigDecimal.ZERO, 0));

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", bearerUserToken(10L, "o@it.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void placeOrder_withLines_returns201_andClearsCart() throws Exception {
        var line = new CartLineSnapshot(
                55L,
                "SKU-55",
                "Ürün",
                "https://ex/img.png",
                new BigDecimal("25.00"),
                2,
                new BigDecimal("50.00"));
        when(fulfillmentClient.getCart(anyString()))
                .thenReturn(new CartSnapshot(List.of(line), new BigDecimal("50.00"), 2));
        when(fulfillmentClient.getProduct(55L))
                .thenReturn(new ProductSnapshot(
                        55L,
                        "SKU-55",
                        "Ürün",
                        "d",
                        "https://ex/img.png",
                        new BigDecimal("25.00"),
                        100));

        mockMvc.perform(
                        post("/api/v1/orders")
                                .header("Authorization", bearerUserToken(11L, "buyer@it.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.totalAmount").value(50.0))
                .andExpect(jsonPath("$.lines[0].productId").value(55));

        verify(fulfillmentClient).adjustStock(eq(55L), eq(-2));
        verify(fulfillmentClient).clearCart(eq(11L));
    }

    @Test
    void listOrders_empty_returnsEmptyPage() throws Exception {
        mockMvc.perform(
                        get("/api/v1/orders")
                                .header("Authorization", bearerUserToken(12L, "l@it.com"))
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getOrder_unknown_returns404() throws Exception {
        mockMvc.perform(
                        get("/api/v1/orders/999999")
                                .header("Authorization", bearerUserToken(13L, "g@it.com")))
                .andExpect(status().isNotFound());
    }

    private static String bearerUserToken(long uid, String email) {
        JwtTokenSupport s = new JwtTokenSupport(new JwtProperties(TEST_SECRET, 3600_000L));
        Date now = new Date();
        Date exp = new Date(now.getTime() + 3600_000L);
        String compact = Jwts.builder()
                .subject(email.toLowerCase())
                .claim("uid", uid)
                .claim("role", "USER")
                .issuedAt(now)
                .expiration(exp)
                .signWith(s.signKey())
                .compact();
        return "Bearer " + compact;
    }
}
