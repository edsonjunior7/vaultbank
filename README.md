# 🏦 VaultBank — Distributed Digital Banking System

> Plataforma bancária distribuída enterprise-grade com microsserviços, Kafka, segurança avançada e observabilidade completa.

---

## ⚡ Quick Start — Fase 1

```bash
# 1. Subir infraestrutura (PostgreSQL + Redis)
docker-compose up postgres redis -d

# 2. Subir Auth Service
docker-compose up auth-service -d

# 3. Testar
curl http://localhost:8081/actuator/health

# 4. Swagger UI
open http://localhost:8081/swagger-ui.html
```

---

## 🏗️ Arquitetura

```
                        API Gateway (Fase futura)
                              |
     ┌────────────────────────┼────────────────────────┐
     │            │           │            │            │
  Auth         Account   Transaction    Card         Fraud
  Service      Service    Service      Service      Service
                              │
                        ┌─────┴──────┐
                        │ Kafka Bus  │
                        └─────┬──────┘
              ┌───────────────┼──────────────┐
              │               │              │
         Notification      Audit         Analytics
          Service         Service         Service
```

---

## 🗂️ Estrutura do Projeto

```
vaultbank/
├── services/
│   ├── auth-service/          ← ✅ Fase 1 (completo)
│   ├── account-service/       ← Fase 2
│   ├── transaction-service/   ← Fase 2
│   ├── fraud-service/         ← Fase 4
│   ├── notification-service/  ← Fase 3
│   ├── audit-service/         ← Fase 4
│   └── analytics-service/     ← Fase 5
├── gateway/                   ← Fase futura
├── frontend/                  ← Fase 6
├── docker/
│   └── init-db.sql
├── docker-compose.yml
├── docs/
└── scripts/
```

---

## 🔐 Auth Service — Endpoints

| Método | Endpoint        | Auth | Descrição                     |
|--------|-----------------|------|-------------------------------|
| POST   | /auth/register  | ❌   | Criar conta                   |
| POST   | /auth/login     | ❌   | Autenticar                    |
| POST   | /auth/refresh   | ❌   | Renovar access token          |
| POST   | /auth/logout    | ✅   | Encerrar sessão               |
| GET    | /auth/me        | ✅   | Dados do usuário autenticado  |

---

## 🛡️ Segurança

- **JWT** — Access token (15min) + Refresh token (7 dias)
- **BCrypt** — Hash de senha com custo 12
- **Blacklist** — Tokens inválidos guardados no Redis
- **Rate Limiting** — Bloqueio após 5 tentativas falhas (15min)
- **RBAC** — Roles: CUSTOMER, ADMIN, SUPPORT, AUDITOR

---

## 🛠️ Stack Tecnológica

| Camada       | Tecnologia                |
|--------------|---------------------------|
| Runtime      | Java 21                   |
| Framework    | Spring Boot 3.2           |
| Segurança    | Spring Security + JWT     |
| Banco        | PostgreSQL 16             |
| Cache        | Redis 7                   |
| Mensageria   | Kafka (Fase 3)            |
| Containers   | Docker + Docker Compose   |
| Docs         | Swagger / OpenAPI 3       |
| Observab.    | Prometheus + Grafana (F5) |

---

## 📋 Roadmap

- [x] **Fase 1** — Auth Service + JWT + Docker
- [ ] **Fase 2** — Account Service + Ledger + Transactions
- [ ] **Fase 3** — Kafka + Notification Service
- [ ] **Fase 4** — Fraud Detection + Audit
- [ ] **Fase 5** — Observabilidade (Grafana + Prometheus)
- [ ] **Fase 6** — Frontend Dashboard

---

## 🧪 Testando a API

### Registrar usuário
```bash
curl -X POST http://localhost:8081/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "João Silva",
    "email": "joao@email.com",
    "documentNumber": "12345678901",
    "password": "Senha@123",
    "phone": "11999999999"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "joao@email.com", "password": "Senha@123"}'
```

### Dados do usuário
```bash
curl http://localhost:8081/auth/me \
  -H "Authorization: Bearer {ACCESS_TOKEN}"
```
