# 🏦 BankSim — Full-Stack Banking Simulator

> Microservices architecture with Spring Boot, JWT security, API Gateway, and a React frontend — built from scratch as a portfolio project.

**BankSim** is a banking simulation that covers the full software engineering lifecycle: from designing a distributed backend with domain-separated services, to building a modern React UI that consumes protected APIs. The goal is to demonstrate real-world engineering practices in a project that runs entirely with `docker compose up`.

---

## ⚡ Key Engineering Highlights

| Area | What's Demonstrated |
|------|---------------------|
| **Architecture** | Microservices with domain separation (Auth, Account), API Gateway as single entry point |
| **Security** | RSA-signed JWT (custom issuer + JWKS), token blacklist via Redis, service-to-service auth |
| **Data** | JPA with Flyway migrations, pessimistic locking for concurrent transfers, per-service databases |
| **Frontend** | React 19 + TypeScript SPA with protected routes, auth context, Axios interceptors, dark/light theme |
| **DevOps** | Multi-stage Docker builds, Docker Compose orchestration, Nginx for SPA serving |
| **Observability** | End-to-end Correlation ID propagation across gateway and all services |

---

## 🏗 Architecture Overview

```text
┌──────────────────┐
│  React Frontend  │  (Vite + TypeScript + Tailwind)
│   localhost:3000  │
└────────┬─────────┘
         │ HTTP
         ▼
┌──────────────────────────────────────────────────────────┐
│                    API Gateway                           │
│         Spring Cloud Gateway + WebFlux                   │
│   Routing · CORS · JWT Blacklist Filter · Correlation ID │
│                  localhost:8080                           │
└──────┬──────────────────────────────────────┬────────────┘
       │                                      │
       ▼                                      ▼
┌──────────────┐                     ┌─────────────────┐
│ Auth Service │                     │ Account Service  │
│  Spring Boot │──── service token ──│   Spring Boot    │
│  + Security  │                     │   + JPA          │
└──────┬───────┘                     └────────┬────────┘
       │                                      │
       ▼                                      ▼
┌──────────────┐  ┌───────┐          ┌────────────────┐
│ Postgres     │  │ Redis │          │   Postgres     │
│ (auth)       │  │       │          │   (account)    │
└──────────────┘  └───────┘          └────────────────┘
```

**Docker Compose services:** `gateway` · `auth-service` · `account-service` · `postgres-auth` · `postgres-account` · `redis` · `rabbitmq`* · `frontend`

> \* RabbitMQ infrastructure is provisioned and ready; event-driven integration is planned for the next iteration.

---

## 📁 Repository Modules

| Module | Purpose |
|--------|---------|
| `auth-service` | User registration, login, JWT issuing (RSA + JWKS), logout with Redis blacklist, profile management |
| `account-service` | Account lifecycle, balance queries, contact lookup by email, money transfers with concurrency control |
| `gateway` | Single entry point — routing, CORS, edge security (blacklist filter), correlation ID injection |
| `common-lib` | Shared configs reused across services (Redis, RestClient, security utilities) |
| `front-end-react` | React SPA — authentication flow, dashboard, transfers, account management, theming |

---

## ✅ Implemented Features

### Backend

- **User registration** (`POST /auth/register`) with username, password strength, email, and location validation — creates `User` + `UserProfile` and automatically provisions an account in `account-service` via service token
- **Login** (`POST /auth/login`) returns an RSA-signed JWT
- **JWKS endpoint** (`GET /.well-known/jwks.json`) for standard key distribution
- **Logout** (`POST /auth/logout`) with Redis-backed token blacklist (by `jti`, auto-expires with token)
- **Account balance** (`GET /account/me`) and **profile** (`GET /auth/user/profile`)
- **Money transfer** (`POST /account/transfer`) with balance validation, `PESSIMISTIC_WRITE` locking, and transactional persistence
- **Recent contacts** (`GET /account/contacts/recent`) derived from transfer history
- **Recipient lookup** (`GET /account/{email}`) for transfer flow validation
- **Correlation ID** propagated across gateway → services for request traceability
- **Standardized error handling** with global exception handlers and consistent API responses
- **Flyway migrations** for safe, versioned schema evolution (no `ddl-auto` in production)

### Frontend

- **Full authentication flow**: register → login → JWT stored in `localStorage` → auto-redirect on 401 → logout with backend blacklist call
- **Protected routing**: `ProtectedRoute` wrapper redirects unauthenticated users; auth pages redirect logged-in users to dashboard
- **Dashboard**: live account balance from API, personalized greeting, quick-action buttons (Send Money, Request Money, Cards)
- **Send Money modal** with multi-step UX:
  - Recent contacts carousel (fetched from backend transfer history)
  - Email-based recipient lookup with real-time validation
  - Balance check → confirmation step → receipt screen
  - Animated step transitions using Framer Motion `AnimatePresence`
- **Account page**: profile data fetched from backend (email, location, name) with edit modal
- **Activity page**: transaction list with detail modal, stat cards
- **Settings page**: theme toggle (dark/light), notification and security placeholders
- **Global theming**: CSS custom properties with `light`/`dark` class toggle, persisted in `localStorage`
- **Axios interceptor layer**: automatic JWT injection, 401 detection with redirect, centralized error extraction
- **Type safety**: TypeScript interfaces for all API contracts (`Account`, `Profile`, `RecentContact`, `RecipientAccount`)
- **Production Docker build**: multi-stage (Node → Nginx), SPA fallback routing via nginx config

---

## 🛠 Tech Stack

### Backend
| Technology | Usage |
|-----------|-------|
| Java 25 | Language runtime |
| Spring Boot 4 | Application framework |
| Spring Security + OAuth2 Resource Server | JWT validation via JWKS |
| Spring Data JPA | Persistence layer |
| Spring Cloud Gateway (WebFlux) | API Gateway |
| Flyway | Database migrations |
| PostgreSQL | Per-service relational storage |
| Redis | Token blacklist + shared cache |
| Maven (multi-module) | Build and dependency management |

### Frontend
| Technology | Usage |
|-----------|-------|
| React 19 + TypeScript | UI framework with type safety |
| Vite 8 | Build tool and dev server |
| React Router 7 | Client-side routing with protected routes |
| Axios | HTTP client with interceptors |
| Tailwind CSS 4 | Utility-first styling with custom theme |
| Framer Motion | Page transitions and micro-animations |
| Recharts | Data visualization (spending chart) |
| Lucide React | Icon system |

### Infrastructure
| Technology | Usage |
|-----------|-------|
| Docker + Docker Compose | Container orchestration |
| Nginx | Frontend static serving with SPA fallback |

---

## 🚀 Run Locally

### Prerequisites

- Docker + Docker Compose
- *Optional (for non-container dev):* Java 25, Maven 3.9+, Node 22+

### 1. Generate JWT Keys

If they don't exist yet:

```bash
cp secrets/auth/private.pem.example secrets/auth/private.pem
cp secrets/auth/public.pem.example secrets/auth/public.pem
```

Replace the contents with a valid RSA 2048-bit key pair.

### 2. Start the Backend

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| Gateway | `http://localhost:8080` |
| Auth Service (direct) | `http://localhost:8081` |
| Account Service (direct) | `http://localhost:8082` |

### 3. Start with Frontend

```bash
docker compose --profile frontend up --build
```

| Service | URL |
|---------|-----|
| Frontend | `http://localhost:3000` |

### 4. Java Dev Mode (hot reload)

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up
```

---

## 📡 API Reference

### Auth Service (`/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Authenticate and receive JWT |
| `POST` | `/auth/logout` | Invalidate token (Redis blacklist) |
| `GET` | `/auth/me` | Get authenticated user info |
| `GET` | `/auth/user/profile` | Get user profile (email, location) |
| `PUT` | `/auth/user/profile` | Update profile *(endpoint exists, persistence pending)* |

### Account Service (`/account`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/account/me` | Get account details + balance |
| `GET` | `/account/contacts/recent` | Recent transfer contacts |
| `GET` | `/account/{email}` | Lookup account by email |
| `POST` | `/account/transfer` | Transfer money to another account |

### JWKS
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/.well-known/jwks.json` | Public key set for JWT verification |

---

## 📋 Current Status

### ✅ Complete
- End-to-end authentication/authorization with RSA JWT
- Automatic account provisioning on registration (cross-service via service token)
- Money transfer flow with transactional integrity and pessimistic locking
- Functional frontend covering the core user journey (register → login → view balance → send money → view account)
- Docker Compose orchestration for the full stack

### 🔧 Partially Implemented
- `PUT /auth/user/profile` — endpoint exists but changes are not persisted yet
- Activity and Settings pages use mock data for UI demonstration purposes
- Transfer receipt screen is a placeholder (transfer itself works end-to-end)

### 🗺 Roadmap
- **RabbitMQ integration** — asynchronous domain events for cross-service communication
- **Full transaction history** — backend persistence + frontend consumption
- **Testing** — unit tests for business rules, integration tests, Testcontainers
- **API documentation** — OpenAPI / Swagger
- **Security hardening** — refresh token rotation, stricter internal endpoint protection

---

## 📐 Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Per-service databases** | Enforces domain boundaries; each service owns its data |
| **JWKS for key distribution** | Standard-compliant; gateway validates tokens without sharing private keys |
| **Redis token blacklist** | Enables stateless JWT with logout capability; entries auto-expire with token TTL |
| **Pessimistic locking on transfers** | Prevents double-spend race conditions in concurrent scenarios |
| **Axios interceptors** | Centralizes auth header injection and 401 handling in a single place |
| **CSS custom properties for theming** | Enables dark/light mode toggle without re-rendering the component tree |
| **Zero cloud dependency** | Entire stack runs locally via Docker — no cloud accounts, no costs, no external APIs |

---

<p align="center">
  <sub>Built as a portfolio project to demonstrate full-stack and distributed systems skills.</sub>
</p>
