package com.dev.expense_manager.mapper;

import com.dev.expense_manager.dto.response.MoneySourceResponse;
import com.dev.expense_manager.entity.MoneySource;
import org.springframework.stereotype.Component;

@Component
public class MoneySourceMapper {

    public MoneySourceResponse toResponse(MoneySource source) {
        if (source == null) {
            return null;
        }
        return MoneySourceResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .sourceType(source.getSourceType())
                .initialBalance(source.getInitialBalance())
                .currentBalance(source.getCurrentBalance())
                .isDefault(source.isDefault())
                .createdAt(source.getCreatedAt())
                .updatedAt(source.getUpdatedAt())
                .build();
    }
}
