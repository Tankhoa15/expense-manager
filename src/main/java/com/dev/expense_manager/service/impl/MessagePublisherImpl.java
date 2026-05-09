package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.message.BudgetAlertMessage;
import com.dev.expense_manager.message.NotificationMessage;
import com.dev.expense_manager.message.TransactionMessage;
import com.dev.expense_manager.service.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePublisherImpl implements MessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-keys.transaction}")
    private String transactionRoutingKey;

    @Value("${app.rabbitmq.routing-keys.budget}")
    private String budgetRoutingKey;

    @Value("${app.rabbitmq.routing-keys.notification}")
    private String notificationRoutingKey;

    @Override
    public void publishTransactionCreated(TransactionMessage message) {
        log.info("Publishing transaction message: {} for user: {}", message.getId(), message.getUserId());
        rabbitTemplate.convertAndSend(exchangeName, transactionRoutingKey, message);
    }

    @Override
    public void publishBudgetAlert(BudgetAlertMessage message) {
        log.info("Publishing budget alert message: {} for user: {}, percentage: {}%",
                message.getId(), message.getUserId(), message.getPercentageUsed());
        rabbitTemplate.convertAndSend(exchangeName, budgetRoutingKey, message);
    }

    @Override
    public void publishNotification(NotificationMessage message) {
        log.info("Publishing notification message: {} for user: {}", message.getId(), message.getUserId());
        rabbitTemplate.convertAndSend(exchangeName, notificationRoutingKey, message);
    }
}
