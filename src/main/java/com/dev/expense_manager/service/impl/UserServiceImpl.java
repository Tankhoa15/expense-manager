package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.RegisterRequest;
import com.dev.expense_manager.dto.response.UserResponse;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.DuplicateResourceException;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.mapper.UserMapper;
import com.dev.expense_manager.repository.UserRepository;
import com.dev.expense_manager.service.CategoryService;
import com.dev.expense_manager.service.MoneySourceService;
import com.dev.expense_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;
    private final MoneySourceService moneySourceService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .emailVerified(true)
                .provider("local")
                .build();

        User savedUser = userRepository.save(user);

        // Seed default categories and money source for the new user
        try {
            categoryService.seedDefaultCategories(savedUser.getId());
        } catch (Exception e) {
            System.err.println("Failed to seed categories for user " + savedUser.getId() + ": " + e.getMessage());
        }
        try {
            moneySourceService.createDefaultSource(savedUser.getId());
        } catch (Exception e) {
            System.err.println("Failed to create default money source for user " + savedUser.getId() + ": " + e.getMessage());
        }

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
