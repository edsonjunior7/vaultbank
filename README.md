# 🏦 VaultBank

> Distributed digital banking system built with microservices, Kafka, and enterprise-grade security.

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk">
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-green?style=for-the-badge&logo=springboot">
  <img src="https://img.shields.io/badge/Kafka-Event_Driven-black?style=for-the-badge&logo=apachekafka">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql">
  <img src="https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker">
  <img src="https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis">
</p>

---

## ✨ Highlights

- Distributed microservices banking architecture
- Event-driven communication with Kafka
- Ledger-based balance system
- Fraud detection engine with scoring rules
- JWT authentication + AES-256-GCM encryption
- Idempotent financial transactions
- Full observability with Prometheus, Grafana, and Zipkin
- Dockerized infrastructure

---

## 🧠 Engineering Concepts

VaultBank implements several real-world backend engineering patterns commonly used in modern fintech platforms:

- Event-Driven Architecture
- Distributed Systems Design
- Ledger Accounting System
- Pessimistic Locking
- Idempotency Patterns
- Distributed Audit Trail
- Stateless Authentication
- RBAC Authorization
- Asynchronous Communication
- Centralized Observability

---

# 📋 Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Microservices](#-microservices)
- [Technology Stack](#-technology-stack)
- [Security](#-security)
- [Quick Start](#-quick-start)
- [API Endpoints](#-api-endpoints)
- [Kafka Events](#-kafka-events)
- [Observability](#-observability)
- [Roadmap](#-roadmap)

---

# 🎯 Overview

VaultBank simulates a real-world digital banking platform inspired by Nubank, Revolut, and Wise.

The system was designed using a distributed microservices architecture with asynchronous event streaming via Kafka, enabling scalability, resilience, and service decoupling.

Core banking concepts implemented:

- Bank accounts with ledger-based balance calculation
- Secure financial transactions
- Fraud detection engine
- Immutable audit trail
- Distributed notifications
- Enterprise-grade authentication
- Metrics, tracing, and monitoring

---

# 🏗️ Architecture

```text
                        ┌─────────────────┐
                        │   API Clients   │
                        └────────┬────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
   ┌──────▼──────┐      ┌───────▼───────┐      ┌──────▼──────┐
   │Auth Service │      │Account Service│      │Transaction  │
   │   :8081     │      │    :8082      │      │  Service    │
   │ JWT + BCrypt│      │ Ledger System │      │Idempotency  │
   │ AES-256-GCM │      │ Pessim. Lock  │      │   + Kafka   │
   └─────────────┘      └───────────────┘      └──────┬──────┘
                                                       │
                                              ┌────────▼────────┐
                                              │   Kafka Bus     │
                                              │ money.deposited │
                                              │ money.withdrawn │
                                              │money.transferred│
                                              │ fraud.detected  │
                                              └────────┬────────┘
                                                       │
              ┌────────────────────┬──────────────────┴─────────────────┐
              │                    │                                     │
   ┌──────────▼──────┐  ┌─────────▼──────────┐             ┌───────────▼──────┐
   │ Notification    │  │ Fraud Detection    │             │  Audit Service   │
   │ Service :8084   │  │ Service :8085      │             │      :8086       │
   │ Push/Email/WS   │  │ Score Engine       │             │ Immutable Logs   │
   └─────────────────┘  │ Fraud Rules        │             └──────────────────┘
                        └────────────────────┘
🔧 Microservices
1. Auth Service :8081
Feature	Implementation
Passwords	BCrypt
Authentication	JWT + Refresh Tokens
Encryption	AES-256-GCM
Blacklist	Redis
Rate Limiting	5 failed attempts
Authorization	RBAC
2. Account Service :8082
Feature	Implementation
Account Types	CHECKING, SAVINGS
Balance	Ledger-based
Concurrency	Pessimistic Locking
History	Paginated Ledger
The balance is never directly stored. Every transaction creates a ledger entry, and the balance is calculated by summing all entries.
3. Transaction Service :8083
Feature	Implementation
Transfers	Simulated PIX
Deposits	Ledger credit
Withdrawals	Ledger debit
Idempotency	Unique operation keys
Events	Kafka Producer
4. Notification Service :8084
Consumes Kafka financial events and generates asynchronous notifications.
Topics consumed:
money.deposited
money.withdrawn
money.transferred
5. Fraud Detection Service :8085
Rule	Score
HIGH_AMOUNT	+40
HIGH_FREQUENCY	+35
HIGH_VOLUME	+30
SUSPICIOUS_AMOUNT	+20
ROUND_AMOUNT	+10
Score Range	Decision
0-19	APPROVED
20-39	APPROVED
40-69	REVIEW
70+	BLOCKED
6. Audit Service :8086
Immutable append-only audit trail for:
Financial transactions
Fraud detection events
Distributed system logs
🛠️ Technology Stack
Layer	Technology
Runtime	Java 21
Framework	Spring Boot 3
Security	Spring Security + JWT
Database	PostgreSQL 16
Cache	Redis 7
Message Broker	Apache Kafka
Containers	Docker
Metrics	Prometheus
Dashboards	Grafana
Tracing	Zipkin
🔒 Security
Encryption
AES-256-GCM encryption
Random IV per operation
Authenticated encryption
Authentication
JWT Access Tokens
Refresh Token rotation
Redis token blacklist
Authorization
CUSTOMER → Banking operations
ADMIN    → Full access
SUPPORT  → Customer support
AUDITOR  → Read-only access
🚀 Quick Start
Clone repository
git clone https://github.com/edsonjunior7/vaultbank.git
cd vaultbank
Start infrastructure
docker-compose up -d --build
Initialize databases
docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_accounts < docker/init-account-db.sql

docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_transactions < docker/init-transaction-db.sql

docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_notifications < docker/init-notification-db.sql

docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_fraud < docker/init-fraud-db.sql

docker exec -i vaultbank-postgres psql -U vaultbank -d vaultbank_audit < docker/init-audit-db.sql
📡 API Endpoints
Auth Service
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
GET  /auth/me
Account Service
POST   /accounts
GET    /accounts
GET    /accounts/{id}/balance
GET    /accounts/{id}/ledger
PATCH  /accounts/{id}/block
Transaction Service
POST /transactions/transfer
POST /transactions/deposit
POST /transactions/withdraw
GET  /transactions/history
📨 Kafka Events
Topic	Producer	Consumers
money.deposited	Transaction	Notification, Fraud, Audit
money.withdrawn	Transaction	Notification, Fraud, Audit
money.transferred	Transaction	Notification, Fraud, Audit
fraud.detected	Fraud	Audit
📊 Observability
Tool	URL
Grafana	http://localhost:3000
Prometheus	http://localhost:9090
Zipkin	http://localhost:9411
Metrics collected:
Request throughput
p99 latency
JVM metrics
HTTP 5xx errors
Active threads
📈 Scalability
Horizontally scalable services
Stateless authentication
Event-driven architecture
Service decoupling
Distributed tracing
Independent deployments
🗺️ Roadmap
 Auth Service
 Ledger Banking System
 Kafka Event Streaming
 Fraud Detection Engine
 Audit Trail
 Observability Stack
 AES-256-GCM Encryption
 React Dashboard
 GitHub Actions CI/CD
 mTLS Between Services
 Kubernetes Deployment
👨‍💻 Author
Edson Junior
GitHub:
https://github.com/edsonjunior7
<p align="center"> Built with enterprise backend architecture principles. </p> ```
