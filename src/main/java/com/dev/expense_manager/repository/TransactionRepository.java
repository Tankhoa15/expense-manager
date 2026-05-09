package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByUserIdAndIsDeletedFalseOrderByTransactionDateDescCreatedAtDesc(String userId, Pageable pageable);

    List<Transaction> findByUserIdAndTransactionDateBetweenAndIsDeletedFalse(String userId, LocalDate startDate, LocalDate endDate);

    Optional<Transaction> findByIdAndUserId(String id, String userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'INCOME' AND t.isDeleted = false AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateRange(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
