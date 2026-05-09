package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.RegisterRequest;
import com.dev.expense_manager.dto.response.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse getCurrentUser(String userId);

    UserResponse getUserByEmail(String email);

    boolean existsByEmail(String email);
}
