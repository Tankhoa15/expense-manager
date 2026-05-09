package com.dev.expense_manager.service;

import com.dev.expense_manager.message.BudgetAlertMessage;
import com.dev.expense_manager.message.NotificationMessage;
import com.dev.expense_manager.message.TransactionMessage;

public interface MessagePublisher {

    void publishTransactionCreated(TransactionMessage message);

    void publishBudgetAlert(BudgetAlertMessage message);

    void publishNotification(NotificationMessage message);
}
