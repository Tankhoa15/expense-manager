package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.response.DashboardSummaryResponse;

public interface DashboardService {
    DashboardSummaryResponse getDashboard(String userId);
}
