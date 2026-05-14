package com.vaultbank.account.repository;

import com.vaultbank.account.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    Page<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    // Calcula saldo atual somando todas as entradas
    @Query("SELECT COALESCE(SUM(le.amount), 0) FROM LedgerEntry le WHERE le.account.id = :accountId")
    BigDecimal calculateBalance(Long accountId);

    // Pega o ultimo snapshot de saldo
    @Query("SELECT le.balanceAfter FROM LedgerEntry le WHERE le.account.id = :accountId ORDER BY le.createdAt DESC LIMIT 1")
    Optional<BigDecimal> findLastBalance(Long accountId);
}
