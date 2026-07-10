package com.orbitamarket.orders_service.controller;

import com.orbitamarket.orders.exception.InvalidPayloadException;
import com.orbitamarket.orders.model.Order;
import com.orbitamarket.orders.repository.OrderRepository;
import com.orbitamarket.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader("X-User-Id") String userId,
                                         @Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(userId, request);
            return ResponseEntity.status(201).body(toMap(order));
        } catch (InvalidPayloadException e) {
            return ResponseEntity.badRequest().body(errorMap(e.getErrorCode(), e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listOrders(@RequestHeader("X-User-Id") String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return ResponseEntity.ok(orders.stream().map(this::toMap).toList());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@RequestHeader("X-User-Id") String userId, @PathVariable String orderId) {
        return orderRepository.findById(orderId)
                .filter(o -> o.getUserId().equals(userId))
                .map(o -> ResponseEntity.ok(toMap(o)))
                .orElse(ResponseEntity.status(404).body(errorMap("ORDER_NOT_FOUND", "Order not found")));
    }

    private Map<String, Object> toMap(Order o) {
        return Map.of(
                "order_id", o.getOrderId(),
                "status", o.getStatus().name(),
                "product_type", o.getProductType(),
                "price", o.getPrice(),
                "created_at", o.getCreatedAt().toString()
        );
    }

    private Map<String, Object> errorMap(String code, String msg) {
        return Map.of(
                "error_code", code,
                "message", msg,
                "timestamp", Instant.now().toString()
        );
    }
}