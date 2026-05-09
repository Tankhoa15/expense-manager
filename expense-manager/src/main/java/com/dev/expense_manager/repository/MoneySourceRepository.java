package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.MoneySource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoneySourceRepository extends JpaRepository<MoneySource, Long> {
    List<MoneySource> findByUserIdAndIsActiveTrue(Long userId);
    List<MoneySource> findByUserId(Long userId);
    Optional<MoneySource> findByIdAndUserId(Long id, Long userId);
}
