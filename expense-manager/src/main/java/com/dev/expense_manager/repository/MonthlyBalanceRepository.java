package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.MonthlyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyBalanceRepository extends JpaRepository<MonthlyBalance, Long> {

    Optional<MonthlyBalance> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);

    List<MonthlyBalance> findByUserIdOrderByYearDescMonthDesc(Long userId);

    @Query("SELECT mb FROM MonthlyBalance mb WHERE mb.user.id = :userId ORDER BY mb.year DESC, mb.month DESC")
    List<MonthlyBalance> findLatestByUserId(@Param("userId") Long userId);

    @Query("SELECT mb FROM MonthlyBalance mb WHERE mb.user.id = :userId AND mb.year = :year AND mb.month = :month")
    Optional<MonthlyBalance> findCurrentBalance(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Integer month);
}
