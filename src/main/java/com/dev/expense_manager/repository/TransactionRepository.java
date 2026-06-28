package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.Transaction;
import com.dev.expense_manager.entity.TransactionStatus;
import com.dev.expense_manager.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
            String userId, Pageable pageable);

    Page<Transaction> findByUserIdAndStatusAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(
            String userId, TransactionStatus status, Pageable pageable);

    List<Transaction> findByUserIdAndTransactionDateBetweenAndIsDeletedFalse(
            String userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type AND t.status = 'CONFIRMED' " +
           "AND t.isDeleted = false AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(
            @Param("userId") String userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.category.id, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type AND t.status = 'CONFIRMED' " +
           "AND t.isDeleted = false AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.id")
    List<Object[]> sumAmountByCategoryAndTypeAndDateRange(
            @Param("userId") String userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.status = 'CONFIRMED' " +
           "AND t.isDeleted = false " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndCategoryIdAndDateRange(
            @Param("userId") String userId,
            @Param("categoryId") String categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.status = 'PENDING' AND t.isDeleted = false")
    BigDecimal sumPendingAmountByUserId(@Param("userId") String userId);
}
