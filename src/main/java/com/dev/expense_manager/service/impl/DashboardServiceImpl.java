package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.response.DashboardSummaryResponse;
import com.dev.expense_manager.dto.response.DashboardSummaryResponse.*;
import com.dev.expense_manager.entity.MoneySource;
import com.dev.expense_manager.entity.Transaction;
import com.dev.expense_manager.entity.TransactionType;
import com.dev.expense_manager.repository.CategoryRepository;
import com.dev.expense_manager.repository.MoneySourceRepository;
import com.dev.expense_manager.repository.TransactionRepository;
import com.dev.expense_manager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
public class DashboardServiceImpl implements DashboardService {

    private final MoneySourceRepository moneySourceRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboard(String userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        // Totals
        BigDecimal totalBalance = moneySourceRepository.sumCurrentBalanceByUserId(userId);
        BigDecimal monthIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, monthStart, today);
        BigDecimal monthExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, monthStart, today);
        BigDecimal pendingAmount = transactionRepository.sumPendingAmountByUserId(userId);

        if (totalBalance == null) totalBalance = BigDecimal.ZERO;
        if (monthIncome == null) monthIncome = BigDecimal.ZERO;
        if (monthExpense == null) monthExpense = BigDecimal.ZERO;
        if (pendingAmount == null) pendingAmount = BigDecimal.ZERO;

        // Money sources
        List<MoneySourceSummary> moneySources = moneySourceRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .map(s -> MoneySourceSummary.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .sourceType(s.getSourceType())
                        .currentBalance(s.getCurrentBalance())
                        .build())
                .collect(Collectors.toList());

        // Recent transactions (last 5)
        List<RecentTransaction> recentTransactions = transactionRepository
                .findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(userId, PageRequest.of(0, 5))
                .getContent()
                .stream()
                .map(this::toRecentTransaction)
                .collect(Collectors.toList());

        // Category breakdown (this month's expenses)
        List<Object[]> breakdown = transactionRepository.sumAmountByCategoryAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, monthStart, today);

        final BigDecimal finalMonthExpense = monthExpense;
        List<CategoryBreakdown> categoryBreakdown = new ArrayList<>();
        for (Object[] row : breakdown) {
            String categoryId = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            categoryRepository.findById(categoryId).ifPresent(cat -> {
                BigDecimal pct = finalMonthExpense.compareTo(BigDecimal.ZERO) > 0
                        ? amount.multiply(BigDecimal.valueOf(100)).divide(finalMonthExpense, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
                categoryBreakdown.add(CategoryBreakdown.builder()
                        .categoryId(categoryId)
                        .categoryName(cat.getName())
                        .categoryColor(cat.getColor())
                        .icon(cat.getIcon())
                        .totalAmount(amount)
                        .percentage(pct)
                        .build());
            });
        }

        return DashboardSummaryResponse.builder()
                .totalBalance(totalBalance)
                .monthIncome(monthIncome)
                .monthExpense(monthExpense)
                .pendingAmount(pendingAmount)
                .moneySources(moneySources)
                .recentTransactions(recentTransactions)
                .categoryBreakdown(categoryBreakdown)
                .build();
    }

    private RecentTransaction toRecentTransaction(Transaction t) {
        return RecentTransaction.builder()
                .id(t.getId())
                .description(t.getDescription())
                .amount(t.getAmount())
                .type(t.getType())
                .status(t.getStatus() != null ? t.getStatus().name() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .categoryIcon(t.getCategory() != null ? t.getCategory().getIcon() : null)
                .categoryColor(t.getCategory() != null ? t.getCategory().getColor() : null)
                .transactionDate(t.getTransactionDate())
                .build();
    }
}
