package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.MoneySourceRequest;
import com.dev.expense_manager.dto.response.MoneySourceResponse;

import java.util.List;

public interface MoneySourceService {
    MoneySourceResponse create(String userId, MoneySourceRequest request);
    MoneySourceResponse getById(String userId, String id);
    List<MoneySourceResponse> getAllByUserId(String userId);
    MoneySourceResponse update(String userId, String id, MoneySourceRequest request);
    void delete(String userId, String id);
    void createDefaultSource(String userId);
}
