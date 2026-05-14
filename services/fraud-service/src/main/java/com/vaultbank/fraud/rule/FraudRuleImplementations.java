package com.vaultbank.fraud.rule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// ── Regra 1: Valor alto por transacao ─────────────────────────
@Component
class HighAmountRule implements FraudRule {

    @Value("${fraud.rules.max-amount-per-transaction:10000.00}")
    private BigDecimal maxAmount;

    @Value("${fraud.rules.suspicious-amount-threshold:5000.00}")
    private BigDecimal suspiciousThreshold;

    @Override
    public RuleResult evaluate(FraudContext ctx) {
        if (ctx.amount().compareTo(maxAmount) > 0) {
            return RuleResult.triggered("HIGH_AMOUNT", 40,
                    String.format("Valor R$ %.2f excede limite de R$ %.2f", ctx.amount(), maxAmount));
        }
        if (ctx.amount().compareTo(suspiciousThreshold) > 0) {
            return RuleResult.triggered("SUSPICIOUS_AMOUNT", 20,
                    String.format("Valor R$ %.2f acima do limiar suspeito", ctx.amount()));
        }
        return RuleResult.clean("HIGH_AMOUNT");
    }
}

// ── Regra 2: Muitas transacoes por minuto ─────────────────────
@Component
class HighFrequencyRule implements FraudRule {

    @Value("${fraud.rules.max-transactions-per-minute:5}")
    private int maxPerMinute;

    @Override
    public RuleResult evaluate(FraudContext ctx) {
        if (ctx.recentTransactionCount() >= maxPerMinute) {
            return RuleResult.triggered("HIGH_FREQUENCY", 35,
                    String.format("%d transacoes no ultimo minuto (max: %d)",
                            ctx.recentTransactionCount(), maxPerMinute));
        }
        if (ctx.recentTransactionCount() >= maxPerMinute - 1) {
            return RuleResult.triggered("NEAR_FREQUENCY_LIMIT", 15,
                    "Proximo do limite de transacoes por minuto");
        }
        return RuleResult.clean("HIGH_FREQUENCY");
    }
}

// ── Regra 3: Volume alto na ultima hora ───────────────────────
@Component
class HighVolumeRule implements FraudRule {

    @Value("${fraud.rules.max-amount-per-hour:20000.00}")
    private BigDecimal maxPerHour;

    @Override
    public RuleResult evaluate(FraudContext ctx) {
        BigDecimal totalWithCurrent = ctx.amountLastHour().add(ctx.amount());

        if (totalWithCurrent.compareTo(maxPerHour) > 0) {
            return RuleResult.triggered("HIGH_VOLUME", 30,
                    String.format("Volume de R$ %.2f na hora excede limite de R$ %.2f",
                            totalWithCurrent, maxPerHour));
        }
        return RuleResult.clean("HIGH_VOLUME");
    }
}

// ── Regra 4: Valor redondo suspeito ──────────────────────────
@Component
class RoundAmountRule implements FraudRule {

    @Override
    public RuleResult evaluate(FraudContext ctx) {
        // Valores redondos grandes sao suspeitos (ex: 5000.00, 10000.00)
        boolean isRound = ctx.amount().stripTrailingZeros().scale() <= 0;
        boolean isLarge = ctx.amount().compareTo(new BigDecimal("1000")) >= 0;

        if (isRound && isLarge) {
            return RuleResult.triggered("ROUND_AMOUNT", 10,
                    String.format("Valor redondo suspeito: R$ %.2f", ctx.amount()));
        }
        return RuleResult.clean("ROUND_AMOUNT");
    }
}
