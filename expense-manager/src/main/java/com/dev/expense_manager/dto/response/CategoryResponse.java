package com.dev.expense_manager.dto.response;

import com.dev.expense_manager.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Category.CategoryType type;
    private String icon;
    private String color;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
