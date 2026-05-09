package com.dev.expense_manager.dto.request;

import com.dev.expense_manager.entity.MoneySource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MoneySourceRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Source type is required")
    private MoneySource.SourceType sourceType;

    @PositiveOrZero(message = "Initial balance must be zero or positive")
    private BigDecimal initialBalance = BigDecimal.ZERO;
}
