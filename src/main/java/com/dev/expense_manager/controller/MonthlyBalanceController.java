package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.request.MonthlyBalanceRequest;
import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.MonthlyBalanceResponse;
import com.dev.expense_manager.security.UserPrincipal;
import com.dev.expense_manager.service.MonthlyBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monthly-balances")
@RequiredArgsConstructor
public class MonthlyBalanceController {

    private final MonthlyBalanceService monthlyBalanceService;

    @PostMapping
    public ResponseEntity<ApiResponse<MonthlyBalanceResponse>> createOrUpdate(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody MonthlyBalanceRequest request) {
        MonthlyBalanceResponse response = monthlyBalanceService.createOrUpdate(userPrincipal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Monthly balance saved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MonthlyBalanceResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<MonthlyBalanceResponse> balances = monthlyBalanceService.getAllByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(balances));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<MonthlyBalanceResponse>> getCurrent(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        MonthlyBalanceResponse balance = monthlyBalanceService.getCurrent(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(balance));
    }
}
