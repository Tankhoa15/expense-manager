package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.CategoryRequest;
import com.dev.expense_manager.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(Long userId, CategoryRequest request);
    List<CategoryResponse> getAllByUserId(Long userId);
    List<CategoryResponse> getByType(Long userId, String type);
    CategoryResponse getById(Long userId, Long id);
    CategoryResponse update(Long userId, Long id, CategoryRequest request);
    void delete(Long userId, Long id);
}
