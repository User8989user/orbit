package com.orbitamarket.orders_service.messaging;

import com.orbitamarket.orders.model.InboxEntry;
import com.orbitamarket.orders.model.Order;
import com.orbitamarket.orders.model.OrderStatus;
import com.orbitamarket.orders.model.events.OrderPaymentCompleted;
import com.orbitamarket.orders.model.events.OrderPaymentFailed;
import com.orbitamarket.orders.repository.InboxRepository;
import com.orbitamarket.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResultConsumer {
    private final OrderRepository orderRepository;
    private final InboxRepository inboxRepository;

    @KafkaListener(topics = "order.payment.completed", groupId = "orders-group")
    public void onPaymentCompleted(OrderPaymentCompleted event) {
        if (inboxRepository.existsByEventId(event.getEventId())) return;

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            log.error("Order not found for completed payment: {}", event.getOrderId());
            return;
        }
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        InboxEntry inbox = new InboxEntry();
        inbox.setEventId(event.getEventId());
        inbox.setOrderId(event.getOrderId());
        inbox.setEventType("OrderPaymentCompleted");
        inbox.setProcessed(true);
        inboxRepository.save(inbox);
    }

    @KafkaListener(topics = "order.payment.failed", groupId = "orders-group")
    public void onPaymentFailed(OrderPaymentFailed event) {
        if (inboxRepository.existsByEventId(event.getEventId())) return;

        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) return;
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        order.setFailureReason(event.getReason());
        orderRepository.save(order);

        InboxEntry inbox = new InboxEntry();
        inbox.setEventId(event.getEventId());
        inbox.setOrderId(event.getOrderId());
        inbox.setEventType("OrderPaymentFailed");
        inbox.setProcessed(true);
        inboxRepository.save(inbox);
    }
}