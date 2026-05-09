package com.dev.expense_manager.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlertResponse {

    private String budgetId;
    private String categoryName;
    private java.math.BigDecimal amount;
    private java.math.BigDecimal spentAmount;
    private int percentageUsed;
    private int alertThreshold;
    private java.time.LocalDate periodStart;
    private java.time.LocalDate periodEnd;
}
