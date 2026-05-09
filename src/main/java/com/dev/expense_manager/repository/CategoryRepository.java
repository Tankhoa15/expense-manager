package com.dev.expense_manager.repository;

import com.dev.expense_manager.entity.Category;
import com.dev.expense_manager.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByUserIdAndIsDeletedFalse(String userId);

    List<Category> findByUserIdAndTypeAndIsDeletedFalse(String userId, TransactionType type);

    Optional<Category> findByIdAndUserIdAndIsDeletedFalse(String id, String userId);

    Optional<Category> findByUserIdAndNameAndIsDeletedFalse(String userId, String name);

    boolean existsByUserIdAndNameAndIsDeletedFalse(String userId, String name);

    List<Category> findByIsDefaultTrueAndIsDeletedFalse();
}
