package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.CategoryRequest;
import com.dev.expense_manager.dto.response.CategoryResponse;
import com.dev.expense_manager.entity.TransactionType;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(String userId, CategoryRequest request);

    CategoryResponse getById(String userId, String categoryId);

    List<CategoryResponse> getAllByUserId(String userId);

    List<CategoryResponse> getByType(String userId, TransactionType type);

    CategoryResponse update(String userId, String categoryId, CategoryRequest request);

    void delete(String userId, String categoryId);

    void seedDefaultCategories(String userId);
}
