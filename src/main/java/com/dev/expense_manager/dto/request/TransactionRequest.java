package com.dev.expense_manager.dto.request;

import com.dev.expense_manager.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount format is invalid")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private java.time.LocalDate transactionDate;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    private String moneySourceId;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}
