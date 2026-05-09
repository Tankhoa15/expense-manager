package com.dev.expense_manager.service.impl;

import com.dev.expense_manager.dto.request.MoneySourceRequest;
import com.dev.expense_manager.dto.response.MoneySourceResponse;
import com.dev.expense_manager.entity.MoneySource;
import com.dev.expense_manager.entity.User;
import com.dev.expense_manager.exception.ResourceNotFoundException;
import com.dev.expense_manager.repository.MoneySourceRepository;
import com.dev.expense_manager.service.MoneySourceService;
import com.dev.expense_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MoneySourceServiceImpl implements MoneySourceService {

    private final MoneySourceRepository moneySourceRepository;
    private final UserService userService;

    @Override
    @Transactional
    public MoneySourceResponse create(Long userId, MoneySourceRequest request) {
        User user = userService.getUserById(userId);

        MoneySource source = MoneySource.builder()
                .name(request.getName())
                .sourceType(request.getSourceType())
                .currentBalance(request.getInitialBalance())
                .initialBalance(request.getInitialBalance())
                .isActive(true)
                .user(user)
                .build();

        MoneySource saved = moneySourceRepository.save(source);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneySourceResponse> getAllByUserId(Long userId) {
        return moneySourceRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MoneySourceResponse getById(Long userId, Long id) {
        MoneySource source = findByIdAndUserId(id, userId);
        return mapToResponse(source);
    }

    @Override
    @Transactional
    public MoneySourceResponse update(Long userId, Long id, MoneySourceRequest request) {
        MoneySource source = findByIdAndUserId(id, userId);

        source.setName(request.getName());
        source.setSourceType(request.getSourceType());

        MoneySource saved = moneySourceRepository.save(source);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        MoneySource source = findByIdAndUserId(id, userId);
        source.setIsActive(false);
        moneySourceRepository.save(source);
    }

    private MoneySource findByIdAndUserId(Long id, Long userId) {
        return moneySourceRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Money source not found"));
    }

    private MoneySourceResponse mapToResponse(MoneySource source) {
        return MoneySourceResponse.builder()
                .id(source.getId())
                .name(source.getName())
                .sourceType(source.getSourceType())
                .currentBalance(source.getCurrentBalance())
                .initialBalance(source.getInitialBalance())
                .availableBalance(source.getCurrentBalance())
                .isActive(source.getIsActive())
                .createdAt(source.getCreatedAt())
                .build();
    }
}
