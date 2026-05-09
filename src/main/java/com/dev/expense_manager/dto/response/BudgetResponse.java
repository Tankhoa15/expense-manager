package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.BudgetPeriod;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {

    private String id;
    private BigDecimal amount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private int percentageUsed;
    private BudgetPeriod period;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String categoryId;
    private String categoryName;
    private boolean isActive;
    private Integer alertThreshold;
    private boolean isAlertTriggered;
    private LocalDateTime createdAt;
}
