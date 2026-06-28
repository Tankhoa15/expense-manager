package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.BudgetRequest;
import com.dev.expense_manager.dto.response.BudgetAlertResponse;
import com.dev.expense_manager.dto.response.BudgetResponse;
import com.dev.expense_manager.entity.Budget;
import com.dev.expense_manager.entity.Category;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.BadRequestException;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.mapper.BudgetMapper;
import com.dev.expense_manager.repository.BudgetRepository;
import com.dev.expense_manager.repository.CategoryRepository;
import com.dev.expense_manager.repository.TransactionRepository;
import com.dev.expense_manager.repository.UserRepository;
import com.dev.expense_manager.service.BudgetService;
import com.dev.expense_manager.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public BudgetResponse create(String userId, BudgetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BadRequestException("Period end date must be after start date");
        }

        Category category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(request.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        Budget budget = Budget.builder()
                .amount(request.getAmount())
                .spentAmount(BigDecimal.ZERO)
                .period(request.getPeriod())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .user(user)
                .category(category)
                .alertThreshold(request.getAlertThreshold())
                .isActive(true)
                .build();

        Budget savedBudget = budgetRepository.save(budget);
        recalculateBudgetSpendingForBudget(savedBudget);
        return budgetMapper.toResponse(savedBudget);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponse getById(String userId, String budgetId) {
        Budget budget = budgetRepository.findByIdAndUserIdAndIsDeletedFalse(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
        return budgetMapper.toResponse(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllByUserId(String userId) {
        return budgetRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .peek(this::recalculateBudgetSpendingForBudget)
                .map(budgetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetAlertResponse> getBudgetAlerts(String userId) {
        return budgetRepository.findByUserIdAndAlertThresholdLessThanEqualAndIsActiveTrueAndIsDeletedFalse(
                        userId, 100)
                .stream()
                .peek(this::recalculateBudgetSpendingForBudget)
                .filter(budget -> budget.getPercentageUsed() >= budget.getAlertThreshold())
                .map(budget -> BudgetAlertResponse.builder()
                        .budgetId(budget.getId())
                        .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : "Overall")
                        .amount(budget.getAmount())
                        .spentAmount(budget.getSpentAmount())
                        .percentageUsed(budget.getPercentageUsed())
                        .alertThreshold(budget.getAlertThreshold())
                        .periodStart(budget.getPeriodStart())
                        .periodEnd(budget.getPeriodEnd())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BudgetResponse update(String userId, String budgetId, BudgetRequest request) {
        Budget budget = budgetRepository.findByIdAndUserIdAndIsDeletedFalse(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));

        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BadRequestException("Period end date must be after start date");
        }

        Category category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            category = categoryRepository.findByIdAndUserIdAndIsDeletedFalse(request.getCategoryId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        budget.setAmount(request.getAmount());
        budget.setPeriod(request.getPeriod());
        budget.setPeriodStart(request.getPeriodStart());
        budget.setPeriodEnd(request.getPeriodEnd());
        budget.setCategory(category);
        budget.setAlertThreshold(request.getAlertThreshold());

        Budget updatedBudget = budgetRepository.save(budget);
        recalculateBudgetSpendingForBudget(updatedBudget);
        return budgetMapper.toResponse(updatedBudget);
    }

    @Override
    @Transactional
    public void delete(String userId, String budgetId) {
        Budget budget = budgetRepository.findByIdAndUserIdAndIsDeletedFalse(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", budgetId));
        budget.setDeleted(true);
        budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public void recalculateBudgetSpending(String userId) {
        List<Budget> budgets = budgetRepository.findByUserIdAndIsActiveTrueAndIsDeletedFalse(userId);
        LocalDate today = LocalDate.now();

        for (Budget budget : budgets) {
            if (!today.isBefore(budget.getPeriodStart()) && !today.isAfter(budget.getPeriodEnd())) {
                recalculateBudgetSpendingForBudget(budget);
            }
        }
    }

    private void recalculateBudgetSpendingForBudget(Budget budget) {
        BigDecimal spent = transactionRepository.sumAmountByUserIdAndCategoryIdAndDateRange(
                budget.getUser().getId(),
                budget.getCategory() != null ? budget.getCategory().getId() : null,
                budget.getPeriodStart(),
                budget.getPeriodEnd()
        );

        if (spent == null) {
            spent = BigDecimal.ZERO;
        }

        int previousPercentage = budget.getSpentAmount() != null ? budget.getPercentageUsed() : 0;
        budget.setSpentAmount(spent);
        budgetRepository.save(budget);

        int currentPercentage = budget.getPercentageUsed();
        if (currentPercentage >= budget.getAlertThreshold() && previousPercentage < budget.getAlertThreshold()) {
            publishBudgetAlert(budget);
        }
    }

    private void publishBudgetAlert(Budget budget) {
        try {
            String alertType = budget.getPercentageUsed() >= 100 ? "EXCEEDED" : "WARNING";

            emailService.sendBudgetAlertEmail(
                    budget.getUser(),
                    budget.getCategory() != null ? budget.getCategory().getName() : "Overall",
                    budget.getAmount(),
                    budget.getSpentAmount(),
                    budget.getPercentageUsed(),
                    alertType
            );
        } catch (Exception e) {
            log.error("Failed to send budget alert: {}", e.getMessage());
        }
    }
}
