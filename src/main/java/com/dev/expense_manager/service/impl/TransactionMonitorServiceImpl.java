package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.constant.CacheKeyConstants;
import com.dev.expense_manager.dto.response.TransactionMonitorResponse;
import com.dev.expense_manager.entity.Transaction;
import com.dev.expense_manager.entity.TransactionType;
import com.dev.expense_manager.repository.TransactionRepository;
import com.dev.expense_manager.service.CacheService;
import com.dev.expense_manager.service.TransactionMonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionMonitorServiceImpl implements TransactionMonitorService {

    private final TransactionRepository transactionRepository;
    private final CacheService cacheService;

    @Override
    @Transactional(readOnly = true)
    public TransactionMonitorResponse getOverview(String userId) {
        String cacheKey = CacheKeyConstants.dashboardOverviewKey(userId);

        return cacheService.get(cacheKey, TransactionMonitorResponse.class, () -> {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate tomorrow = today.plusDays(1);

            List<Transaction> todayTransactions = transactionRepository
                    .findByUserIdAndTransactionDateBetweenAndIsDeletedFalse(userId, today, tomorrow);
            List<Transaction> weekTransactions = transactionRepository
                    .findByUserIdAndTransactionDateBetweenAndIsDeletedFalse(userId, weekAgo, tomorrow);
            List<Transaction> monthTransactions = transactionRepository
                    .findByUserIdAndTransactionDateBetweenAndIsDeletedFalse(userId, monthStart, tomorrow);

            BigDecimal todayIncome = calculateIncome(todayTransactions);
            BigDecimal todayExpense = calculateExpense(todayTransactions);
            BigDecimal weekIncome = calculateIncome(weekTransactions);
            BigDecimal weekExpense = calculateExpense(weekTransactions);
            BigDecimal monthIncome = calculateIncome(monthTransactions);
            BigDecimal monthExpense = calculateExpense(monthTransactions);

            return TransactionMonitorResponse.builder()
                    .todayCount(todayTransactions.size())
                    .todayIncome(todayIncome)
                    .todayExpense(todayExpense)
                    .weekCount(weekTransactions.size())
                    .weekIncome(weekIncome)
                    .weekExpense(weekExpense)
                    .monthCount(monthTransactions.size())
                    .monthIncome(monthIncome)
                    .monthExpense(monthExpense)
                    .totalPending(0L)
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecentActivity(String userId) {
        String cacheKey = CacheKeyConstants.dashboardRecentKey(userId);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cached = (List<Map<String, Object>>) cacheService.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        Page<Transaction> transactions = transactionRepository
                .findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(userId, PageRequest.of(0, 50));

        List<Map<String, Object>> result = transactions.getContent().stream()
                .map(this::mapToActivity)
                .collect(Collectors.toList());

        cacheService.put(cacheKey, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<String, Object> getStatistics(String userId) {
        String cacheKey = CacheKeyConstants.statisticsKey(userId);

        Map<String, Object> cached = cacheService.get(cacheKey, Map.class);
        if (cached != null) {
            return cached;
        }

        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate tomorrow = today.plusDays(1);

        List<Transaction> monthTransactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenAndIsDeletedFalse(userId, monthStart, tomorrow);

        Map<String, BigDecimal> categoryExpense = monthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Uncategorized",
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        Map<String, BigDecimal> categoryIncome = monthTransactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Uncategorized",
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        BigDecimal totalExpense = categoryExpense.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryPercentages = new HashMap<>();
        for (Map.Entry<String, BigDecimal> entry : categoryExpense.entrySet()) {
            BigDecimal percentage = totalExpense.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().multiply(BigDecimal.valueOf(100))
                            .divide(totalExpense, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            categoryPercentages.put(entry.getKey(), percentage);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("categoryExpense", categoryExpense);
        stats.put("categoryIncome", categoryIncome);
        stats.put("categoryPercentages", categoryPercentages);
        stats.put("averageTransaction", monthTransactions.isEmpty() ? BigDecimal.ZERO :
                totalExpense.divide(BigDecimal.valueOf(monthTransactions.size()), 2, RoundingMode.HALF_UP));
        stats.put("largestTransaction", monthTransactions.stream()
                .map(Transaction::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO));
        stats.put("totalTransactions", monthTransactions.size());

        cacheService.put(cacheKey, stats);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTransactionTrend(String userId, int days) {
        String cacheKey = CacheKeyConstants.dashboardTrendKey(userId, days);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cached = (List<Map<String, Object>>) cacheService.get(cacheKey, List.class);
        if (cached != null) {
            return cached;
        }

        LocalDate today = LocalDate.now();
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDate nextDate = date.plusDays(1);

            List<Transaction> dayTransactions = transactionRepository
                    .findByUserIdAndTransactionDateBetweenAndIsDeletedFalse(userId, date, nextDate);

            BigDecimal income = calculateIncome(dayTransactions);
            BigDecimal expense = calculateExpense(dayTransactions);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("income", income);
            dayData.put("expense", expense);
            dayData.put("count", dayTransactions.size());
            trend.add(dayData);
        }

        cacheService.put(cacheKey, trend);
        return trend;
    }

    private BigDecimal calculateIncome(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateExpense(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, Object> mapToActivity(Transaction t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("description", t.getDescription());
        map.put("amount", t.getAmount());
        map.put("type", t.getType().name());
        map.put("categoryName", t.getCategory() != null ? t.getCategory().getName() : "N/A");
        map.put("categoryIcon", t.getCategory() != null ? t.getCategory().getIcon() : "");
        map.put("categoryColor", t.getCategory() != null ? t.getCategory().getColor() : "#666666");
        map.put("transactionDate", t.getTransactionDate().toString());
        map.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : "");
        return map;
    }
}
