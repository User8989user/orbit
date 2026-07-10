package com.orbitamarket.payments_service.model.events;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderPaymentFailed {
    private String eventId;
    private String orderId;
    private String userId;
    private String reason;
}