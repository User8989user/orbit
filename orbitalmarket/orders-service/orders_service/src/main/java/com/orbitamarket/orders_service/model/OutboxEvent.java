package com.orbitamarket.orders_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "outbox")
@Data
@NoArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String aggregateId;
    private String eventType;
    @Column(columnDefinition = "text")
    private String payload;
    private Instant createdAt;
    private boolean published = false;
}