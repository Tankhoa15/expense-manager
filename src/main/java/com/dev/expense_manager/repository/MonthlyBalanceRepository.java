package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.MonthlyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyBalanceRepository extends JpaRepository<MonthlyBalance, String> {

    List<MonthlyBalance> findByUserIdOrderByYearDescMonthDesc(String userId);

    Optional<MonthlyBalance> findByUserIdAndYearAndMonth(String userId, Integer year, Integer month);
}
