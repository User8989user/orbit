package com.orbitamarket.orders_service.repository;

import com.orbitamarket.orders.model.InboxEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxRepository extends JpaRepository<InboxEntry, String> {
    boolean existsByEventId(String eventId);
}