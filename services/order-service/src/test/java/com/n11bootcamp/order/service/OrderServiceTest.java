package com.n11bootcamp.order.service;

import com.n11bootcamp.common.exception.EmptyCartException;
import com.n11bootcamp.order.domain.ShopOrderRepository;
import com.n11bootcamp.order.integration.CartSnapshot;
import com.n11bootcamp.order.integration.OrderFulfillmentClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ShopOrderRepository shopOrderRepository;

    @Mock
    private OrderFulfillmentClient fulfillmentClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_emptyCart_throws() {
        when(fulfillmentClient.getCart(anyString()))
                .thenReturn(new CartSnapshot(List.of(), BigDecimal.ZERO, 0));

        assertThatThrownBy(() -> orderService.placeOrder(1L, "Bearer token"))
                .isInstanceOf(EmptyCartException.class);
    }
}
