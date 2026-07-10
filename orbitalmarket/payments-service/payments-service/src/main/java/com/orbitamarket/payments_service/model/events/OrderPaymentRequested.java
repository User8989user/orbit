package com.orbitamarket.payments_service.model.events;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
public class OrderPaymentRequested {
    private String eventId;
    private String orderId;
    private String userId;
    private long amount;
    private Instant occurredAt;
}