package com.orbitamarket.orders_service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitamarket.orders.model.OutboxEvent;
import com.orbitamarket.orders.model.events.OrderPaymentRequested;
import com.orbitamarket.orders.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findTop100ByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent e : events) {
            try {
                OrderPaymentRequested event = objectMapper.readValue(e.getPayload(), OrderPaymentRequested.class);
                kafkaTemplate.send("order.payment.requested", e.getAggregateId(), event).get();
                e.setPublished(true);
                outboxRepository.save(e);
            } catch (Exception ex) {
                log.error("Failed to publish outbox event {}", e.getId(), ex);
            }
        }
    }
}