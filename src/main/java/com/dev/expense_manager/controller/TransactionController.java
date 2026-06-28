package com.dev.expense_manager.controller;

import com.dev.expense_manager.dto.request.TransactionRequest;
import com.dev.expense_manager.dto.response.ApiResponse;
import com.dev.expense_manager.dto.response.TransactionResponse;
import com.dev.expense_manager.security.UserPrincipal;
import com.dev.expense_manager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.create(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully", transaction));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAll(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> transactions = transactionService.getAllByUserId(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getPending(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponse> transactions = transactionService.getPendingByUserId(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        TransactionResponse transaction = transactionService.getById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getByDateRange(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TransactionResponse> transactions = transactionService.getByDateRange(userPrincipal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> update(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.update(userPrincipal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated successfully", transaction));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirm(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        transactionService.confirm(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction confirmed successfully", null));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        transactionService.cancel(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction cancelled successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String id) {
        transactionService.delete(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted successfully", null));
    }
}
