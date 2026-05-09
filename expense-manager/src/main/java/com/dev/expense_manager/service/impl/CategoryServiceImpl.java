package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.CategoryRequest;
import com.dev.expense_manager.dto.response.CategoryResponse;
import com.dev.expense_manager.entity.Category;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.repository.CategoryRepository;
import com.dev.expense_manager.service.CategoryService;
import com.dev.expense_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserService userService;

    @Override
    @Transactional
    public CategoryResponse create(Long userId, CategoryRequest request) {
        User user = userService.getUserById(userId);

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .icon(request.getIcon())
                .color(request.getColor())
                .isActive(true)
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllByUserId(Long userId) {
        return categoryRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getByType(Long userId, String type) {
        Category.CategoryType categoryType = Category.CategoryType.valueOf(type.toUpperCase());
        return categoryRepository.findByUserIdAndTypeAndIsActiveTrue(userId, categoryType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long userId, Long id) {
        Category category = findByIdAndUserId(id, userId);
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long userId, Long id, CategoryRequest request) {
        Category category = findByIdAndUserId(id, userId);

        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Category category = findByIdAndUserId(id, userId);
        category.setIsActive(false);
        categoryRepository.save(category);
    }

    private Category findByIdAndUserId(Long id, Long userId) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
