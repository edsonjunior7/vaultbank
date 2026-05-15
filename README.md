cat > ~/vaultbank/README.md << 'README'
# 🏦 VaultBank — Distributed Digital Banking System

> Enterprise-grade distributed banking platform inspired by Nubank, Revolut and Wise.
> Built with microservices, Kafka, advanced security, fraud detection and full observability.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-green?style=flat-square&logo=springboot)
![Kafka](https://img.shields.io/badge/Apache_Kafka-7.6-black?style=flat-square&logo=apachekafka)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)
![Redis](https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Microservices](#microservices)
- [Tech Stack](#tech-stack)
- [Security](#security)
- [Quick Start](#quick-start)
- [API Endpoints](#api-endpoints)
- [Kafka Events](#kafka-events)
- [Observability](#observability)

---

## 🎯 Overview

VaultBank simulates a real digital bank with a complete enterprise architecture:

- **Bank accounts** with balance calculated via Ledger System (the way real banks do it)
- **Transfers, deposits and withdrawals** with idempotency and pessimistic locking
- **Fraud detection** with a score engine and 4 configurable rules
- **Complete audit trail** of all operations via Kafka
- **Async notifications** fully decoupled via event streaming
- **AES-256-GCM encryption** for sensitive data at rest
- **Full observability** with Prometheus, Grafana and Zipkin

---

## 🏗️ Architecture
                    ┌─────────────────┐
                    │   API Clients   │
                    └────────┬────────┘
                             │
      ┌──────────────────────┼──────────────────────┐
      │                      │                      │
┌──────▼──────┐      ┌───────▼───────┐      ┌──────▼──────┐
│Auth Service │      │Account Service│      │Transaction  │
│   :8081     │      │    :8082      │      │  Service    │
│ JWT+BCrypt  │      │ Ledger System │      │   :8083     │
│ AES-256-GCM │      │ Pessim. Lock  │      │ Idempotency │
└─────────────┘      └───────────────┘      └──────┬──────┘
│
┌────────▼────────┐
│   Kafka Bus     │
│ money.deposited │
│ money.withdrawn │
│money.transferred│
│ fraud.detected  │
└───────┬─────────┘
┌────────────────────┬──┴──────────────────┐
│                    │                     │
┌──────────▼──────┐  ┌──────────▼─────────┐ ┌───────▼──────┐
│  Notification   │  │  Fraud Detection   │ │    Audit     │
│  Service :8084  │  │   Service :8085    │ │ Service:8086 │
└─────────────────┘  └────────────────────┘ └──────────────┘

---

## 🔧 Microservices

### Auth Service `:8081`
| Feature | Implementation |
|---|---|
| Passwords | BCrypt (cost 12) |
| Tokens | JWT HMAC-SHA384 + Refresh Token |
| Blacklist | Redis with automatic TTL |
| Rate Limiting | Account lock after 5 failed attempts (15 min) |
| Sensitive Data | AES-256-GCM (SSN, phone) |
| SSN Lookup | SHA-256 deterministic hash |

### Account Service `:8082`
| Feature | Implementation |
|---|---|
| Balance | Calculated via Ledger (never stored directly) |
| Concurrency | Pessimistic Locking (PESSIMISTIC_WRITE) |
| Account Types | CHECKING, SAVINGS |
| History | Paginated with balance snapshot |

> **Ledger System**: the balance is never stored directly. Each transaction generates an entry (+1000, -200) and the balance is calculated by summing all entries — exactly how Nubank and real banks work.

### Transaction Service `:8083`
| Feature | Implementation |
|---|---|
| Operations | Transfer, Deposit, Withdrawal |
| Idempotency | Unique key per operation (prevents duplicates) |
| Events | Publishes to Kafka after each operation |

### Notification Service `:8084`
Consumes `money.deposited`, `money.withdrawn`, `money.transferred` and stores notifications. Completely decoupled from the Transaction Service.

### Fraud Detection Service `:8085`
Score engine 0-100:

| Rule | Score | Trigger |
|---|---|---|
| HIGH_AMOUNT | +40 | Amount > $10,000 |
| SUSPICIOUS_AMOUNT | +20 | Amount > $5,000 |
| HIGH_FREQUENCY | +35 | > 5 transactions/minute |
| HIGH_VOLUME | +30 | > $20,000/hour |
| ROUND_AMOUNT | +10 | Round amount > $1,000 |

| Score | Level | Decision |
|---|---|---|
| 0-19 | LOW | APPROVED |
| 20-39 | MEDIUM | APPROVED |
| 40-69 | HIGH | REVIEW |
| 70+ | CRITICAL | BLOCKED |

### Audit Service `:8086`
Immutable audit trail of all operations and fraud detections via Kafka. Append-only by design.

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| Runtime | Java | 21 |
| Framework | Spring Boot | 3.2.5 |
| Security | Spring Security + JWT | - |
| Database | PostgreSQL | 16 |
| Cache / Blacklist | Redis | 7 |
| Message Broker | Apache Kafka | 7.6 |
| Encryption | AES-256-GCM (JCA) | - |
| Containers | Docker + Docker Compose | - |
| Metrics | Prometheus + Micrometer | - |
| Dashboards | Grafana | 10.4 |
| Tracing | Zipkin | 3 |
| API Docs | SpringDoc OpenAPI | 2.5 |

---

## 🔐 Security

### Encryption at Rest — AES-256-GCM
SSN:   99988877766  →  u2U1l3c99u5JqAG8J59xQmWQQv8OvkQsEoX99UlmASAFVKsIe+yu
Phone: 11999999999  →  nejPNx4zPo1R8UItdY5yzQcx9cwse3zmghWTLFosNhyhwipoT0Ly
- IV: 96-bit random per operation (never reused)
- Authentication tag: 128 bits (tamper detection)
- Key: 256 bits via environment variable

### JWT + BCrypt
- Access Token: 15 minutes
- Refresh Token: 7 days, rotated on every use
- Blacklist stored in Redis with automatic TTL

### RBAC
CUSTOMER → own banking operations
ADMIN    → full system access
SUPPORT  → customer service
AUDITOR  → read-only

---

## 🚀 Quick Start

### Prerequisites
- Docker Desktop
- Git

### Run

```bash
git clone https://github.com/edsonjunior7/vaultbank.git
cd vaultbank
docker-compose up -d --build
```

### Create database schemas

```bash
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_accounts < docker/init-account-db.sql
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_transactions < docker/init-transaction-db.sql
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_notifications < docker/init-notification-db.sql
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_fraud < docker/init-fraud-db.sql
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_audit < docker/init-audit-db.sql
docker-compose restart account-service transaction-service notification-service fraud-service audit-service
```

### Check containers

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

Expected output:
vaultbank-transaction    Up (healthy)
vaultbank-notification   Up (healthy)
vaultbank-audit          Up (healthy)
vaultbank-fraud          Up (healthy)
vaultbank-account        Up (healthy)
vaultbank-auth           Up (healthy)
vaultbank-grafana        Up
vaultbank-kafka          Up (healthy)
vaultbank-postgres       Up (healthy)
vaultbank-redis          Up (healthy)
vaultbank-prometheus     Up
vaultbank-zookeeper      Up (healthy)
vaultbank-zipkin         Up (healthy)

---

## 📡 API Endpoints

### Auth Service `http://localhost:8081`
```http
POST /auth/register     # Create account (SSN encrypted)
POST /auth/login        # Authenticate (returns JWT)
POST /auth/refresh      # Renew access token
POST /auth/logout       # Revoke tokens (Redis blacklist)
GET  /auth/me           # Authenticated user data
```

### Account Service `http://localhost:8082`
```http
POST   /accounts              # Create bank account
GET    /accounts              # List my accounts
GET    /accounts/{id}/balance # Check balance (via Ledger)
GET    /accounts/{id}/ledger  # Transaction history
PATCH  /accounts/{id}/block   # Block account
```

### Transaction Service `http://localhost:8083`
```http
POST /transactions/transfer   # Transfer between accounts
POST /transactions/deposit    # Deposit funds
POST /transactions/withdraw   # Withdraw funds
GET  /transactions/history    # Transaction history
```

### Swagger UI
| Service | URL |
|---|---|
| Auth | http://localhost:8081/swagger-ui.html |
| Account | http://localhost:8082/swagger-ui.html |
| Transaction | http://localhost:8083/swagger-ui.html |

---

## 📨 Kafka Events

| Topic | Published by | Consumed by |
|---|---|---|
| `money.deposited` | Transaction | Notification, Fraud, Audit |
| `money.withdrawn` | Transaction | Notification, Fraud, Audit |
| `money.transferred` | Transaction | Notification, Fraud, Audit |
| `fraud.detected` | Fraud | Audit |

---

## 📊 Observability

| Tool | URL | Credentials |
|---|---|---|
| Grafana | http://localhost:3000 | admin / vaultbank123 |
| Prometheus | http://localhost:9090 | - |
| Zipkin | http://localhost:9411 | - |

Metrics collected from all services via `/actuator/prometheus`:
- Requests per second per service
- p99 latency
- JVM Heap usage
- Active threads
- HTTP 5xx errors

---

## 📁 Project Structure
vaultbank/
├── services/
│   ├── auth-service/           # JWT + BCrypt + AES-256-GCM
│   ├── account-service/        # Ledger Banking System
│   ├── transaction-service/    # Idempotency + Kafka Producer
│   ├── notification-service/   # Kafka Consumer
│   ├── fraud-service/          # Score Engine + 4 Rules
│   └── audit-service/          # Audit Trail
├── docker/
│   ├── prometheus/             # prometheus.yml
│   ├── grafana/                # Datasources + Dashboards
│   └── *.sql                   # Database schemas
├── docker-compose.yml          # 13 containers orchestrated
├── .env.example                # Environment variables template
└── .gitignore

---

## 🗺️ Roadmap

- [x] Phase 1 — Auth Service + JWT + Docker
- [x] Phase 2 — Account Service + Ledger + Transactions
- [x] Phase 3 — Kafka + Notification Service
- [x] Phase 4 — Fraud Detection + Audit Service
- [x] Phase 5 — Prometheus + Grafana + Zipkin
- [x] AES-256-GCM Encryption
- [ ] Phase 6 — Frontend Dashboard
- [ ] CI/CD — GitHub Actions
- [ ] mTLS — Secure inter-service communication

---

## 👨‍💻 Author

**Edson Junior** — [@edsonjunior7](https://github.com/edsonjunior7)

---

<p align="center">
  <strong>VaultBank</strong> — Real banking architecture, built from scratch.
</p>
README
