package com.n11bootcamp.product.service;

import com.n11bootcamp.common.api.PageResponse;
import com.n11bootcamp.common.exception.ProductNotFoundException;
import com.n11bootcamp.product.domain.Product;
import com.n11bootcamp.product.domain.ProductRepository;
import com.n11bootcamp.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getById_returnsMappedResponse() {
        Product p = sampleProduct(5L);
        when(productRepository.findById(5L)).thenReturn(Optional.of(p));

        ProductResponse r = productService.getById(5L);

        assertThat(r.id()).isEqualTo(5L);
        assertThat(r.sku()).isEqualTo("SKU-5");
        assertThat(r.name()).isEqualTo("Kalem");
        assertThat(r.imageUrl()).isEqualTo("https://example.com/5.jpg");
        assertThat(r.price()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(r.stockQuantity()).isEqualTo(3);
    }

    @Test
    void getById_throwsWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(99L)).isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void list_returnsPageResponse() {
        Product p = sampleProduct(1L);
        var pageable = PageRequest.of(0, 20);
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(p), pageable, 1));

        PageResponse<ProductResponse> page = productService.list(pageable);

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.page()).isZero();
        assertThat(page.content().getFirst().sku()).isEqualTo("SKU-1");
    }

    private static Product sampleProduct(Long id) {
        Product p = new Product();
        p.setId(id);
        p.setSku("SKU-" + id);
        p.setName("Kalem");
        p.setDescription("Açıklama");
        p.setImageUrl("https://example.com/" + id + ".jpg");
        p.setPrice(BigDecimal.TEN);
        p.setStockQuantity(3);
        return p;
    }
}
