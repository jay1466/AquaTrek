# 💧 AquaTrack — Water Consumption & Billing Management Platform

> Enterprise-grade, multi-tenant SaaS application for apartment society water monitoring and billing.

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react)](https://react.dev/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)](https://docs.docker.com/compose/)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Modules](#modules)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Docker Deployment](#docker-deployment)
- [Environment Variables](#environment-variables)

---

## Overview

AquaTrack is a production-ready, multi-tenant water management platform designed for apartment societies. A single deployment supports multiple apartment societies with strict data isolation.

**Key Features:**
- 🏢 Multi-tenant architecture (one deployment, many apartment societies)
- 💧 Water meter management and reading collection (manual + CSV bulk upload)
- 💰 Tier-based billing engine with dynamic tariff support
- 🪣 Bulk water purchase tracking and cost distribution
- 📄 PDF invoice generation with QR codes
- 🚨 Anomaly detection and alert engine
- 📊 Analytics, trends, and consumption reporting
- 🔐 JWT authentication with refresh tokens, email verification, and role-based access control

---

## Architecture

```
┌─────────────────────────────────────────────┐
│              React Frontend                  │
│  MUI + Tailwind | React Query | React Hook  │
└───────────────────┬─────────────────────────┘
                    │ HTTPS/REST
┌───────────────────▼─────────────────────────┐
│           Spring Boot Backend                │
│                                             │
│  Controller → Service → Repository          │
│  JWT Auth | Tenant Isolation | Flyway       │
└───────────────────┬─────────────────────────┘
                    │ JDBC/HikariCP
┌───────────────────▼─────────────────────────┐
│              PostgreSQL 16                   │
│  Shared DB + Shared Schema                  │
│  Tenant Isolation via apartment_id          │
└─────────────────────────────────────────────┘
```

### Multi-Tenancy Strategy
- **Pattern**: Shared Database, Shared Schema
- **Isolation**: `apartment_id` column on every tenant-scoped table
- **Enforcement**: JWT embeds `apartmentId` claim; every service method validates it
- **Security**: `TenantAccessException` (403) on any cross-tenant access attempt

---

## Tech Stack

| Layer        | Technology                                         |
|--------------|----------------------------------------------------|
| Language     | Java 21 (virtual threads ready)                    |
| Framework    | Spring Boot 3.3.5                                  |
| Security     | Spring Security 6 + JWT (JJWT 0.12.3)             |
| ORM          | Spring Data JPA + Hibernate                        |
| Database     | PostgreSQL 16                                      |
| Migrations   | Flyway 10                                          |
| Mapping      | MapStruct 1.5.5                                    |
| Docs         | SpringDoc OpenAPI 2.3 (Swagger UI)                 |
| Build        | Maven 3.9                                          |
| Frontend     | React 18 + Vite 5                                  |
| UI           | Material UI v5 + Tailwind CSS v3                   |
| Data Fetching| TanStack React Query v5                            |
| Forms        | React Hook Form + Zod                              |
| Charts       | Recharts                                           |
| Container    | Docker + Docker Compose                            |
| Testing      | JUnit 5 + Mockito + Testcontainers                 |

---

## Getting Started

### Prerequisites
- Java 21+
- Node.js 20+
- Docker & Docker Compose
- Maven 3.9+

### Quick Start (Docker — Recommended)

```bash
# 1. Clone the repository
git clone https://github.com/your-org/aquatrack.git
cd aquatrack

# 2. Create environment file
cp .env.example .env
# Edit .env with your values (especially JWT_SECRET and mail credentials)

# 3. Start all services
docker compose up -d

# 4. Wait for services to be healthy (~60-90 seconds)
docker compose ps

# 5. Access the application
# Frontend:     http://localhost:5173
# Backend API:  http://localhost:8080
# Swagger UI:   http://localhost:8080/swagger-ui.html
# MailHog UI:   http://localhost:8025
```

### Local Development (Without Docker)

```bash
# ── Backend ────────────────────────────────────────────────────
cd backend

# Ensure PostgreSQL is running locally (or use the docker-compose postgres only)
docker compose up -d postgres mailhog

# Run the Spring Boot application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# ── Frontend ───────────────────────────────────────────────────
cd frontend
npm install
npm run dev
```

---

## Project Structure

```
aquatrack/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/aquatrack/
│   │   ├── AquaTrackApplication.java
│   │   ├── config/                   # Spring configuration classes
│   │   ├── constants/                # Application-wide constants
│   │   ├── entity/base/              # BaseEntity with audit fields
│   │   ├── enums/                    # All domain enumerations
│   │   ├── exception/                # Custom exceptions + global handler
│   │   ├── response/                 # ApiResponse + PagedResponse wrappers
│   │   └── utility/                  # Utility classes
│   ├── src/main/resources/
│   │   ├── application.yml           # Main config
│   │   ├── application-dev.yml       # Dev profile
│   │   ├── application-prod.yml      # Prod profile
│   │   ├── logback-spring.xml        # Logging config
│   │   └── db/migration/             # Flyway SQL scripts
│   ├── src/test/                     # Unit + integration tests
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                         # React application
│   ├── src/
│   │   ├── api/                      # Axios config + API services
│   │   ├── components/common/        # Reusable UI components
│   │   ├── context/                  # React Context providers
│   │   ├── pages/                    # Page-level components (added per module)
│   │   ├── theme/                    # MUI theme configuration
│   │   ├── styles/                   # Global CSS
│   │   ├── App.jsx                   # Root component + routing
│   │   └── main.jsx                  # Entry point with providers
│   ├── Dockerfile
│   ├── index.html
│   ├── package.json
│   ├── tailwind.config.js
│   └── vite.config.js
│
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

---

## Modules

| # | Module              | Status       | API Prefix              |
|---|---------------------|--------------|-------------------------|
| 0 | Project Foundation  | ✅ Complete   | —                       |
| 1 | Authentication      | ✅ Complete   | `/api/v1/auth`          |
| 2 | Apartment Mgmt      | ✅ Complete   | `/api/v1/apartments`    |
| 3 | Household Mgmt      | 🔄 Next       | `/api/v1/households`    |
| 4 | Water Meters        | ⏳ Pending    | `/api/v1/meters`        |
| 5 | Billing Engine      | ⏳ Pending    | `/api/v1/billing`       |
| 6 | Bulk Water Purchase | ⏳ Pending    | `/api/v1/bulk-water`    |
| 7 | Invoice Generation  | ⏳ Pending    | `/api/v1/invoices`      |
| 8 | Alert Engine        | ⏳ Pending    | `/api/v1/alerts`        |
| 9 | Analytics           | ⏳ Pending    | `/api/v1/analytics`     |

---

## API Documentation

Once the backend is running:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

All endpoints (except `/api/v1/auth/**`) require a JWT Bearer token.
Use the **Authorize** button in Swagger UI to set your token.

---

## Testing

```bash
cd backend

# Run all tests (requires Docker for Testcontainers)
./mvnw test

# Run with coverage report
./mvnw test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## Docker Deployment

```bash
# Build production images
docker compose build

# Start in production mode
SPRING_PROFILES_ACTIVE=prod docker compose up -d

# Scale the backend (if using a load balancer)
docker compose up -d --scale backend=3

# View logs
docker compose logs -f backend

# Stop everything
docker compose down

# Stop and remove all data volumes (DESTRUCTIVE)
docker compose down -v
```

---

## Environment Variables

See `.env.example` for the complete list. Critical variables:

| Variable        | Description                                    | Required |
|----------------|------------------------------------------------|----------|
| `JWT_SECRET`   | 256-bit secret for signing JWT tokens          | ✅ Yes   |
| `DB_PASSWORD`  | PostgreSQL password                            | ✅ Yes   |
| `MAIL_USERNAME`| SMTP username for sending emails               | ✅ Yes   |
| `MAIL_PASSWORD`| SMTP password / app password                   | ✅ Yes   |

---

## License

Proprietary — AquaTrack Engineering Team © 2026. All Rights Reserved.
