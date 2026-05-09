package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.TransactionMonitorResponse;
import com.dev.expense_manager.security.UserPrincipal;
import com.dev.expense_manager.service.TransactionMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class TransactionMonitorController {

    private final TransactionMonitorService monitorService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<TransactionMonitorResponse>> getOverview(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        TransactionMonitorResponse overview = monitorService.getOverview(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRecentActivity(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<Map<String, Object>> activities = monitorService.getRecentActivity(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(activities));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Map<String, Object> statistics = monitorService.getStatistics(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTrend(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> trend = monitorService.getTransactionTrend(userPrincipal.getId(), days);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }
}
