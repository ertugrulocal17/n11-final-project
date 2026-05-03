package com.n11bootcamp.cart.service;

import com.n11bootcamp.cart.client.ProductCatalogClient;
import com.n11bootcamp.cart.client.ProductCatalogClient.ProductDto;
import com.n11bootcamp.cart.domain.CartItem;
import com.n11bootcamp.cart.domain.CartItemRepository;
import com.n11bootcamp.cart.dto.AddToCartRequest;
import com.n11bootcamp.cart.dto.CartResponse;
import com.n11bootcamp.common.exception.CartItemNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductCatalogClient productCatalogClient;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCart_emptyUser_returnsZeroTotals() {
        when(cartItemRepository.findAllByUserIdOrderByIdAsc(7L)).thenReturn(List.of());

        CartResponse cart = cartService.getCart(7L);

        assertThat(cart.lines()).isEmpty();
        assertThat(cart.totalQuantity()).isZero();
        assertThat(cart.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void addItem_newLine_savesCartItem() {
        ProductDto product =
                new ProductDto(2L, "SKU2", "Ürün", "Açıklama", "https://x/u.jpg", new BigDecimal("25.00"), 10);
        when(productCatalogClient.getProduct(2L)).thenReturn(product);
        when(cartItemRepository.findByUserIdAndProductId(1L, 2L)).thenReturn(Optional.empty());

        cartService.addItem(1L, new AddToCartRequest(2L, 3));

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getProductId()).isEqualTo(2L);
        assertThat(saved.getQuantity()).isEqualTo(3);
    }

    @Test
    void removeItem_missing_throws() {
        when(cartItemRepository.findByUserIdAndProductId(1L, 99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(1L, 99L)).isInstanceOf(CartItemNotFoundException.class);
        verify(cartItemRepository).findByUserIdAndProductId(1L, 99L);
        verify(cartItemRepository, never()).deleteByUserIdAndProductId(anyLong(), anyLong());
    }
}
