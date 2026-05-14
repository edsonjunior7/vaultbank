-- ============================================================
-- VaultBank - Account Service Database Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS accounts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    account_number  VARCHAR(20)     NOT NULL UNIQUE,
    account_type    VARCHAR(20)     NOT NULL,  -- CHECKING, SAVINGS
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, BLOCKED, CLOSED
    currency        VARCHAR(3)      NOT NULL DEFAULT 'BRL',
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Ledger: cada linha e uma movimentacao financeira
-- O saldo NUNCA e armazenado diretamente — e calculado somando as entradas
CREATE TABLE IF NOT EXISTS ledger_entries (
    id              BIGSERIAL PRIMARY KEY,
    account_id      BIGINT          NOT NULL REFERENCES accounts(id),
    amount          NUMERIC(19,4)   NOT NULL,  -- positivo = credito, negativo = debito
    entry_type      VARCHAR(30)     NOT NULL,  -- DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, FEE
    description     VARCHAR(255),
    reference_id    VARCHAR(100),              -- ID da transacao de origem
    balance_after   NUMERIC(19,4)   NOT NULL,  -- snapshot do saldo apos a entrada
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_accounts_user_id       ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_number        ON accounts(account_number);
CREATE INDEX IF NOT EXISTS idx_ledger_account_id      ON ledger_entries(account_id);
CREATE INDEX IF NOT EXISTS idx_ledger_created_at      ON ledger_entries(created_at);
CREATE INDEX IF NOT EXISTS idx_ledger_reference_id    ON ledger_entries(reference_id);
