package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.MoneySourceRequest;
import com.dev.expense_manager.dto.response.MoneySourceResponse;
import com.dev.expense_manager.entity.MoneySource;
import com.dev.expense_manager.entity.MoneySourceType;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.DuplicateResourceException;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.mapper.MoneySourceMapper;
import com.dev.expense_manager.repository.MoneySourceRepository;
import com.dev.expense_manager.repository.UserRepository;
import com.dev.expense_manager.service.MoneySourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoneySourceServiceImpl implements MoneySourceService {

    private final MoneySourceRepository moneySourceRepository;
    private final UserRepository userRepository;
    private final MoneySourceMapper moneySourceMapper;

    @Override
    @Transactional
    public MoneySourceResponse create(String userId, MoneySourceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (moneySourceRepository.existsByUserIdAndNameAndIsDeletedFalse(userId, request.getName())) {
            throw new DuplicateResourceException("Money source already exists: " + request.getName());
        }

        MoneySource source = MoneySource.builder()
                .name(request.getName())
                .sourceType(request.getSourceType())
                .initialBalance(request.getInitialBalance())
                .currentBalance(request.getInitialBalance())
                .user(user)
                .isDefault(false)
                .build();

        return moneySourceMapper.toResponse(moneySourceRepository.save(source));
    }

    @Override
    @Transactional(readOnly = true)
    public MoneySourceResponse getById(String userId, String id) {
        MoneySource source = moneySourceRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("MoneySource", "id", id));
        return moneySourceMapper.toResponse(source);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneySourceResponse> getAllByUserId(String userId) {
        return moneySourceRepository.findByUserIdAndIsDeletedFalse(userId)
                .stream()
                .map(moneySourceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MoneySourceResponse update(String userId, String id, MoneySourceRequest request) {
        MoneySource source = moneySourceRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("MoneySource", "id", id));

        if (!source.getName().equals(request.getName()) &&
                moneySourceRepository.existsByUserIdAndNameAndIsDeletedFalse(userId, request.getName())) {
            throw new DuplicateResourceException("Money source already exists: " + request.getName());
        }

        BigDecimal newInitial = request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO;
        BigDecimal balanceDiff = newInitial.subtract(source.getInitialBalance() != null ? source.getInitialBalance() : BigDecimal.ZERO);
        source.setName(request.getName());
        source.setSourceType(request.getSourceType());
        source.setInitialBalance(newInitial);
        source.setCurrentBalance(source.getCurrentBalance().add(balanceDiff != null ? balanceDiff : BigDecimal.ZERO));

        return moneySourceMapper.toResponse(moneySourceRepository.save(source));
    }

    @Override
    @Transactional
    public void delete(String userId, String id) {
        MoneySource source = moneySourceRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("MoneySource", "id", id));
        source.setDeleted(true);
        moneySourceRepository.save(source);
    }

    @Override
    @Transactional
    public void createDefaultSource(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!moneySourceRepository.findByUserIdAndIsDeletedFalse(userId).isEmpty()) {
            return;
        }

        MoneySource defaultSource = MoneySource.builder()
                .name("Cash")
                .sourceType(MoneySourceType.CASH)
                .initialBalance(java.math.BigDecimal.ZERO)
                .currentBalance(java.math.BigDecimal.ZERO)
                .user(user)
                .isDefault(true)
                .build();

        moneySourceRepository.save(defaultSource);
    }
}
