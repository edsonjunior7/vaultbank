package com.vaultbank.transaction.service;

import com.vaultbank.transaction.client.AccountServiceClient;
import com.vaultbank.transaction.dto.request.DepositWithdrawRequest;
import com.vaultbank.transaction.dto.request.TransferRequest;
import com.vaultbank.transaction.dto.response.TransactionResponse;
import com.vaultbank.transaction.entity.Transaction;
import com.vaultbank.transaction.exception.BusinessException;
import com.vaultbank.transaction.exception.DuplicateTransactionException;
import com.vaultbank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final KafkaEventPublisher kafkaEventPublisher;

    @Transactional
    public TransactionResponse transfer(Long userId, TransferRequest request) {
        var existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            throw new DuplicateTransactionException("Transacao ja processada", toResponse(existing.get()));
        }

        if (request.getSourceAccount().equals(request.getDestinationAccount())) {
            throw new BusinessException("Conta de origem e destino nao podem ser iguais");
        }

        Transaction transaction = Transaction.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .userId(userId)
                .sourceAccount(request.getSourceAccount())
                .destinationAccount(request.getDestinationAccount())
                .amount(request.getAmount())
                .transactionType(Transaction.TransactionType.TRANSFER)
                .description(request.getDescription())
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        transactionRepository.save(transaction);

        try {
            accountServiceClient.addLedgerEntry(request.getSourceAccount(),
                    request.getAmount().negate(), "TRANSFER_OUT",
                    request.getDescription(), transaction.getId().toString());

            accountServiceClient.addLedgerEntry(request.getDestinationAccount(),
                    request.getAmount(), "TRANSFER_IN",
                    request.getDescription(), transaction.getId().toString());

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            // Publicar evento no Kafka
            kafkaEventPublisher.publishTransferEvent(
                    transaction.getId(), userId,
                    request.getSourceAccount(), request.getDestinationAccount(),
                    request.getAmount(), request.getDescription());

            log.info("Transferencia concluida: id={}", transaction.getId());

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new BusinessException("Transferencia falhou: " + e.getMessage());
        }

        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse deposit(Long userId, DepositWithdrawRequest request) {
        var existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            throw new DuplicateTransactionException("Deposito ja processado", toResponse(existing.get()));
        }

        Transaction transaction = Transaction.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .userId(userId)
                .sourceAccount("EXTERNAL")
                .destinationAccount(request.getAccountNumber())
                .amount(request.getAmount())
                .transactionType(Transaction.TransactionType.DEPOSIT)
                .description(request.getDescription() != null ? request.getDescription() : "Deposito")
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        transactionRepository.save(transaction);

        try {
            accountServiceClient.addLedgerEntry(request.getAccountNumber(),
                    request.getAmount(), "DEPOSIT",
                    transaction.getDescription(), transaction.getId().toString());

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            // Publicar evento no Kafka
            kafkaEventPublisher.publishDepositEvent(
                    transaction.getId(), userId,
                    request.getAccountNumber(), request.getAmount(), request.getDescription());

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new BusinessException("Deposito falhou: " + e.getMessage());
        }

        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse withdraw(Long userId, DepositWithdrawRequest request) {
        var existing = transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            throw new DuplicateTransactionException("Saque ja processado", toResponse(existing.get()));
        }

        Transaction transaction = Transaction.builder()
                .idempotencyKey(request.getIdempotencyKey())
                .userId(userId)
                .sourceAccount(request.getAccountNumber())
                .destinationAccount("EXTERNAL")
                .amount(request.getAmount())
                .transactionType(Transaction.TransactionType.WITHDRAWAL)
                .description(request.getDescription() != null ? request.getDescription() : "Saque")
                .status(Transaction.TransactionStatus.PENDING)
                .build();

        transactionRepository.save(transaction);

        try {
            accountServiceClient.addLedgerEntry(request.getAccountNumber(),
                    request.getAmount().negate(), "WITHDRAWAL",
                    transaction.getDescription(), transaction.getId().toString());

            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            // Publicar evento no Kafka
            kafkaEventPublisher.publishWithdrawalEvent(
                    transaction.getId(), userId,
                    request.getAccountNumber(), request.getAmount(), request.getDescription());

        } catch (Exception e) {
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new BusinessException("Saque falhou: " + e.getMessage());
        }

        return toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getHistory(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .idempotencyKey(t.getIdempotencyKey())
                .sourceAccount(t.getSourceAccount())
                .destinationAccount(t.getDestinationAccount())
                .amount(t.getAmount())
                .transactionType(t.getTransactionType())
                .status(t.getStatus())
                .description(t.getDescription())
                .createdAt(t.getCreatedAt())
                .completedAt(t.getCompletedAt())
                .build();
    }
}
