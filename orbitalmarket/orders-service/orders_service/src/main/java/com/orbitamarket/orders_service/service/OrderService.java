package com.orbitamarket.orders_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitamarket.orders.exception.InvalidPayloadException;
import com.orbitamarket.orders.model.Order;
import com.orbitamarket.orders.model.OrderStatus;
import com.orbitamarket.orders.model.OutboxEvent;
import com.orbitamarket.orders.model.events.OrderPaymentRequested;
import com.orbitamarket.orders.repository.OrderRepository;
import com.orbitamarket.orders.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public Order createOrder(String userId, CreateOrderRequest request) {
        if (request.getPrice() <= 0) {
            throw new InvalidPayloadException("INVALID_PRICE", "Price must be > 0");
        }
        if (!List.of("ARCHIVE", "TASKING", "MONITORING").contains(request.getProductType())) {
            throw new InvalidPayloadException("UNKNOWN_PRODUCT_TYPE", "Unknown product type");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setProductType(request.getProductType());
        order.setPrice(request.getPrice());
        order.setPayload(toJson(request.getPayload()));
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setCreatedAt(Instant.now());
        order = orderRepository.save(order);

        // Outbox-запись в той же транзакции
        OrderPaymentRequested event = new OrderPaymentRequested();
        event.setEventId(UUID.randomUUID().toString());
        event.setOrderId(order.getOrderId());
        event.setUserId(userId);
        event.setAmount(order.getPrice());
        event.setOccurredAt(Instant.now());

        OutboxEvent outbox = new OutboxEvent();
        outbox.setAggregateId(order.getOrderId());
        outbox.setEventType("OrderPaymentRequested");
        outbox.setPayload(toJson(event));
        outbox.setCreatedAt(Instant.now());
        outboxRepository.save(outbox);

        return order;
    }

    @SneakyThrows
    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}