package com.n11bootcamp.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {

    Page<ShopOrder> findByUserId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "lines")
    Optional<ShopOrder> findByIdAndUserId(Long id, Long userId);

    Optional<ShopOrder> findByPaymentConversationId(String paymentConversationId);
}
