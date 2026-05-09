package com.dev.expense_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionMonitorResponse {
    private long todayCount;
    private BigDecimal todayIncome;
    private BigDecimal todayExpense;

    private long weekCount;
    private BigDecimal weekIncome;
    private BigDecimal weekExpense;

    private long monthCount;
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;

    private long todayPending;
    private long todayConfirmed;
    private long todayCancelled;
    private long totalPending;
}
