package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.MonthlyBalanceRequest;
import com.dev.expense_manager.dto.response.MonthlyBalanceResponse;
import com.dev.expense_manager.entity.MonthlyBalance;
import com.dev.expense_manager.entity.Transaction;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.repository.MonthlyBalanceRepository;
import com.dev.expense_manager.repository.TransactionRepository;
import com.dev.expense_manager.service.MonthlyBalanceService;
import com.dev.expense_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlyBalanceServiceImpl implements MonthlyBalanceService {

    private final MonthlyBalanceRepository monthlyBalanceRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Override
    @Transactional
    public MonthlyBalanceResponse createOrUpdate(Long userId, MonthlyBalanceRequest request) {
        User user = userService.getUserById(userId);

        Optional<MonthlyBalance> existing = monthlyBalanceRepository
                .findByUserIdAndYearAndMonth(userId, request.getYear(), request.getMonth());

        MonthlyBalance balance;
        if (existing.isPresent()) {
            balance = existing.get();
            balance.setOpeningBalance(request.getOpeningBalance());
        } else {
            balance = MonthlyBalance.builder()
                    .user(user)
                    .year(request.getYear())
                    .month(request.getMonth())
                    .openingBalance(request.getOpeningBalance())
                    .totalIncome(BigDecimal.ZERO)
                    .totalExpense(BigDecimal.ZERO)
                    .closingBalance(request.getOpeningBalance())
                    .build();
        }

        recalculateBalance(balance, userId);
        MonthlyBalance saved = monthlyBalanceRepository.save(balance);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyBalanceResponse> getAllByUserId(Long userId) {
        return monthlyBalanceRepository.findByUserIdOrderByYearDescMonthDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyBalanceResponse getCurrentBalance(Long userId) {
        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.from(now);

        Optional<MonthlyBalance> balance = monthlyBalanceRepository
                .findByUserIdAndYearAndMonth(userId, now.getYear(), now.getMonthValue());

        if (balance.isEmpty()) {
            Optional<MonthlyBalance> latest = monthlyBalanceRepository.findLatestByUserId(userId).stream().findFirst();
            if (latest.isPresent()) {
                MonthlyBalance prevBalance = latest.get();
                BigDecimal lastClosing = prevBalance.getClosingBalance();
                return MonthlyBalanceResponse.builder()
                        .year(now.getYear())
                        .month(now.getMonthValue())
                        .monthName(now.getMonth().name())
                        .openingBalance(lastClosing)
                        .totalIncome(BigDecimal.ZERO)
                        .totalExpense(BigDecimal.ZERO)
                        .closingBalance(lastClosing)
                        .currentBalance(lastClosing)
                        .build();
            }
            return MonthlyBalanceResponse.builder()
                    .year(now.getYear())
                    .month(now.getMonthValue())
                    .monthName(now.getMonth().name())
                    .openingBalance(BigDecimal.ZERO)
                    .totalIncome(BigDecimal.ZERO)
                    .totalExpense(BigDecimal.ZERO)
                    .closingBalance(BigDecimal.ZERO)
                    .currentBalance(BigDecimal.ZERO)
                    .build();
        }

        return mapToResponse(balance.get());
    }

    private void recalculateBalance(MonthlyBalance balance, Long userId) {
        YearMonth yearMonth = YearMonth.of(balance.getYear(), balance.getMonth());
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal totalIncome = transactionRepository.sumAmountByTypeAndDateRange(
                userId, "INCOME", startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByTypeAndDateRange(
                userId, "EXPENSE", startDate, endDate);

        balance.setTotalIncome(totalIncome);
        balance.setTotalExpense(totalExpense);
        balance.setClosingBalance(balance.getOpeningBalance().add(totalIncome).subtract(totalExpense));
    }

    private MonthlyBalanceResponse mapToResponse(MonthlyBalance balance) {
        return MonthlyBalanceResponse.builder()
                .id(balance.getId())
                .year(balance.getYear())
                .month(balance.getMonth())
                .monthName(Month.of(balance.getMonth()).name())
                .openingBalance(balance.getOpeningBalance())
                .totalIncome(balance.getTotalIncome())
                .totalExpense(balance.getTotalExpense())
                .closingBalance(balance.getClosingBalance())
                .currentBalance(balance.getClosingBalance())
                .createdAt(balance.getCreatedAt())
                .build();
    }
}
