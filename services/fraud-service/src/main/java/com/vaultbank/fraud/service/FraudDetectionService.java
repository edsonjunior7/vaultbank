package com.vaultbank.fraud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaultbank.fraud.entity.BlockedTransaction;
import com.vaultbank.fraud.entity.FraudAnalysis;
import com.vaultbank.fraud.repository.BlockedTransactionRepository;
import com.vaultbank.fraud.repository.FraudAnalysisRepository;
import com.vaultbank.fraud.rule.FraudRule;
import com.vaultbank.fraud.rule.FraudRule.FraudContext;
import com.vaultbank.fraud.rule.FraudRule.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final BlockedTransactionRepository blockedTransactionRepository;
    private final List<FraudRule> fraudRules;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public FraudAnalysis analyze(Long transactionId, Long userId, String accountNumber,
                                  BigDecimal amount, String ipAddress) {

        // Contexto para as regras
        long recentCount = fraudAnalysisRepository.countRecentTransactions(
                userId, LocalDateTime.now().minusMinutes(1));
        BigDecimal amountLastHour = fraudAnalysisRepository.sumAmountSince(
                userId, LocalDateTime.now().minusHours(1));

        FraudContext context = new FraudContext(
                transactionId, userId, accountNumber, amount, ipAddress,
                recentCount, amountLastHour
        );

        // Avaliar todas as regras
        List<RuleResult> results = fraudRules.stream()
                .map(rule -> rule.evaluate(context))
                .toList();

        // Calcular score total
        int totalScore = results.stream()
                .mapToInt(RuleResult::scoreAdded)
                .sum();
        totalScore = Math.min(totalScore, 100);

        List<String> triggeredRules = results.stream()
                .filter(RuleResult::triggered)
                .map(r -> r.ruleName() + ": " + r.reason())
                .collect(Collectors.toList());

        // Determinar nivel de risco e decisao
        FraudAnalysis.RiskLevel riskLevel = calculateRiskLevel(totalScore);
        FraudAnalysis.Decision decision = calculateDecision(totalScore);

        String rulesJson = triggeredRules.isEmpty() ? "[]" :
                "[\"" + String.join("\",\"", triggeredRules) + "\"]";

        FraudAnalysis analysis = FraudAnalysis.builder()
                .transactionId(transactionId)
                .userId(userId)
                .accountNumber(accountNumber)
                .amount(amount)
                .riskScore(totalScore)
                .riskLevel(riskLevel)
                .decision(decision)
                .rulesTriggered(rulesJson)
                .build();

        fraudAnalysisRepository.save(analysis);

        log.info("Analise de fraude: tx={} score={} nivel={} decisao={}",
                transactionId, totalScore, riskLevel, decision);

        // Bloquear se necessario
        if (decision == FraudAnalysis.Decision.BLOCKED) {
            blockTransaction(transactionId, userId, triggeredRules);
            publishFraudEvent(transactionId, userId, accountNumber, amount, totalScore, triggeredRules);
        }

        return analysis;
    }

    private void blockTransaction(Long transactionId, Long userId, List<String> reasons) {
        if (!blockedTransactionRepository.existsByTransactionId(transactionId)) {
            blockedTransactionRepository.save(BlockedTransaction.builder()
                    .transactionId(transactionId)
                    .userId(userId)
                    .reason(String.join("; ", reasons))
                    .build());
            log.warn("TRANSACAO BLOQUEADA por fraude: tx={} usuario={}", transactionId, userId);
        }
    }

    private void publishFraudEvent(Long transactionId, Long userId, String accountNumber,
                                    BigDecimal amount, int score, List<String> reasons) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "transactionId", transactionId,
                    "userId", userId,
                    "accountNumber", accountNumber,
                    "amount", amount,
                    "riskScore", score,
                    "reasons", reasons,
                    "occurredAt", LocalDateTime.now().toString()
            ));
            kafkaTemplate.send("fraud.detected", payload);
            log.info("Evento de fraude publicado: tx={}", transactionId);
        } catch (Exception e) {
            log.error("Falha ao publicar evento de fraude: {}", e.getMessage());
        }
    }

    private FraudAnalysis.RiskLevel calculateRiskLevel(int score) {
        if (score >= 70) return FraudAnalysis.RiskLevel.CRITICAL;
        if (score >= 40) return FraudAnalysis.RiskLevel.HIGH;
        if (score >= 20) return FraudAnalysis.RiskLevel.MEDIUM;
        return FraudAnalysis.RiskLevel.LOW;
    }

    private FraudAnalysis.Decision calculateDecision(int score) {
        if (score >= 70) return FraudAnalysis.Decision.BLOCKED;
        if (score >= 40) return FraudAnalysis.Decision.REVIEW;
        return FraudAnalysis.Decision.APPROVED;
    }
}
