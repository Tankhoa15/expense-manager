package com.dev.expense_manager.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private List<CategorySummary> categoryBreakdown;
    private List<MonthlySummary> monthlyTrend;
    private List<BudgetAlertResponse> budgetAlerts;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategorySummary {
        private String categoryId;
        private String categoryName;
        private String categoryColor;
        private BigDecimal totalAmount;
        private int transactionCount;
        private BigDecimal percentage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlySummary {
        private int year;
        private int month;
        private BigDecimal income;
        private BigDecimal expense;
    }
}
