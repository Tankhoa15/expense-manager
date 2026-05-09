package com.dev.expense_manager.service;

import com.dev.expense_manager.entity.User;

import java.math.BigDecimal;

public interface EmailService {
    void sendBudgetAlertEmail(User user, String categoryName, BigDecimal budgetAmount,
                             BigDecimal spentAmount, int percentageUsed, String alertType);
    void send2FACode(String email, String code);
    void sendWelcomeEmail(User user);
}
