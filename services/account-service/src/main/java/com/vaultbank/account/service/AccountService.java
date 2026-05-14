package com.vaultbank.account.service;

import com.vaultbank.account.dto.request.CreateAccountRequest;
import com.vaultbank.account.dto.request.LedgerEntryRequest;
import com.vaultbank.account.dto.response.AccountResponse;
import com.vaultbank.account.dto.response.LedgerEntryResponse;
import com.vaultbank.account.entity.Account;
import com.vaultbank.account.entity.LedgerEntry;
import com.vaultbank.account.exception.BusinessException;
import com.vaultbank.account.repository.AccountRepository;
import com.vaultbank.account.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    // ─── Criar conta ──────────────────────────────────────────

    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .userId(userId)
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .build();

        accountRepository.save(account);
        log.info("Conta criada: {} para usuario {}", accountNumber, userId);

        return toResponse(account, BigDecimal.ZERO);
    }

    // ─── Listar contas do usuario ─────────────────────────────

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(a -> toResponse(a, getBalance(a.getId())))
                .toList();
    }

    // ─── Buscar conta por ID ──────────────────────────────────

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId, Long userId) {
        Account account = findAccountOwnedBy(accountId, userId);
        return toResponse(account, getBalance(accountId));
    }

    // ─── Saldo ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long accountId) {
        return ledgerEntryRepository.findLastBalance(accountId)
                .orElse(BigDecimal.ZERO);
    }

    // ─── Bloquear conta ───────────────────────────────────────

    @Transactional
    public AccountResponse blockAccount(Long accountId, Long userId) {
        Account account = findAccountOwnedBy(accountId, userId);

        if (account.getStatus() == Account.AccountStatus.BLOCKED) {
            throw new BusinessException("Conta ja esta bloqueada");
        }

        account.setStatus(Account.AccountStatus.BLOCKED);
        accountRepository.save(account);
        log.info("Conta bloqueada: {}", account.getAccountNumber());

        return toResponse(account, getBalance(accountId));
    }

    // ─── Adicionar entrada no ledger (uso interno e do transaction-service) ──

    @Transactional
    public LedgerEntryResponse addLedgerEntry(LedgerEntryRequest request) {
        // Pessimistic lock para evitar race condition
        Account account = accountRepository.findByAccountNumberWithLock(request.getAccountNumber())
                .orElseThrow(() -> new BusinessException("Conta nao encontrada: " + request.getAccountNumber()));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BusinessException("Conta nao esta ativa: " + request.getAccountNumber());
        }

        BigDecimal currentBalance = getBalance(account.getId());
        BigDecimal newBalance = currentBalance.add(request.getAmount());

        // Validar saldo para debitos
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Saldo insuficiente. Saldo atual: " + currentBalance);
        }

        LedgerEntry entry = LedgerEntry.builder()
                .account(account)
                .amount(request.getAmount())
                .entryType(request.getEntryType())
                .description(request.getDescription())
                .referenceId(request.getReferenceId())
                .balanceAfter(newBalance)
                .build();

        ledgerEntryRepository.save(entry);
        log.info("Ledger entry: conta={} tipo={} valor={} saldo_depois={}",
                account.getAccountNumber(), request.getEntryType(), request.getAmount(), newBalance);

        return toLedgerResponse(entry);
    }

    // ─── Historico do ledger ──────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LedgerEntryResponse> getLedgerHistory(Long accountId, Long userId, Pageable pageable) {
        findAccountOwnedBy(accountId, userId);
        return ledgerEntryRepository
                .findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(this::toLedgerResponse);
    }

    // ─── Helpers ──────────────────────────────────────────────

    private Account findAccountOwnedBy(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Conta nao encontrada"));

        if (!account.getUserId().equals(userId)) {
            throw new BusinessException("Acesso negado a esta conta");
        }
        return account;
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.format("%010d", new Random().nextLong(9_000_000_000L) + 1_000_000_000L);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private AccountResponse toResponse(Account account, BigDecimal balance) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .currency(account.getCurrency())
                .balance(balance)
                .createdAt(account.getCreatedAt())
                .build();
    }

    private LedgerEntryResponse toLedgerResponse(LedgerEntry entry) {
        return LedgerEntryResponse.builder()
                .id(entry.getId())
                .amount(entry.getAmount())
                .entryType(entry.getEntryType())
                .description(entry.getDescription())
                .balanceAfter(entry.getBalanceAfter())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
