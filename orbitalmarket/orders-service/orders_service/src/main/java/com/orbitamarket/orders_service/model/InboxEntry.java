package com.orbitamarket.orders_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inbox")
@Data
@NoArgsConstructor
public class InboxEntry {
    @Id
    private String eventId;
    private String orderId;
    private String eventType;
    private boolean processed;
}