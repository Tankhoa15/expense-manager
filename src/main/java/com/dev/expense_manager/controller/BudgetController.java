package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.request.BudgetRequest;
import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.BudgetAlertResponse;
import com.dev.expense_manager.dto.response.BudgetResponse;
import com.dev.expense_manager.security.UserPrincipal;
import com.dev.expense_manager.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse budget = budgetService.create(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", budget));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<BudgetResponse> budgets = budgetService.getAllByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        BudgetResponse budget = budgetService.getById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(budget));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<BudgetAlertResponse>>> getBudgetAlerts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<BudgetAlertResponse> alerts = budgetService.getBudgetAlerts(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(alerts));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse budget = budgetService.update(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", budget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        budgetService.delete(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }

    @PostMapping("/recalculate")
    public ResponseEntity<ApiResponse<Void>> recalculateBudgets(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        budgetService.recalculateBudgetSpending(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Budgets recalculated", null));
    }
}
