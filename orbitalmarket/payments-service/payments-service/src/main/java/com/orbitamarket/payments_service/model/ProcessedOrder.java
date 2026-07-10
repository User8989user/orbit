package com.orbitamarket.payments_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_orders")
@Data
@NoArgsConstructor
public class ProcessedOrder {
    @Id
    private String orderId;
    private String status;   // COMPLETED или FAILED
    private String reason;   // причина отказа (для FAILED)
}