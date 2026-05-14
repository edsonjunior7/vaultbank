package com.vaultbank.transaction.controller;

import com.vaultbank.transaction.dto.request.DepositWithdrawRequest;
import com.vaultbank.transaction.dto.request.TransferRequest;
import com.vaultbank.transaction.dto.response.ApiResponse;
import com.vaultbank.transaction.dto.response.TransactionResponse;
import com.vaultbank.transaction.security.JwtService;
import com.vaultbank.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Operacoes financeiras")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtService jwtService;

    @PostMapping("/transfer")
    @Operation(summary = "Transferencia entre contas")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        TransactionResponse result = transactionService.transfer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transferencia realizada", result));
    }

    @PostMapping("/deposit")
    @Operation(summary = "Depositar na conta")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody DepositWithdrawRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        TransactionResponse result = transactionService.deposit(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Deposito realizado", result));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Saque da conta")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody DepositWithdrawRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        TransactionResponse result = transactionService.withdraw(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Saque realizado", result));
    }

    @GetMapping("/history")
    @Operation(summary = "Historico de transacoes")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> history(
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Historico", transactionService.getHistory(userId, pageable)));
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.extractUserId(token);
    }
}
