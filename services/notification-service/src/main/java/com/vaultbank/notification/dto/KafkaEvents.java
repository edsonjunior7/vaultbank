package com.vaultbank.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ── Evento de Deposito ────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class MoneyDepositedEvent {
    private Long transactionId;
    private Long userId;
    private String accountNumber;
    private BigDecimal amount;
    private String description;
    private LocalDateTime occurredAt;
}

// ── Evento de Saque ───────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class MoneyWithdrawnEvent {
    private Long transactionId;
    private Long userId;
    private String accountNumber;
    private BigDecimal amount;
    private String description;
    private LocalDateTime occurredAt;
}

// ── Evento de Transferencia ───────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class MoneyTransferredEvent {
    private Long transactionId;
    private Long userId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String description;
    private LocalDateTime occurredAt;
}
