package com.dev.expense_manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBalanceResponse {
    private Long id;
    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal openingBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal closingBalance;
    private BigDecimal currentBalance;
    private LocalDateTime createdAt;
}
