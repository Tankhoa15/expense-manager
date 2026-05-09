package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private String moneySourceName;
    private Long moneySourceId;
    private String categoryName;
    private Long categoryId;
    private String categoryIcon;
    private String categoryColor;
    private Transaction.TransactionStatus status;
    private LocalDateTime confirmedAt;
    private String note;
    private LocalDateTime createdAt;
}
