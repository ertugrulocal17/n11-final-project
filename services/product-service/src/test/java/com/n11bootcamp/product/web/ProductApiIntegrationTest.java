package com.n11bootcamp.product.web;

import com.n11bootcamp.product.domain.Product;
import com.n11bootcamp.product.domain.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProductApiIntegrationTest {

    private final MockMvc mockMvc;
    private final ProductRepository productRepository;

    ProductApiIntegrationTest(MockMvc mockMvc, ProductRepository productRepository) {
        this.mockMvc = mockMvc;
        this.productRepository = productRepository;
    }

    @BeforeEach
    void clean() {
        productRepository.deleteAll();
    }

    @Test
    void listProducts_emptyPage() throws Exception {
        mockMvc.perform(get("/api/v1/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getProduct_byId_returnsJson() throws Exception {
        Product p = new Product();
        p.setSku("INT-SKU-1");
        p.setName("Entegrasyon ürünü");
        p.setDescription("Test");
        p.setImageUrl("https://picsum.photos/seed/it/400/300");
        p.setPrice(new BigDecimal("99.99"));
        p.setStockQuantity(7);
        p = productRepository.save(p);

        mockMvc.perform(get("/api/v1/products/" + p.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p.getId()))
                .andExpect(jsonPath("$.sku").value("INT-SKU-1"))
                .andExpect(jsonPath("$.name").value("Entegrasyon ürünü"))
                .andExpect(jsonPath("$.imageUrl").value("https://picsum.photos/seed/it/400/300"))
                .andExpect(jsonPath("$.stockQuantity").value(7));
    }

    @Test
    void getProduct_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
