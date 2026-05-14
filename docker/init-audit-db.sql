-- ============================================================
-- VaultBank - Audit Service Database Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    event_type      VARCHAR(50)     NOT NULL,
    user_id         BIGINT,
    entity_type     VARCHAR(50),
    entity_id       VARCHAR(100),
    action          VARCHAR(100)    NOT NULL,
    details         TEXT,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_event_type  ON audit_logs(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_user_id     ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity      ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_created_at  ON audit_logs(created_at);
