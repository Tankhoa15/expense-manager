package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.MonthlyBalanceRequest;
import com.dev.expense_manager.dto.response.MonthlyBalanceResponse;
import com.dev.expense_manager.entity.MonthlyBalance;
import com.dev.expense_manager.entity.TransactionType;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.mapper.MonthlyBalanceMapper;
import com.dev.expense_manager.repository.MonthlyBalanceRepository;
import com.dev.expense_manager.repository.TransactionRepository;
import com.dev.expense_manager.repository.UserRepository;
import com.dev.expense_manager.service.MonthlyBalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonthlyBalanceServiceImpl implements MonthlyBalanceService {

    private final MonthlyBalanceRepository monthlyBalanceRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MonthlyBalanceMapper monthlyBalanceMapper;

    @Override
    @Transactional
    public MonthlyBalanceResponse createOrUpdate(String userId, MonthlyBalanceRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        MonthlyBalance balance = monthlyBalanceRepository
                .findByUserIdAndYearAndMonth(userId, request.getYear(), request.getMonth())
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).get();
                    return MonthlyBalance.builder()
                            .user(user)
                            .year(request.getYear())
                            .month(request.getMonth())
                            .build();
                });

        balance.setOpeningBalance(request.getOpeningBalance());
        MonthlyBalance saved = monthlyBalanceRepository.save(balance);

        BigDecimal[] totals = computeTotals(userId, request.getYear(), request.getMonth());
        return monthlyBalanceMapper.toResponse(saved, totals[0], totals[1]);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyBalanceResponse> getAllByUserId(String userId) {
        return monthlyBalanceRepository.findByUserIdOrderByYearDescMonthDesc(userId)
                .stream()
                .map(balance -> {
                    BigDecimal[] totals = computeTotals(userId, balance.getYear(), balance.getMonth());
                    return monthlyBalanceMapper.toResponse(balance, totals[0], totals[1]);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyBalanceResponse getCurrent(String userId) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        MonthlyBalance balance = monthlyBalanceRepository
                .findByUserIdAndYearAndMonth(userId, year, month)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    return MonthlyBalance.builder()
                            .user(user)
                            .year(year)
                            .month(month)
                            .openingBalance(BigDecimal.ZERO)
                            .build();
                });

        BigDecimal[] totals = computeTotals(userId, year, month);
        return monthlyBalanceMapper.toResponse(balance, totals[0], totals[1]);
    }

    private BigDecimal[] computeTotals(String userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal income = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.INCOME, start, end);
        BigDecimal expense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, start, end);

        return new BigDecimal[]{
                income != null ? income : BigDecimal.ZERO,
                expense != null ? expense : BigDecimal.ZERO
        };
    }
}
