package com.dev.expense_manager.mapper;

import com.dev.expense_manager.dto.response.BudgetResponse;
import com.dev.expense_manager.entity.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

    public BudgetResponse toResponse(Budget budget) {
        if (budget == null) {
            return null;
        }
        return BudgetResponse.builder()
                .id(budget.getId())
                .amount(budget.getAmount())
                .spentAmount(budget.getSpentAmount())
                .remainingAmount(budget.getRemainingAmount())
                .percentageUsed(budget.getPercentageUsed())
                .period(budget.getPeriod())
                .periodStart(budget.getPeriodStart())
                .periodEnd(budget.getPeriodEnd())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : null)
                .isActive(budget.isActive())
                .alertThreshold(budget.getAlertThreshold())
                .isAlertTriggered(budget.getPercentageUsed() >= budget.getAlertThreshold())
                .createdAt(budget.getCreatedAt())
                .build();
    }
}
