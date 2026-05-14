-- ============================================================
-- VaultBank - Notification Service Database Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    type            VARCHAR(50)     NOT NULL,  -- DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    title           VARCHAR(255)    NOT NULL,
    message         TEXT            NOT NULL,
    reference_id    VARCHAR(100),              -- ID da transacao
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',  -- PENDING, SENT, FAILED
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    sent_at         TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id   ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status    ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_type      ON notifications(type);
