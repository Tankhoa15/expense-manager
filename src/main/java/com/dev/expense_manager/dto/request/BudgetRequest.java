package com.dev.expense_manager.dto.request;

import com.dev.expense_manager.entity.BudgetPeriod;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount format is invalid")
    private BigDecimal amount;

    @NotNull(message = "Budget period is required")
    private BudgetPeriod period;

    @NotNull(message = "Period start date is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end date is required")
    private LocalDate periodEnd;

    private String categoryId;

    @Min(value = 1, message = "Alert threshold must be at least 1")
    @Max(value = 100, message = "Alert threshold cannot exceed 100")
    private Integer alertThreshold = 80;
}
