package com.dev.expense_manager.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBalanceResponse {

    private String id;
    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal openingBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal closingBalance;
    private BigDecimal currentBalance;
}
