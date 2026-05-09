package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.RegisterRequest;
import com.dev.expense_manager.dto.response.UserResponse;
import com.dev.expense_manager.entity.User;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse getCurrentUser(Long userId);
    User getUserById(Long userId);
}
