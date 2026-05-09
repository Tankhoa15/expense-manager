package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.MoneySourceRequest;
import com.dev.expense_manager.dto.response.MoneySourceResponse;

import java.util.List;

public interface MoneySourceService {
    MoneySourceResponse create(Long userId, MoneySourceRequest request);
    List<MoneySourceResponse> getAllByUserId(Long userId);
    MoneySourceResponse getById(Long userId, Long id);
    MoneySourceResponse update(Long userId, Long id, MoneySourceRequest request);
    void delete(Long userId, Long id);
}
