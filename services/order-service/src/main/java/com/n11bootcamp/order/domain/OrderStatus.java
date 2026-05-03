package com.n11bootcamp.order.domain;

public enum OrderStatus {
    /** Sipariş oluşturuldu; ödeme (İyzico) sonrası PAID olacak */
    CREATED,
    PAID,
    CANCELLED
}
