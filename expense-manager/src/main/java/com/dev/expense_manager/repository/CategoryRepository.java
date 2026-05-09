package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdAndIsActiveTrue(Long userId);
    List<Category> findByUserIdAndTypeAndIsActiveTrue(Long userId, Category.CategoryType type);
    List<Category> findByUserId(Long userId);
    Optional<Category> findByIdAndUserId(Long id, Long userId);
}
