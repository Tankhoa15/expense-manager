package com.dev.expense_manager.messaging;

import com.dev.expense_manager.message.BudgetAlertMessage;
import com.dev.expense_manager.message.NotificationMessage;
import com.dev.expense_manager.message.TransactionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionMessageConsumer {

    @RabbitListener(queues = "${app.rabbitmq.queues.transaction}")
    public void handleTransactionCreated(TransactionMessage message) {
        log.info("Received transaction message: id={}, userId={}, type={}, amount={}",
                message.getId(),
                message.getUserId(),
                message.getType(),
                message.getAmount());

        try {
            processTransaction(message);
        } catch (Exception e) {
            log.error("Error processing transaction message: {}", message.getId(), e);
            throw e;
        }
    }

    private void processTransaction(TransactionMessage message) {
        switch (message.getEventType()) {
            case "CREATED" -> handleTransactionCreatedEvent(message);
            case "UPDATED" -> handleTransactionUpdatedEvent(message);
            case "DELETED" -> handleTransactionDeletedEvent(message);
            default -> log.warn("Unknown event type: {}", message.getEventType());
        }
    }

    private void handleTransactionCreatedEvent(TransactionMessage message) {
        log.info("Processing new transaction: {} - {} for {}",
                message.getType(),
                message.getAmount(),
                message.getDescription());

        if (message.getType().name().equals("EXPENSE")) {
            log.info("Expense recorded for category: {}", message.getCategoryName());
        } else {
            log.info("Income recorded: {}", message.getAmount());
        }
    }

    private void handleTransactionUpdatedEvent(TransactionMessage message) {
        log.info("Transaction updated: {} - new amount: {}",
                message.getId(),
                message.getAmount());
    }

    private void handleTransactionDeletedEvent(TransactionMessage message) {
        log.info("Transaction deleted: {}", message.getId());
    }
}
