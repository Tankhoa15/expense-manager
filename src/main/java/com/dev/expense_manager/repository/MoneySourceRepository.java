package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.MoneySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoneySourceRepository extends JpaRepository<MoneySource, String> {

    List<MoneySource> findByUserIdAndIsDeletedFalse(String userId);

    Optional<MoneySource> findByIdAndUserIdAndIsDeletedFalse(String id, String userId);

    boolean existsByUserIdAndNameAndIsDeletedFalse(String userId, String name);

    @Query("SELECT COALESCE(SUM(m.currentBalance), 0) FROM MoneySource m WHERE m.user.id = :userId AND m.isDeleted = false")
    BigDecimal sumCurrentBalanceByUserId(@Param("userId") String userId);
}
