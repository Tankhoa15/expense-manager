package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.TransactionType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private String id;
    private java.math.BigDecimal amount;
    private TransactionType type;
    private LocalDate transactionDate;
    private String note;
    private String description;
    private String categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
