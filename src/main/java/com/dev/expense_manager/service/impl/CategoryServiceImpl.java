package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.CategoryRequest;
import com.dev.expense_manager.dto.response.CategoryResponse;
import com.dev.expense_manager.entity.Category;
import com.dev.expense_manager.entity.TransactionType;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.DuplicateResourceException;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.mapper.CategoryMapper;
import com.dev.expense_manager.repository.CategoryRepository;
import com.dev.expense_manager.repository.UserRepository;
import com.dev.expense_manager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse create(String userId, CategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (categoryRepository.existsByUserIdAndNameAndIsDeletedFalse(userId, request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .color(request.getColor())
                .type(request.getType())
                .user(user)
                .isDefault(false)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(String userId, String categoryId) {
        Category category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllByUserId(String userId) {
        return categoryRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getByType(String userId, TransactionType type) {
        return categoryRepository.findByUserIdAndTypeAndIsDeletedFalse(userId, type)
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse update(String userId, String categoryId, CategoryRequest request) {
        Category category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (!category.getName().equals(request.getName()) &&
            categoryRepository.existsByUserIdAndNameAndIsDeletedFalse(userId, request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }

        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setType(request.getType());

        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void delete(String userId, String categoryId) {
        Category category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void seedDefaultCategories(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Category> defaultCategories = List.of(
            Category.builder().name("Salary").icon("💰").color("#4CAF50").type(TransactionType.INCOME).user(user).isDefault(true).build(),
            Category.builder().name("Investment").icon("📈").color("#2196F3").type(TransactionType.INCOME).user(user).isDefault(true).build(),
            Category.builder().name("Side Income").icon("💵").color("#9C27B0").type(TransactionType.INCOME).user(user).isDefault(true).build(),
            Category.builder().name("Food & Dining").icon("🍽️").color("#FF5722").type(TransactionType.EXPENSE).user(user).isDefault(true).build(),
            Category.builder().name("Transportation").icon("🚗").color("#607D8B").type(TransactionType.EXPENSE).user(user).isDefault(true).build(),
            Category.builder().name("Shopping").icon("🛍️").color("#E91E63").type(TransactionType.EXPENSE).user(user).isDefault(true).build(),
            Category.builder().name("Entertainment").icon("🎮").color("#9C27B0").type(TransactionType.EXPENSE).user(user).isDefault(true).build(),
            Category.builder().name("Bills & Utilities").icon("💡").color("#FFC107").type(TransactionType.EXPENSE).user(user).isDefault(true).build(),
            Category.builder().name("Healthcare").icon("🏥").color("#F44336").type(TransactionType.EXPENSE).user(user).isDefault(true).build(),
            Category.builder().name("Education").icon("📚").color("#3F51B5").type(TransactionType.EXPENSE).user(user).isDefault(true).build()
        );

        categoryRepository.saveAll(defaultCategories);
    }
}
