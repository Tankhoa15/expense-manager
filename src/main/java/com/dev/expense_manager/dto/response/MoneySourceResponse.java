package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.MoneySourceType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoneySourceResponse {

    private String id;
    private String name;
    private MoneySourceType sourceType;
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
