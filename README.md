
cat > ~/vaultbank/README.md << 'README'
# 🏦 VaultBank — Distributed Digital Banking System

> Plataforma bancária distribuída enterprise-grade inspirada em Nubank, Revolut e Wise.
> Construída com microsserviços, Kafka, segurança avançada, antifraude e observabilidade completa.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green?style=flat-square&logo=springboot)
![Kafka](https://img.shields.io/badge/Apache_Kafka-7.6-black?style=flat-square&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![Redis](https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis)

---

## 📋 Sumário

- [Visão Geral](#visão-geral)
- [Arquitetura](#arquitetura)
- [Microsserviços](#microsserviços)
- [Stack Tecnológica](#stack-tecnológica)
- [Segurança](#segurança)
- [Quick Start](#quick-start)
- [Endpoints da API](#endpoints-da-api)
- [Kafka Events](#kafka-events)
- [Observabilidade](#observabilidade)

---

## 🎯 Visão Geral

O VaultBank simula um banco digital real com:

- **Contas bancárias** com saldo calculado via Ledger System
- **Transferências, depósitos e saques** com idempotência e pessimistic locking
- **Antifraude** com score engine e 4 regras configuráveis
- **Auditoria completa** de todas as operações via Kafka
- **Notificações assíncronas** desacopladas via event streaming
- **Criptografia AES-256-GCM** para dados sensíveis em repouso
- **Observabilidade** com Prometheus, Grafana e Zipkin

---

## 🏗️ Arquitetura

```
                        ┌─────────────────┐
                        │   API Clients   │
                        └────────┬────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
   ┌──────▼──────┐      ┌───────▼───────┐      ┌──────▼──────┐
   │Auth Service │      │Account Service│      │Transaction  │
   │   :8081     │      │    :8082      │      │  Service    │
   │             │      │               │      │   :8083     │
   │ JWT + BCrypt│      │ Ledger System │      │Idempotência │
   │ AES-256-GCM │      │ Pessim. Lock  │      │   + Kafka   │
   └─────────────┘      └───────────────┘      └──────┬──────┘
                                                       │
                                              ┌────────▼────────┐
                                              │   Kafka Bus     │
                                              │  money.deposited│
                                              │  money.withdrawn│
                                              │money.transferred│
                                              │ fraud.detected  │
                                              └────────┬────────┘
                                                       │
              ┌────────────────────┬──────────────────┴─────────────────┐
              │                    │                                     │
   ┌──────────▼──────┐  ┌─────────▼──────────┐             ┌───────────▼──────┐
   │  Notification   │  │  Fraud Detection   │             │  Audit Service   │
   │   Service :8084 │  │   Service :8085    │             │     :8086        │
   │ Push/Email/WS   │  │  Score Engine      │             │ Trilha completa  │
   └─────────────────┘  │  4 Fraud Rules     │             └──────────────────┘
                        └────────────────────┘
```

---

## 🔧 Microsserviços

### 1. Auth Service `:8081`

| Funcionalidade | Implementação |
|---|---|
| Senhas | BCrypt (custo 12) |
| Tokens | JWT (HMAC-SHA384) + Refresh Token |
| Blacklist | Redis com TTL automático |
| Rate Limiting | Bloqueio após 5 tentativas (15 min) |
| Dados pessoais | AES-256-GCM (CPF, telefone) |
| Busca por CPF | SHA-256 hash determinístico |

### 2. Account Service `:8082`

| Funcionalidade | Implementação |
|---|---|
| Tipos de conta | CHECKING, SAVINGS |
| Saldo | Calculado via Ledger (nunca armazenado) |
| Concorrência | Pessimistic Locking (PESSIMISTIC_WRITE) |
| Histórico | Paginado com snapshot de saldo |

> **Ledger System**: o saldo nunca é armazenado diretamente. Cada movimentação gera uma entrada e o saldo é calculado somando todas as entradas — exatamente como Nubank e bancos reais funcionam.

### 3. Transaction Service `:8083`

| Funcionalidade | Implementação |
|---|---|
| Transferências | PIX simulado entre contas |
| Idempotência | Chave única por operação (evita duplicatas) |
| Eventos | Publica no Kafka após cada operação |

### 4. Notification Service `:8084`
Consome `money.deposited`, `money.withdrawn`, `money.transferred` e registra notificações. Completamente desacoplado do Transaction Service.

### 5. Fraud Detection Service `:8085`

| Regra | Score | Trigger |
|---|---|---|
| HIGH_AMOUNT | +40 | Valor > R$ 10.000 |
| SUSPICIOUS_AMOUNT | +20 | Valor > R$ 5.000 |
| HIGH_FREQUENCY | +35 | > 5 transações/minuto |
| HIGH_VOLUME | +30 | > R$ 20.000/hora |
| ROUND_AMOUNT | +10 | Valor redondo > R$ 1.000 |

| Score | Nível | Decisão |
|---|---|---|
| 0-19 | LOW | APPROVED |
| 20-39 | MEDIUM | APPROVED |
| 40-69 | HIGH | REVIEW |
| 70+ | CRITICAL | BLOCKED |

### 6. Audit Service `:8086`
Trilha imutável de todas as operações e detecções de fraude via Kafka.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia | Versão |
|---|---|---|
| Runtime | Java | 21 |
| Framework | Spring Boot | 3.2.5 |
| Segurança | Spring Security + JWT | - |
| Banco principal | PostgreSQL | 16 |
| Cache / Blacklist | Redis | 7 |
| Message Broker | Apache Kafka | 7.6 |
| Criptografia | AES-256-GCM (JCA) | - |
| Containers | Docker + Docker Compose | - |
| Métricas | Prometheus + Micrometer | - |
| Dashboards | Grafana | 10.4 |
| Tracing | Zipkin | 3 |
| API Docs | SpringDoc OpenAPI | 2.5 |

---

## 🔐 Segurança

### Criptografia em Repouso — AES-256-GCM

```
CPF:      99988877766  →  u2U1l3c99u5JqAG8J59xQmWQQv8OvkQsEoX99UlmASAFVKsIe+yu
Telefone: 11999999999  →  nejPNx4zPo1R8UItdY5yzQcx9cwse3zmghWTLFosNhyhwipoT0Ly
```

- IV: 96 bits aleatório por operação
- Tag de autenticação: 128 bits (detecta adulteração)
- Chave: 256 bits via variável de ambiente

### JWT + BCrypt
- Access Token: 15 minutos
- Refresh Token: 7 dias, rotacionado a cada uso
- Blacklist no Redis com TTL automático

### RBAC
```
CUSTOMER  →  operações bancárias próprias
ADMIN     →  acesso total
SUPPORT   →  atendimento
AUDITOR   →  somente leitura
```

---

## 🚀 Quick Start

```bash
git clone https://github.com/edsonjunior7/vaultbank.git
cd vaultbank
docker-compose up -d --build
```

Criar schemas:

```bash
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_accounts < docker/init-account-db.sql
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_transactions < docker/init-transaction-db.sql
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_notifications < docker/init-notification-db.sql
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_fraud < docker/init-fraud-db.sql
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_audit < docker/init-audit-db.sql
docker-compose restart account-service transaction-service notification-service fraud-service audit-service
```

---

## 📡 Endpoints da API

### Auth Service `http://localhost:8081`
```http
POST /auth/register     # Criar conta
POST /auth/login        # Autenticar
POST /auth/refresh      # Renovar token
POST /auth/logout       # Revogar tokens
GET  /auth/me           # Dados do usuário
```

### Account Service `http://localhost:8082`
```http
POST   /accounts
GET    /accounts
GET    /accounts/{id}/balance
GET    /accounts/{id}/ledger
PATCH  /accounts/{id}/block
```

### Transaction Service `http://localhost:8083`
```http
POST /transactions/transfer
POST /transactions/deposit
POST /transactions/withdraw
GET  /transactions/history
```

### Swagger UI
| Serviço | URL |
|---|---|
| Auth | http://localhost:8081/swagger-ui.html |
| Account | http://localhost:8082/swagger-ui.html |
| Transaction | http://localhost:8083/swagger-ui.html |

---

## 📨 Kafka Events

| Tópico | Publicado por | Consumido por |
|---|---|---|
| `money.deposited` | Transaction | Notification, Fraud, Audit |
| `money.withdrawn` | Transaction | Notification, Fraud, Audit |
| `money.transferred` | Transaction | Notification, Fraud, Audit |
| `fraud.detected` | Fraud | Audit |

---

## 📊 Observabilidade

| Ferramenta | URL | Credenciais |
|---|---|---|
| Grafana | http://localhost:3000 | admin / vaultbank123 |
| Prometheus | http://localhost:9090 | - |
| Zipkin | http://localhost:9411 | - |

---

## 🗺️ Roadmap

- [x] Fase 1 — Auth Service + JWT + Docker
- [x] Fase 2 — Account Service + Ledger + Transactions
- [x] Fase 3 — Kafka + Notification Service
- [x] Fase 4 — Fraud Detection + Audit Service
- [x] Fase 5 — Prometheus + Grafana + Zipkin
- [x] Criptografia AES-256-GCM
- [ ] Fase 6 — Frontend Dashboard
- [ ] CI/CD — GitHub Actions

---

## 👨‍💻 Autor

**Edson Junior** — [@edsonjunior7](https://github.com/edsonjunior7)
README

git add README.md
git commit -m "docs: add complete professional README"
git push
```
