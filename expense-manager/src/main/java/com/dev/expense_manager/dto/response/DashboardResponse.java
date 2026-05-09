package com.dev.expense_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private BigDecimal totalBalance;
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal monthBalance;
    private BigDecimal pendingAmount;
    private List<CategorySummary> categoryBreakdown;
    private List<TransactionResponse> recentTransactions;
    private List<MoneySourceResponse> moneySources;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private Long categoryId;
        private String categoryName;
        private String icon;
        private String color;
        private BigDecimal totalAmount;
        private BigDecimal percentage;
    }
}
