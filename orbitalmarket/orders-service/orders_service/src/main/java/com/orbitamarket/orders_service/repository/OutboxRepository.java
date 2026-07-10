package com.orbitamarket.orders.repository;

import com.orbitamarket.orders.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop100ByPublishedFalseOrderByCreatedAtAsc();
}