package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.TransactionRequest;
import com.dev.expense_manager.dto.response.DashboardResponse;
import com.dev.expense_manager.dto.response.MoneySourceResponse;
import com.dev.expense_manager.dto.response.TransactionResponse;
import com.dev.expense_manager.entity.*;
import com.dev.expense_manager.exception.BadRequestException;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.repository.*;
import com.dev.expense_manager.service.MoneySourceService;
import com.dev.expense_manager.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final MoneySourceRepository moneySourceRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final MoneySourceService moneySourceService;

    @Override
    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MoneySource moneySource = moneySourceRepository.findByIdAndUserId(request.getMoneySourceId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Money source not found"));

        Category category = categoryRepository.findByIdAndUserId(request.getCategoryId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .moneySource(moneySource)
                .category(category)
                .user(user)
                .status(Transaction.TransactionStatus.PENDING)
                .note(request.getNote())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TransactionResponse confirm(Long userId, Long transactionId) {
        Transaction transaction = findByIdAndUserId(transactionId, userId);

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new BadRequestException("Only pending transactions can be confirmed");
        }

        MoneySource moneySource = transaction.getMoneySource();
        Category category = transaction.getCategory();
        BigDecimal amount = transaction.getAmount();

        // Update money source balance based on transaction type
        if (category.getType() == Category.CategoryType.EXPENSE) {
            if (moneySource.getCurrentBalance().compareTo(amount) < 0) {
                throw new BadRequestException("Insufficient balance in money source. Available: " + moneySource.getCurrentBalance());
            }
            moneySource.setCurrentBalance(moneySource.getCurrentBalance().subtract(amount));
        } else {
            moneySource.setCurrentBalance(moneySource.getCurrentBalance().add(amount));
        }
        moneySourceRepository.save(moneySource);

        // Update transaction status
        transaction.setStatus(Transaction.TransactionStatus.CONFIRMED);
        transaction.setConfirmedAt(java.time.LocalDateTime.now());
        Transaction saved = transactionRepository.save(transaction);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public TransactionResponse cancel(Long userId, Long transactionId) {
        Transaction transaction = findByIdAndUserId(transactionId, userId);

        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new BadRequestException("Only pending transactions can be cancelled");
        }

        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        Transaction saved = transactionRepository.save(transaction);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getPendingByUserId(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdAndStatusOrderByTransactionDateDescCreatedAtDesc(
                        userId, Transaction.TransactionStatus.PENDING, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getByDateRange(Long userId, String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                        userId, start, end)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(Long userId, Long id) {
        Transaction transaction = findByIdAndUserId(id, userId);
        return mapToResponse(transaction);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Transaction transaction = findByIdAndUserId(id, userId);
        if (transaction.getStatus() == Transaction.TransactionStatus.CONFIRMED) {
            throw new BadRequestException("Cannot delete confirmed transactions. Cancel it first.");
        }
        transactionRepository.delete(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(now);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        // Get money sources
        List<MoneySourceResponse> moneySources = moneySourceService.getAllByUserId(userId);
        BigDecimal totalBalance = moneySources.stream()
                .map(MoneySourceResponse::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get monthly totals
        BigDecimal monthIncome = transactionRepository.sumAmountByTypeAndDateRange(
                userId, "INCOME", startOfMonth, endOfMonth);
        BigDecimal monthExpense = transactionRepository.sumAmountByTypeAndDateRange(
                userId, "EXPENSE", startOfMonth, endOfMonth);

        // Get pending amount
        Page<TransactionResponse> pendingPage = getPendingByUserId(userId, Pageable.unpaged());
        BigDecimal pendingAmount = pendingPage.getContent().stream()
                .filter(t -> t.getCategoryName() != null)
                .filter(t -> {
                    // Only count expenses
                    return t.getCategoryName().toLowerCase().contains("expense") ||
                           t.getStatus() == Transaction.TransactionStatus.PENDING;
                })
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get recent transactions
        Page<TransactionResponse> recentPage = getAllByUserId(userId, Pageable.ofSize(5));
        List<TransactionResponse> recentTransactions = recentPage.getContent();

        // Get category breakdown
        List<Object[]> categoryTotals = transactionRepository.sumAmountByCategoryAndDateRange(
                userId, startOfMonth, endOfMonth);
        List<DashboardResponse.CategorySummary> categoryBreakdown = new ArrayList<>();

        for (Object[] row : categoryTotals) {
            Long categoryId = (Long) row[0];
            BigDecimal amount = (BigDecimal) row[1];

            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                BigDecimal percentage = BigDecimal.ZERO;
                if (monthExpense.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = amount.multiply(BigDecimal.valueOf(100))
                            .divide(monthExpense, 2, RoundingMode.HALF_UP);
                }

                categoryBreakdown.add(DashboardResponse.CategorySummary.builder()
                        .categoryId(categoryId)
                        .categoryName(category.getName())
                        .icon(category.getIcon())
                        .color(category.getColor())
                        .totalAmount(amount)
                        .percentage(percentage)
                        .build());
            }
        }

        return DashboardResponse.builder()
                .totalBalance(totalBalance)
                .monthIncome(monthIncome)
                .monthExpense(monthExpense)
                .monthBalance(monthIncome.subtract(monthExpense))
                .pendingAmount(pendingAmount)
                .categoryBreakdown(categoryBreakdown)
                .recentTransactions(recentTransactions)
                .moneySources(moneySources)
                .build();
    }

    private Transaction findByIdAndUserId(Long id, Long userId) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .moneySourceName(transaction.getMoneySource().getName())
                .moneySourceId(transaction.getMoneySource().getId())
                .categoryName(transaction.getCategory().getName())
                .categoryId(transaction.getCategory().getId())
                .categoryIcon(transaction.getCategory().getIcon())
                .categoryColor(transaction.getCategory().getColor())
                .status(transaction.getStatus())
                .confirmedAt(transaction.getConfirmedAt())
                .note(transaction.getNote())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
