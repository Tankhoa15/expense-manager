package com.dev.expense_manager.mapper;

import com.dev.expense_manager.dto.response.CategoryResponse;
import com.dev.expense_manager.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .type(category.getType())
                .isDefault(category.isDefault())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
