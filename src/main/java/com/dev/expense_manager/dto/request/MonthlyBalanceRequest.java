package com.dev.expense_manager.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBalanceRequest {

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be >= 2000")
    private Integer year;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Opening balance is required")
    @DecimalMin(value = "0", message = "Opening balance cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal openingBalance;
}
