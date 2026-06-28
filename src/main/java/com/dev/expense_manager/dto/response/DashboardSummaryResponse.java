package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.MoneySourceType;
import com.dev.expense_manager.entity.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private BigDecimal totalBalance;
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal pendingAmount;
    private List<MoneySourceSummary> moneySources;
    private List<RecentTransaction> recentTransactions;
    private List<CategoryBreakdown> categoryBreakdown;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MoneySourceSummary {
        private String id;
        private String name;
        private MoneySourceType sourceType;
        private BigDecimal currentBalance;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentTransaction {
        private String id;
        private String description;
        private BigDecimal amount;
        private TransactionType type;
        private String status;
        private String categoryName;
        private String categoryIcon;
        private String categoryColor;
        private LocalDate transactionDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryBreakdown {
        private String categoryId;
        private String categoryName;
        private String categoryColor;
        private String icon;
        private BigDecimal totalAmount;
        private BigDecimal percentage;
    }
}
