package com.vaultbank.fraud.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_transactions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BlockedTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private Long transactionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "blocked_at", nullable = false)
    @Builder.Default
    private LocalDateTime blockedAt = LocalDateTime.now();
}
