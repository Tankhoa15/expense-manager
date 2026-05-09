package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private String id;
    private String name;
    private String icon;
    private String color;
    private TransactionType type;
    private boolean isDefault;
    private LocalDateTime createdAt;
}
