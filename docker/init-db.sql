-- ============================================================
-- VaultBank - Auth Service Database Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    full_name       VARCHAR(150)        NOT NULL,
    email           VARCHAR(255)        NOT NULL UNIQUE,
    -- CPF criptografado AES-256-GCM (comprimento maior por causa do IV + tag)
    document_number VARCHAR(512)        NOT NULL,
    -- Hash SHA-256 do CPF para busca sem descriptografar
    document_hash   VARCHAR(64)         NOT NULL UNIQUE,
    password_hash   VARCHAR(255)        NOT NULL,
    -- Telefone criptografado AES-256-GCM
    phone           VARCHAR(512),
    status          VARCHAR(20)         NOT NULL DEFAULT 'ACTIVE',
    email_verified  BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expires_at  TIMESTAMP   NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS login_attempts (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    ip_address    VARCHAR(45)  NOT NULL,
    success       BOOLEAN      NOT NULL,
    attempted_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    user_agent    VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS permissions (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

INSERT INTO roles (name, description) VALUES
    ('CUSTOMER', 'Cliente comum do banco'),
    ('ADMIN', 'Administrador do sistema'),
    ('SUPPORT', 'Atendente de suporte'),
    ('AUDITOR', 'Auditor - somente leitura')
ON CONFLICT (name) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_users_email           ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_document_hash   ON users(document_hash);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token  ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user   ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_login_attempts_email  ON login_attempts(email);
