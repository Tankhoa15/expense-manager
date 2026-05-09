package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.BudgetRequest;
import com.dev.expense_manager.dto.response.BudgetResponse;
import com.dev.expense_manager.dto.response.BudgetAlertResponse;

import java.util.List;

public interface BudgetService {

    BudgetResponse create(String userId, BudgetRequest request);

    BudgetResponse getById(String userId, String budgetId);

    List<BudgetResponse> getAllByUserId(String userId);

    List<BudgetAlertResponse> getBudgetAlerts(String userId);

    BudgetResponse update(String userId, String budgetId, BudgetRequest request);

    void delete(String userId, String budgetId);

    void recalculateBudgetSpending(String userId);
}
