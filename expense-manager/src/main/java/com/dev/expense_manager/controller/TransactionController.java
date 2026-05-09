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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse transaction = transactionService.create(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created (pending confirmation)", transaction));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<TransactionResponse>> confirm(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        TransactionResponse transaction = transactionService.confirm(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction confirmed and balance updated", transaction));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<TransactionResponse>> cancel(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        TransactionResponse transaction = transactionService.cancel(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction cancelled", transaction));
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

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getByDateRange(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        List<TransactionResponse> transactions = transactionService.getByDateRange(
                userPrincipal.getId(), startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        TransactionResponse transaction = transactionService.getById(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        transactionService.delete(userPrincipal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted", null));
    }
}
