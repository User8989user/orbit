package com.orbitamarket.payments_service.messaging;

import com.orbitamarket.payments.model.Account;
import com.orbitamarket.payments.model.ProcessedOrder;
import com.orbitamarket.payments.model.events.OrderPaymentCompleted;
import com.orbitamarket.payments.model.events.OrderPaymentFailed;
import com.orbitamarket.payments.model.events.OrderPaymentRequested;
import com.orbitamarket.payments.repository.AccountRepository;
import com.orbitamarket.payments.repository.ProcessedOrderRepository;
import com.orbitamarket.payments.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestConsumer {
    private final AccountService accountService;
    private final ProcessedOrderRepository processedOrderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AccountRepository accountRepository;
    private final PlatformTransactionManager transactionManager;

    @KafkaListener(topics = "order.payment.requested", groupId = "payments-group")
    public void handlePaymentRequest(OrderPaymentRequested event) {
        // иденпотентность: проверка, был ли уже обработан заказ
        Optional<ProcessedOrder> existingOpt = processedOrderRepository.findById(event.getOrderId());
        if (existingOpt.isPresent()) {
            ProcessedOrder existing = existingOpt.get();
            if ("COMPLETED".equals(existing.getStatus())) {
                // Отправляем повторно успех, чтобы Orders мог обновить статус (идемпотентно)
                sendPaymentCompleted(event);
            } else {
                sendPaymentFailed(event, existing.getReason());
            }
            return;
        }

        // пытаемся списать средства 
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        try {
            template.executeWithoutResult(status -> {
                Account account = accountService.findByUserId(event.getUserId());
                if (account.getBalance() < event.getAmount()) {
                    // мало средств
                    ProcessedOrder po = new ProcessedOrder();
                    po.setOrderId(event.getOrderId());
                    po.setStatus("FAILED");
                    po.setReason("INSUFFICIENT_BALANCE");
                    processedOrderRepository.save(po);
                    sendPaymentFailed(event, "INSUFFICIENT_BALANCE");
                } else {
                    account.setBalance(account.getBalance() - event.getAmount());
                    accountRepository.save(account); // может выкинуть OptimisticLockingFailureException
                    ProcessedOrder po = new ProcessedOrder();
                    po.setOrderId(event.getOrderId());
                    po.setStatus("COMPLETED");
                    processedOrderRepository.save(po);
                    sendPaymentCompleted(event);
                }
            });
        } catch (OptimisticLockingFailureException e) {
            // При конфликте версий просто логируем и позволяем Kafka повторно доставить сообщение
            log.error("Optimistic lock failure for order {}, will retry on re-delivery", event.getOrderId());
            throw e; // бросаем, чтобы Kafka не коммитила offset и повторила позже
        }
    }

    private void sendPaymentCompleted(OrderPaymentRequested event) {
        OrderPaymentCompleted completed = new OrderPaymentCompleted();
        completed.setEventId(UUID.randomUUID().toString());
        completed.setOrderId(event.getOrderId());
        completed.setUserId(event.getUserId());
        completed.setAmount(event.getAmount());
        // Баланс можно не передавать, но если нужно:
        // completed.setNewBalance(account.getBalance());
        kafkaTemplate.send("order.payment.completed", event.getOrderId(), completed);
    }

    private void sendPaymentFailed(OrderPaymentRequested event, String reason) {
        OrderPaymentFailed failed = new OrderPaymentFailed();
        failed.setEventId(UUID.randomUUID().toString());
        failed.setOrderId(event.getOrderId());
        failed.setUserId(event.getUserId());
        failed.setReason(reason);
        kafkaTemplate.send("order.payment.failed", event.getOrderId(), failed);
    }
}