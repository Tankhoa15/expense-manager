package com.dev.expense_manager.service;

import com.dev.expense_manager.dto.request.TransactionRequest;
import com.dev.expense_manager.dto.response.TransactionResponse;
import com.dev.expense_manager.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {

    TransactionResponse create(String userId, TransactionRequest request);

    TransactionResponse getById(String userId, String transactionId);

    Page<TransactionResponse> getAllByUserId(String userId, Pageable pageable);

    Page<TransactionResponse> getPendingByUserId(String userId, Pageable pageable);

    List<TransactionResponse> getByDateRange(String userId, LocalDate startDate, LocalDate endDate);

    TransactionResponse update(String userId, String transactionId, TransactionRequest request);

    void delete(String userId, String transactionId);

    void confirm(String userId, String transactionId);

    void cancel(String userId, String transactionId);

    BigDecimal getTotalByTypeAndDateRange(String userId, TransactionType type,
                                          LocalDate startDate, LocalDate endDate);
}
