package com.dev.expense_manager.dto.response;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private String id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String provider;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
