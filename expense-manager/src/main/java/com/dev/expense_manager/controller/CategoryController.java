package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.request.CategoryRequest;
import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.CategoryResponse;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.create(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", category));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) String type) {
        List<CategoryResponse> categories;
        if (type != null) {
            categories = categoryService.getByType(userPrincipal.getId(), type);
        } else {
            categories = categoryService.getAllByUserId(userPrincipal.getId());
        }
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        CategoryResponse category = categoryService.getById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse category = categoryService.update(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated", category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        categoryService.delete(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
