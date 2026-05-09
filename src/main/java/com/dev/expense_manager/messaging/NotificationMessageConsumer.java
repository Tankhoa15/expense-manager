package com.dev.expense_manager.messaging;

import com.dev.expense_manager.message.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationMessageConsumer {

    @RabbitListener(queues = "${app.rabbitmq.queues.notification}")
    public void handleNotification(NotificationMessage message) {
        log.info("Received notification: userId={}, type={}, title={}",
                message.getUserId(),
                message.getNotificationType(),
                message.getTitle());

        try {
            sendNotification(message);
        } catch (Exception e) {
            log.error("Error sending notification: {}", message.getId(), e);
            throw e;
        }
    }

    private void sendNotification(NotificationMessage message) {
        switch (message.getNotificationType()) {
            case "BUDGET_ALERT" -> sendBudgetAlertNotification(message);
            case "TRANSACTION_REMINDER" -> sendTransactionReminderNotification(message);
            case "SYSTEM" -> sendSystemNotification(message);
            default -> log.warn("Unknown notification type: {}", message.getNotificationType());
        }
    }

    private void sendBudgetAlertNotification(NotificationMessage message) {
        log.info("Sending BUDGET ALERT notification to user {}: {}",
                message.getUserId(),
                message.getTitle());
    }

    private void sendTransactionReminderNotification(NotificationMessage message) {
        log.info("Sending TRANSACTION REMINDER to user {}: {}",
                message.getUserId(),
                message.getTitle());
    }

    private void sendSystemNotification(NotificationMessage message) {
        log.info("Sending SYSTEM notification to user {}: {}",
                message.getUserId(),
                message.getTitle());
    }
}
