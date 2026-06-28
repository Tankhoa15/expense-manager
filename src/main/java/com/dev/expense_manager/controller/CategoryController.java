package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.request.CategoryRequest;
import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.CategoryResponse;
import com.dev.expense_manager.entity.TransactionType;
import com.dev.expense_manager.security.UserPrincipal;
import com.dev.expense_manager.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.create(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<CategoryResponse> categories = categoryService.getAllByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        CategoryResponse category = categoryService.getById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getByType(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable TransactionType type) {
        List<CategoryResponse> categories = categoryService.getByType(userPrincipal.getId(), type);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.update(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        categoryService.delete(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    @PostMapping("/seed")
    public ResponseEntity<ApiResponse<Void>> seedDefaultCategories(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        categoryService.seedDefaultCategories(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Default categories created", null));
    }
}
