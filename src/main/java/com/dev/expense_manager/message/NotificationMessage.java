package com.dev.expense_manager.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String email;
    private String title;
    private String content;
    private String notificationType; // BUDGET_ALERT, TRANSACTION_REMINDER, SYSTEM
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
