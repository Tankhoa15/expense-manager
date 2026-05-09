package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.response.TransactionMonitorResponse;

import java.util.List;
import java.util.Map;

public interface TransactionMonitorService {
    TransactionMonitorResponse getOverview(String userId);
    List<Map<String, Object>> getRecentActivity(String userId);
    Map<String, Object> getStatistics(String userId);
    List<Map<String, Object>> getTransactionTrend(String userId, int days);
}
