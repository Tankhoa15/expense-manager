package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.constant.CacheKeyConstants;
import com.dev.expense_manager.dto.request.TransactionRequest;
import com.dev.expense_manager.dto.response.TransactionResponse;
import com.dev.expense_manager.entity.*;
import com.dev.expense_manager.exception.BadRequestException;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.mapper.TransactionMapper;
import com.dev.expense_manager.repository.CategoryRepository;
import com.dev.expense_manager.repository.MoneySourceRepository;
import com.dev.expense_manager.repository.TransactionRepository;
import com.dev.expense_manager.repository.UserRepository;
import com.dev.expense_manager.service.CacheService;
import com.dev.expense_manager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MoneySourceRepository moneySourceRepository;
    private final TransactionMapper transactionMapper;
    private final CacheService cacheService;

    @Override
    @Transactional
    public TransactionResponse create(String userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        MoneySource moneySource = null;
        if (request.getMoneySourceId() != null && !request.getMoneySourceId().isBlank()) {
            moneySource = moneySourceRepository.findByIdAndUserIdAndIsDeletedFalse(request.getMoneySourceId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("MoneySource", "id", request.getMoneySourceId()));
        }

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .status(TransactionStatus.PENDING)
                .transactionDate(request.getTransactionDate())
                .note(request.getNote())
                .description(request.getDescription())
                .user(user)
                .category(category)
                .moneySource(moneySource)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        evictCache(userId);
        return transactionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId) && !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllByUserId(String userId, Pageable pageable) {
        return transactionRepository.findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(userId, pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getPendingByUserId(String userId, Pageable pageable) {
        return transactionRepository.findByUserIdAndStatusAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
                userId, TransactionStatus.PENDING, pageable)
                .map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetweenAndIsDeletedFalse(userId, startDate, endDate)
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TransactionResponse update(String userId, String transactionId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId) && !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getStatus() == TransactionStatus.CONFIRMED) {
            throw new BadRequestException("Cannot edit a confirmed transaction");
        }

        Category category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        MoneySource moneySource = null;
        if (request.getMoneySourceId() != null && !request.getMoneySourceId().isBlank()) {
            moneySource = moneySourceRepository.findByIdAndUserIdAndIsDeletedFalse(request.getMoneySourceId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("MoneySource", "id", request.getMoneySourceId()));
        }

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNote(request.getNote());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);
        transaction.setMoneySource(moneySource);

        Transaction updated = transactionRepository.save(transaction);
        evictCache(userId);
        return transactionMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void confirm(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId) && !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new BadRequestException("Only PENDING transactions can be confirmed");
        }

        if (transaction.getMoneySource() != null) {
            MoneySource source = transaction.getMoneySource();
            if (transaction.getType() == TransactionType.INCOME) {
                source.setCurrentBalance(source.getCurrentBalance().add(transaction.getAmount()));
            } else {
                source.setCurrentBalance(source.getCurrentBalance().subtract(transaction.getAmount()));
            }
            moneySourceRepository.save(source);
        }

        transaction.setStatus(TransactionStatus.CONFIRMED);
        transactionRepository.save(transaction);
        evictCache(userId);
    }

    @Override
    @Transactional
    public void cancel(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId) && !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new BadRequestException("Only PENDING transactions can be cancelled");
        }

        transaction.setStatus(TransactionStatus.CANCELLED);
        transactionRepository.save(transaction);
        evictCache(userId);
    }

    @Override
    @Transactional
    public void delete(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId) && !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (transaction.getStatus() == TransactionStatus.CONFIRMED && transaction.getMoneySource() != null) {
            MoneySource source = transaction.getMoneySource();
            if (transaction.getType() == TransactionType.INCOME) {
                source.setCurrentBalance(source.getCurrentBalance().subtract(transaction.getAmount()));
            } else {
                source.setCurrentBalance(source.getCurrentBalance().add(transaction.getAmount()));
            }
            moneySourceRepository.save(source);
        }

        transaction.setDeleted(true);
        transactionRepository.save(transaction);
        evictCache(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalByTypeAndDateRange(String userId, TransactionType type, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, type, startDate, endDate);
    }

    private void evictCache(String userId) {
        cacheService.evictPattern(CacheKeyConstants.dashboardKey(userId));
        cacheService.evictPattern(CacheKeyConstants.statisticsKey(userId));
    }
}
