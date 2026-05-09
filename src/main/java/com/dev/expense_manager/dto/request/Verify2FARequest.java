package com.dev.expense_manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Verify2FARequest {
    @NotBlank(message = "Code is required")
    private String code;
    
    private boolean isBackupCode;
}
