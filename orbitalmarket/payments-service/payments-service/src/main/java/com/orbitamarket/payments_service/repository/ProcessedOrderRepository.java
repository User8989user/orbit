package com.orbitamarket.payments_service.repository;

import com.orbitamarket.payments.model.ProcessedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, String> {
}