package com.vaultbank.fraud.rule;

import java.math.BigDecimal;

// Contrato para todas as regras de fraude
public interface FraudRule {
    RuleResult evaluate(FraudContext context);

    record RuleResult(String ruleName, int scoreAdded, String reason, boolean triggered) {
        public static RuleResult clean(String ruleName) {
            return new RuleResult(ruleName, 0, null, false);
        }
        public static RuleResult triggered(String ruleName, int score, String reason) {
            return new RuleResult(ruleName, score, reason, true);
        }
    }

    record FraudContext(
            Long transactionId,
            Long userId,
            String accountNumber,
            BigDecimal amount,
            String ipAddress,
            long recentTransactionCount,
            BigDecimal amountLastHour
    ) {}
}
