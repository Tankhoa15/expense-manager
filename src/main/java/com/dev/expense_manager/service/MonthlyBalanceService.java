package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.MonthlyBalanceRequest;
import com.dev.expense_manager.dto.response.MonthlyBalanceResponse;

import java.util.List;

public interface MonthlyBalanceService {
    MonthlyBalanceResponse createOrUpdate(String userId, MonthlyBalanceRequest request);
    List<MonthlyBalanceResponse> getAllByUserId(String userId);
    MonthlyBalanceResponse getCurrent(String userId);
}
