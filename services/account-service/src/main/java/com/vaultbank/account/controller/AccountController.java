package com.vaultbank.account.controller;

import com.vaultbank.account.dto.request.CreateAccountRequest;
import com.vaultbank.account.dto.request.LedgerEntryRequest;
import com.vaultbank.account.dto.response.AccountResponse;
import com.vaultbank.account.dto.response.ApiResponse;
import com.vaultbank.account.dto.response.LedgerEntryResponse;
import com.vaultbank.account.security.JwtService;
import com.vaultbank.account.service.AccountService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Gerenciamento de contas bancarias")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;
    private final JwtService jwtService;

    @PostMapping
    @Operation(summary = "Criar conta bancaria")
    public ResponseEntity<ApiResponse<AccountResponse>> create(
            @Valid @RequestBody CreateAccountRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        AccountResponse account = accountService.createAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Conta criada com sucesso", account));
    }

    @GetMapping
    @Operation(summary = "Listar minhas contas")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> listMyAccounts(HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Contas encontradas", accountService.getAccountsByUser(userId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Conta encontrada", accountService.getAccount(id, userId)));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Consultar saldo")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        accountService.getAccount(id, userId); // valida ownership
        BigDecimal balance = accountService.getBalance(id);
        return ResponseEntity.ok(ApiResponse.ok("Saldo consultado", balance));
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Bloquear conta")
    public ResponseEntity<ApiResponse<AccountResponse>> blockAccount(
            @PathVariable Long id,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Conta bloqueada", accountService.blockAccount(id, userId)));
    }

    @GetMapping("/{id}/ledger")
    @Operation(summary = "Historico de movimentacoes")
    public ResponseEntity<ApiResponse<Page<LedgerEntryResponse>>> getLedger(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Historico", accountService.getLedgerHistory(id, userId, pageable)));
    }

    // Endpoint interno — chamado pelo transaction-service
    @PostMapping("/internal/ledger")
    @Operation(summary = "Adicionar entrada no ledger (uso interno)")
    public ResponseEntity<ApiResponse<LedgerEntryResponse>> addLedgerEntry(
            @Valid @RequestBody LedgerEntryRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Entrada registrada", accountService.addLedgerEntry(request)));
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.extractUserId(token);
    }
}
