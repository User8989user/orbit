package com.orbitamarket.payments_service.model.events;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderPaymentCompleted {
    private String eventId;
    private String orderId;
    private String userId;
    private long amount;
    private long newBalance;   
}