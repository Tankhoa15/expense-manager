package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.TransactionRequest;
import com.dev.expense_manager.dto.response.DashboardResponse;
import com.dev.expense_manager.dto.response.DashboardResponse.CategorySummary;
import com.dev.expense_manager.dto.response.TransactionResponse;
import com.dev.expense_manager.entity.Category;
import com.dev.expense_manager.entity.Transaction;
import com.dev.expense_manager.entity.TransactionType;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.mapper.TransactionMapper;
import com.dev.expense_manager.message.TransactionMessage;
import com.dev.expense_manager.repository.CategoryRepository;
import com.dev.expense_manager.repository.TransactionRepository;
import com.dev.expense_manager.repository.UserRepository;
import com.dev.expense_manager.service.CacheService;
import com.dev.expense_manager.service.MessagePublisher;
import com.dev.expense_manager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final MessagePublisher messagePublisher;
    private final CacheService cacheService;

    @Override
    @Transactional
    public TransactionResponse create(String userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .transactionDate(request.getTransactionDate())
                .note(request.getNote())
                .description(request.getDescription())
                .user(user)
                .category(category)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        publishTransactionMessage(savedTransaction, user, "CREATED");

        // Invalidate cache
        cacheService.evictPattern("dashboard:" + userId);
        cacheService.evictPattern("statistics:" + userId);

        return transactionMapper.toResponse(savedTransaction);
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
        return transactionRepository.findByUserIdAndIsDeletedFalseOrderByTransactionDateDesc(userId, pageable)
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

        Category category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        User user = transaction.getUser();
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNote(request.getNote());
        transaction.setDescription(request.getDescription());
        transaction.setCategory(category);

        Transaction updatedTransaction = transactionRepository.save(transaction);

        publishTransactionMessage(updatedTransaction, user, "UPDATED");

        // Invalidate cache
        cacheService.evictPattern("dashboard:" + userId);
        cacheService.evictPattern("statistics:" + userId);

        return transactionMapper.toResponse(updatedTransaction);
    }

    @Override
    @Transactional
    public void delete(String userId, String transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId) && !t.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        User user = transaction.getUser();
        publishTransactionMessage(transaction, user, "DELETED");

        transaction.setDeleted(true);
        transactionRepository.save(transaction);

        // Invalidate cache
        cacheService.evictPattern("dashboard:" + userId);
        cacheService.evictPattern("statistics:" + userId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalByTypeAndDateRange(String userId, TransactionType type, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, type, startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(userId, TransactionType.EXPENSE, startDate, endDate);

        List<Object[]> categoryBreakdown = transactionRepository.sumAmountByCategoryAndTypeAndDateRange(userId, TransactionType.EXPENSE, startDate, endDate);

        List<CategorySummary> categorySummaries = new ArrayList<>();
        for (Object[] row : categoryBreakdown) {
            String categoryId = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                BigDecimal percentage = BigDecimal.ZERO;
                if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = amount.multiply(BigDecimal.valueOf(100))
                            .divide(totalExpense, 2, RoundingMode.HALF_UP);
                }

                categorySummaries.add(CategorySummary.builder()
                        .categoryId(categoryId)
                        .categoryName(category.getName())
                        .categoryColor(category.getColor())
                        .totalAmount(amount)
                        .transactionCount(0)
                        .percentage(percentage)
                        .build());
            }
        }

        return DashboardResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .categoryBreakdown(categorySummaries)
                .monthlyTrend(new ArrayList<>())
                .budgetAlerts(new ArrayList<>())
                .build();
    }

    private void publishTransactionMessage(Transaction transaction, User user, String eventType) {
        try {
            TransactionMessage message = TransactionMessage.builder()
                    .id(transaction.getId())
                    .userId(user.getId())
                    .email(user.getEmail())
                    .amount(transaction.getAmount())
                    .type(transaction.getType())
                    .description(transaction.getDescription())
                    .categoryId(transaction.getCategory() != null ? transaction.getCategory().getId() : null)
                    .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : null)
                    .transactionDate(transaction.getTransactionDate())
                    .createdAt(transaction.getCreatedAt())
                    .eventType(eventType)
                    .build();

            messagePublisher.publishTransactionCreated(message);
        } catch (Exception e) {
            // Log error but don't fail the main transaction
            System.err.println("Failed to publish transaction message: " + e.getMessage());
        }
    }
}
