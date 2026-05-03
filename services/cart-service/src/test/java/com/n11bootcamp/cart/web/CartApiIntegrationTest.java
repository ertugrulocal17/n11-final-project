package com.n11bootcamp.cart.web;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.n11bootcamp.cart.client.ProductCatalogClient;
import com.n11bootcamp.cart.client.ProductCatalogClient.ProductDto;
import com.n11bootcamp.platform.jwt.JwtProperties;
import com.n11bootcamp.platform.jwt.JwtTokenSupport;
import io.jsonwebtoken.Jwts;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
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
class CartApiIntegrationTest {

    private static final String TEST_SECRET = "test-jwt-secret-key-at-least-32-characters-long!!";

    private final MockMvc mockMvc;

    @MockitoBean
    private ProductCatalogClient productCatalogClient;

    CartApiIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void stubProduct() {
        when(productCatalogClient.getProduct(anyLong()))
                .thenReturn(new ProductDto(
                        1L,
                        "SKU-IT",
                        "Test ürün",
                        "Açıklama",
                        "https://example.com/i.jpg",
                        new BigDecimal("10.00"),
                        50));
    }

    @Test
    void getCart_empty_returnsZeroTotals() throws Exception {
        mockMvc.perform(get("/api/v1/cart").header("Authorization", bearerUserToken(100L, "cart-user@it.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines").isArray())
                .andExpect(jsonPath("$.subtotal").value(0))
                .andExpect(jsonPath("$.totalQuantity").value(0));
    }

    @Test
    void addItem_then_getCart_returnsLine() throws Exception {
        String auth = bearerUserToken(200L, "buyer@it.com");
        mockMvc.perform(
                        post("/api/v1/cart")
                                .header("Authorization", auth)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"productId\":1,\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.lines[0].productId").value(1))
                .andExpect(jsonPath("$.lines[0].quantity").value(2));

        mockMvc.perform(get("/api/v1/cart").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0]").exists());
    }

    @Test
    void getCart_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/cart")).andExpect(status().isUnauthorized());
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
