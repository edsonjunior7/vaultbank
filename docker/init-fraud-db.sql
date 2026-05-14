-- ============================================================
-- VaultBank - Fraud Detection Service Database Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS fraud_analysis (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    account_number  VARCHAR(20)     NOT NULL,
    amount          NUMERIC(19,4)   NOT NULL,
    risk_score      INTEGER         NOT NULL DEFAULT 0,  -- 0-100
    risk_level      VARCHAR(20)     NOT NULL,            -- LOW, MEDIUM, HIGH, CRITICAL
    decision        VARCHAR(20)     NOT NULL,            -- APPROVED, BLOCKED, REVIEW
    rules_triggered TEXT,                                -- JSON array de regras disparadas
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS blocked_transactions (
    id              BIGSERIAL PRIMARY KEY,
    transaction_id  BIGINT          NOT NULL UNIQUE,
    user_id         BIGINT          NOT NULL,
    reason          TEXT            NOT NULL,
    blocked_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fraud_transaction_id  ON fraud_analysis(transaction_id);
CREATE INDEX IF NOT EXISTS idx_fraud_user_id         ON fraud_analysis(user_id);
CREATE INDEX IF NOT EXISTS idx_fraud_risk_level      ON fraud_analysis(risk_level);
CREATE INDEX IF NOT EXISTS idx_fraud_created_at      ON fraud_analysis(created_at);
CREATE INDEX IF NOT EXISTS idx_blocked_transaction   ON blocked_transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_blocked_user          ON blocked_transactions(user_id);
