package com.dev.expense_manager.dto.request;

import com.dev.expense_manager.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 50, message = "Category name must be between 1 and 50 characters")
    private String name;

    @Size(max = 50, message = "Icon cannot exceed 50 characters")
    private String icon;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color format (use hex format)")
    private String color;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;
}
