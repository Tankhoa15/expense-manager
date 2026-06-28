package com.dev.expense_manager.mapper;

import com.dev.expense_manager.dto.response.MonthlyBalanceResponse;
import com.dev.expense_manager.entity.MonthlyBalance;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Month;

@Component
public class MonthlyBalanceMapper {

    public MonthlyBalanceResponse toResponse(MonthlyBalance balance, BigDecimal totalIncome, BigDecimal totalExpense) {
        if (balance == null) {
            return null;
        }
        BigDecimal income = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        BigDecimal expense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        BigDecimal closing = balance.getOpeningBalance().add(income).subtract(expense);

        return MonthlyBalanceResponse.builder()
                .id(balance.getId())
                .year(balance.getYear())
                .month(balance.getMonth())
                .monthName(Month.of(balance.getMonth()).name().charAt(0)
                        + Month.of(balance.getMonth()).name().substring(1).toLowerCase())
                .openingBalance(balance.getOpeningBalance())
                .totalIncome(income)
                .totalExpense(expense)
                .closingBalance(closing)
                .currentBalance(closing)
                .build();
    }
}
