package com.vaultbank.fraud.repository;

import com.vaultbank.fraud.entity.FraudAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysis, Long> {

    List<FraudAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Quantas transacoes o usuario fez no ultimo minuto
    @Query("""
        SELECT COUNT(f) FROM FraudAnalysis f
        WHERE f.userId = :userId
          AND f.createdAt >= :since
        """)
    long countRecentTransactions(Long userId, LocalDateTime since);

    // Soma total transacionada na ultima hora
    @Query("""
        SELECT COALESCE(SUM(f.amount), 0) FROM FraudAnalysis f
        WHERE f.userId = :userId
          AND f.createdAt >= :since
        """)
    BigDecimal sumAmountSince(Long userId, LocalDateTime since);
}
