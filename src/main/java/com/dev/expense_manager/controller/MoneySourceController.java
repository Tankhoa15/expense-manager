package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.request.MoneySourceRequest;
import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.MoneySourceResponse;
import com.dev.expense_manager.security.UserPrincipal;
import com.dev.expense_manager.service.MoneySourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/money-sources")
@RequiredArgsConstructor
public class MoneySourceController {

    private final MoneySourceService moneySourceService;

    @PostMapping
    public ResponseEntity<ApiResponse<MoneySourceResponse>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody MoneySourceRequest request) {
        MoneySourceResponse response = moneySourceService.create(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Money source created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MoneySourceResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<MoneySourceResponse> sources = moneySourceService.getAllByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(sources));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MoneySourceResponse>> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        MoneySourceResponse source = moneySourceService.getById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(source));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MoneySourceResponse>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody MoneySourceRequest request) {
        MoneySourceResponse source = moneySourceService.update(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Money source updated successfully", source));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        moneySourceService.delete(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Money source deleted successfully", null));
    }
}
