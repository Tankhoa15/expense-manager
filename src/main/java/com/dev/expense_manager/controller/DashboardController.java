package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.DashboardSummaryResponse;
import com.dev.expense_manager.security.UserPrincipal;
import com.dev.expense_manager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboard(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        DashboardSummaryResponse dashboard = dashboardService.getDashboard(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
