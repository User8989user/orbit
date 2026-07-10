package com.orbitamarket.orders_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    private String userId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String productType;
    private long price;

    @Column(columnDefinition = "jsonb")
    private String payload;

    private String failureReason;
    private Instant createdAt;
}