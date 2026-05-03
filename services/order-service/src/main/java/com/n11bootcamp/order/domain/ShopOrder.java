package com.n11bootcamp.order.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "shop_orders",
        indexes = {@Index(name = "idx_shop_orders_user", columnList = "user_id")})
public class ShopOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "payment_conversation_id", unique = true, length = 64)
    private String paymentConversationId;

    @Column(name = "iyzico_checkout_token", length = 512)
    private String iyzicoCheckoutToken;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShopOrderLine> lines = new ArrayList<>();

    public ShopOrder() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getPaymentConversationId() {
        return paymentConversationId;
    }

    public void setPaymentConversationId(String paymentConversationId) {
        this.paymentConversationId = paymentConversationId;
    }

    public String getIyzicoCheckoutToken() {
        return iyzicoCheckoutToken;
    }

    public void setIyzicoCheckoutToken(String iyzicoCheckoutToken) {
        this.iyzicoCheckoutToken = iyzicoCheckoutToken;
    }

    public List<ShopOrderLine> getLines() {
        return lines;
    }

    public void setLines(List<ShopOrderLine> lines) {
        this.lines = lines;
    }
}

