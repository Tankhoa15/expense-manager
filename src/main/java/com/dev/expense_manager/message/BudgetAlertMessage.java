package com.dev.expense_manager.message;

import com.dev.expense_manager.entity.BudgetPeriod;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAlertMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String email;
    private String categoryId;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private int percentageUsed;
    private BudgetPeriod period;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String alertType; // WARNING, EXCEEDED
}
