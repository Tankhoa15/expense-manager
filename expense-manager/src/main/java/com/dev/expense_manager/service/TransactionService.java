package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.TransactionRequest;
import com.dev.expense_manager.dto.response.DashboardResponse;
import com.dev.expense_manager.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    TransactionResponse create(Long userId, TransactionRequest request);
    TransactionResponse confirm(Long userId, Long transactionId);
    TransactionResponse cancel(Long userId, Long transactionId);
    Page<TransactionResponse> getAllByUserId(Long userId, Pageable pageable);
    Page<TransactionResponse> getPendingByUserId(Long userId, Pageable pageable);
    List<TransactionResponse> getByDateRange(Long userId, String startDate, String endDate);
    TransactionResponse getById(Long userId, Long id);
    void delete(Long userId, Long id);
    DashboardResponse getDashboard(Long userId);
}
