package com.dev.expense_manager.messaging;

import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.message.BudgetAlertMessage;
import com.dev.expense_manager.repository.UserRepository;
import com.dev.expense_manager.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BudgetMessageConsumer {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @RabbitListener(queues = "${app.rabbitmq.queues.budget}")
    public void handleBudgetAlert(BudgetAlertMessage message) {
        log.info("Received budget alert: userId={}, category={}, percentage={}%, spent={}/{}",
                message.getUserId(),
                message.getCategoryName(),
                message.getPercentageUsed(),
                message.getSpentAmount(),
                message.getBudgetAmount());

        try {
            // Get user for email
            Optional<User> userOpt = userRepository.findById(message.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Send email notification
                emailService.sendBudgetAlertEmail(
                        user,
                        message.getCategoryName(),
                        message.getBudgetAmount(),
                        message.getSpentAmount(),
                        message.getPercentageUsed(),
                        message.getAlertType()
                );

                log.info("Budget alert email sent to user: {}", user.getEmail());
            }

            processBudgetAlert(message);
        } catch (Exception e) {
            log.error("Error processing budget alert: {}", message.getId(), e);
            throw e;
        }
    }

    private void processBudgetAlert(BudgetAlertMessage message) {
        String alertType = message.getAlertType();

        if ("EXCEEDED".equals(alertType)) {
            log.warn("BUDGET EXCEEDED for user {}: {} spent ${} of ${} budget",
                    message.getUserId(),
                    message.getCategoryName(),
                    message.getSpentAmount(),
                    message.getBudgetAmount());
        } else if ("WARNING".equals(alertType)) {
            log.warn("BUDGET WARNING for user {}: {} at {}% (${}/$)",
                    message.getUserId(),
                    message.getCategoryName(),
                    message.getPercentageUsed(),
                    message.getSpentAmount(),
                    message.getBudgetAmount());
        }

        log.info("Budget alert processed for user: {}", message.getUserId());
    }
}
