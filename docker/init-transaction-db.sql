-- ============================================================
-- VaultBank - Transaction Service Database Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS transactions (
    id                  BIGSERIAL PRIMARY KEY,
    idempotency_key     VARCHAR(100)    NOT NULL UNIQUE,
    user_id             BIGINT          NOT NULL,
    source_account      VARCHAR(20)     NOT NULL,
    destination_account VARCHAR(20)     NOT NULL,
    amount              NUMERIC(19,4)   NOT NULL,
    transaction_type    VARCHAR(20)     NOT NULL,  -- TRANSFER, DEPOSIT, WITHDRAWAL
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',  -- PENDING, COMPLETED, FAILED, REVERSED
    description         VARCHAR(255),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    completed_at        TIMESTAMP
);

-- Tabela de idempotencia — evita transacoes duplicadas
CREATE TABLE IF NOT EXISTS idempotency_keys (
    key             VARCHAR(100)    PRIMARY KEY,
    transaction_id  BIGINT          NOT NULL REFERENCES transactions(id),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_transactions_user_id         ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_source          ON transactions(source_account);
CREATE INDEX IF NOT EXISTS idx_transactions_destination     ON transactions(destination_account);
CREATE INDEX IF NOT EXISTS idx_transactions_status          ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_idempotency     ON transactions(idempotency_key);
