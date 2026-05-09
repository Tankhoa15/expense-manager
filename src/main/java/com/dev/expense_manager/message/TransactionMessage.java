package com.dev.expense_manager.message;

import com.dev.expense_manager.entity.TransactionType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String email;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private String categoryId;
    private String categoryName;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
    private String eventType; // CREATED, UPDATED, DELETED
}
