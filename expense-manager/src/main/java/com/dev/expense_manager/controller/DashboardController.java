package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.DashboardResponse;
import com.dev.expense_manager.security.UserPrincipal;
import com.dev.expense_manager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DashboardResponse dashboard = transactionService.getDashboard(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
