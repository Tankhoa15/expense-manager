package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.Budget;
import com.dev.expense_manager.entity.BudgetPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, String> {

    List<Budget> findByUserIdAndIsDeletedFalse(String userId);

    List<Budget> findByUserIdAndIsActiveTrueAndIsDeletedFalse(String userId);

    Optional<Budget> findByIdAndUserIdAndIsDeletedFalse(String id, String userId);

    @Query("SELECT b FROM Budget b " +
           "WHERE b.user.id = :userId " +
           "AND b.category.id = :categoryId " +
           "AND b.isActive = true " +
           "AND b.isDeleted = false " +
           "AND b.periodStart <= :date " +
           "AND b.periodEnd >= :date")
    Optional<Budget> findActiveBudgetByUserAndCategoryAndDate(
            @Param("userId") String userId,
            @Param("categoryId") String categoryId,
            @Param("date") LocalDate date);

    @Query("SELECT b FROM Budget b " +
           "WHERE b.user.id = :userId " +
           "AND b.category IS NULL " +
           "AND b.period = :period " +
           "AND b.isActive = true " +
           "AND b.isDeleted = false " +
           "AND b.periodStart <= :date " +
           "AND b.periodEnd >= :date")
    Optional<Budget> findActiveOverallBudgetByUserAndPeriodAndDate(
            @Param("userId") String userId,
            @Param("period") BudgetPeriod period,
            @Param("date") LocalDate date);

    List<Budget> findByUserIdAndAlertThresholdLessThanEqualAndIsActiveTrueAndIsDeletedFalse(String userId, int threshold);

    @Query("SELECT b FROM Budget b " +
           "WHERE b.user.id = :userId " +
           "AND b.isActive = true " +
           "AND b.isDeleted = false " +
           "AND b.periodStart <= :endDate " +
           "AND b.periodEnd >= :startDate")
    List<Budget> findOverlappingBudgets(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
