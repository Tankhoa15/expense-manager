package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.MoneySource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneySourceResponse {
    private Long id;
    private String name;
    private MoneySource.SourceType sourceType;
    private BigDecimal currentBalance;
    private BigDecimal initialBalance;
    private BigDecimal availableBalance;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
