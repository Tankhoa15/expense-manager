package com.dev.expense_manager.dto.request;

import com.dev.expense_manager.entity.MoneySourceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoneySourceRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Source type is required")
    private MoneySourceType sourceType;

    @DecimalMin(value = "0", message = "Initial balance cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    @Builder.Default
    private BigDecimal initialBalance = BigDecimal.ZERO;
}
