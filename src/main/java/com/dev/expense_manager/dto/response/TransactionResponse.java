package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.TransactionStatus;
import com.dev.expense_manager.entity.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private String id;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDate transactionDate;
    private String note;
    private String description;
    private String categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private String moneySourceId;
    private String moneySourceName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
