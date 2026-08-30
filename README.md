# ResolveHub

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green)](https://spring.io/projects/spring-security)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-blue)](https://www.docker.com/)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%2017-blue)](https://www.postgresql.org/)
[![Documentation](https://img.shields.io/badge/API%20Docs-OpenAPI%20%2F%20Swagger-green)](http://localhost:8081/swagger-ui.html)
[![Tests](https://img.shields.io/badge/Tests-57%20Passed-success)](src/test/java)

**ResolveHub** is an enterprise-grade issue-tracking and resolution platform built with **Java 21, Spring Boot, Spring Security, Spring MVC, Spring Data JPA, Hibernate, PostgreSQL, and Docker Compose**.

The system models a real-world ticket management workflow where users authenticate via **HTTP Basic Auth**, are authorized using strict **Role-Based Access Control (RBAC)** (`REPORTER`, `AGENT`, `ADMIN`), and can create, assign, update, filter, comment on, and audit issues across projects with transaction-safe state machines and centralized error handling.

---

## Table of Contents
- [Quickstart with Docker Compose](#quickstart-with-docker-compose)
- [Docker Architecture & Networking](#docker-architecture--networking)
- [Features](#features)
- [Architecture & Security Flow](#architecture--security-flow)
- [Security & Authentication Model](#security--authentication-model)
- [Domain Model](#domain-model)
- [OpenAPI & Swagger UI](#openapi--swagger-ui)
- [REST API Endpoints & Permissions](#rest-api-endpoints--permissions)
- [Dynamic Filtering & Search](#dynamic-filtering--search)
- [Audit Activities & Comments](#audit-activities--comments)
- [Global Exception Handling](#global-exception-handling)
- [Environment Configuration & Profiles](#environment-configuration--profiles)
- [Automated Testing](#automated-testing)
- [Docker Commands Cheat Sheet](#docker-commands-cheat-sheet)
- [Project Structure](#project-structure)

---

## Quickstart with Docker Compose

Running ResolveHub with a production-grade PostgreSQL database requires only Docker and Docker Compose.

### 1. Clone & Configure Environment
```bash
git clone https://github.com/ayesha/ResolveHub.git
cd ResolveHub

# Optional: customize local environment settings
cp .env.example .env
```

### 2. Build and Start Services
```bash
docker compose up --build -d
```

### 3. Verify Container Status
```bash
docker compose ps
```
You should see `resolvehub-postgres` marked as `healthy` and `resolvehub-app` running on port `8081`.

### 4. Explore Interactive API Documentation
Open your browser and navigate to:
- **Swagger UI**: [`http://localhost:8081/swagger-ui.html`](http://localhost:8081/swagger-ui.html)
- **OpenAPI 3.0 Spec**: [`http://localhost:8081/v3/api-docs`](http://localhost:8081/v3/api-docs)

Click the **Authorize** button and log in with default development credentials:
- **Admin**: `admin@resolvehub.com` / `admin123`
- **Support Agent**: `agent@resolvehub.com` / `agent123`
- **Reporter**: `reporter@resolvehub.com` / `reporter123`

### 5. Stop Containers
```bash
# Stop containers while preserving database volume
docker compose down
```

---

## Docker Architecture & Networking

```mermaid
flowchart TD

    subgraph Host["Host Machine"]
        Client["Browser / REST Client"]
        Swagger["Swagger UI (localhost:8081)"]
    end

    subgraph DockerNetwork["Docker Bridge Network (resolvehub_default)"]
        subgraph AppContainer["resolvehub-app (Container)"]
            App["ResolveHub Spring Boot App\n(Port 8081)"]
        end

        subgraph PostgresContainer["resolvehub-postgres (Container)"]
            DB[("PostgreSQL 17\n(Port 5432)")]
            Health["Healthcheck: pg_isready"]
        end
    end

    subgraph PersistentStorage["Docker Named Volume"]
        Volume[("postgres_data\n(/var/lib/postgresql/data)")]
    end

    Client -->|http://localhost:8081| App
    Swagger -->|http://localhost:8081| App
    App -->|"jdbc:postgresql://postgres:5432/resolvehub"| DB
    DB <--> Volume
    Health -.->|"service_healthy check"| App
```

### Key Architectural Concepts:
1. **Container $\neq$ Virtual Machine**: A container shares the host OS kernel and isolates processes, memory, and filesystem without hypervisor overhead.
2. **Docker Image $\rightarrow$ Container**: An image is a read-only template; a container is an active running instance of that image.
3. **Internal Hostname Resolution**: Inside the Docker Compose network, services communicate using service names (`postgres:5432`) as DNS hostnames rather than `localhost:5432`.
4. **Volume Persistence**: PostgreSQL data is stored in the named Docker volume `postgres_data`, ensuring database rows persist across container restarts (`docker compose down`).

---

## Features

- **Containerized Deployment**: Multi-stage Dockerfile using Eclipse Temurin Java 21 with a lightweight non-root runtime container.
- **Docker Compose Orchestration**: Automated PostgreSQL provisioning, health checks (`pg_isready`), and dependency sequencing (`condition: service_healthy`).
- **Role-Based Access Control (RBAC)**: Spring Security integration with `REPORTER`, `AGENT`, and `ADMIN` roles.
- **BCrypt Password Hashing**: Passwords hashed with salted BCrypt before persistence and strictly protected from JSON serialization and logs.
- **Stateless REST Security**: HTTP Basic authentication over stateless sessions with explicit CSRF disabling justification.
- **Interactive OpenAPI Documentation**: Embedded Swagger UI 3.0 specification with `basicAuth` authentication support.
- **Transactional State Transitions**: Enforces valid ticket status transitions (`OPEN` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED`).
- **Dynamic Filtering with JPA Specifications**: Composable multi-criteria search without combinatorial repository methods.
- **Database Pagination & Whitelist Sorting**: Database-level `LIMIT`/`OFFSET` queries with sort field whitelisting to protect against injection.
- **Automated Audit Logging**: Append-only activity history tracking ticket creation, status changes, assignments, and priority updates.
- **Paginated Discussions**: Ticket comments with author metadata and N+1 query prevention using `@EntityGraph`.
- **Centralized Exception Handling**: Uniform REST error responses via `@RestControllerAdvice` and `ApiErrorResponse` DTOs.
- **Comprehensive Automated Test Suite**: 57 automated tests covering Security, Unit, MockMvc, JPA Data, and Integration workflows.

---

## Security & Authentication Model

### Core Concepts: Authentication vs Authorization
- **Authentication ("Who are you?")**: Identifies the calling principal using their email and BCrypt-verified password credentials via HTTP Basic header.
- **Authorization ("What are you allowed to do?")**: Determines whether the authenticated principal possesses the required `Role` (`ROLE_REPORTER`, `ROLE_AGENT`, `ROLE_ADMIN`) to access the requested URI and HTTP method.

### Role Hierarchy & Permissions
| Role | Permitted Operations |
|---|---|
| **`PUBLIC`** | OpenAPI specifications (`/v3/api-docs/**`) and Swagger UI (`/swagger-ui/**`, `/swagger-ui.html`) |
| **`REPORTER`** | View tickets & search, view ticket activities, post tickets, post comments |
| **`AGENT`** | All `REPORTER` capabilities + assign tickets, update ticket status, update ticket details |
| **`ADMIN`** | Complete system access including deleting tickets, project management, and user administration |

### Default Development Credentials
When running locally with the default/dev profile, ResolveHub initializes the following seed accounts:

| Email | Password | Role |
|---|---|---|
| `admin@resolvehub.com` | `admin123` | `ADMIN` |
| `agent@resolvehub.com` | `agent123` | `AGENT` |
| `reporter@resolvehub.com` | `reporter123` | `REPORTER` |

---

## REST API Endpoints & Permissions

| Method | Endpoint | Required Role | Description |
|---|---|---|---|
| `GET` | `/v3/api-docs/**` | `PUBLIC` | Raw OpenAPI 3.0 specification |
| `GET` | `/swagger-ui/**` | `PUBLIC` | Interactive Swagger UI assets |
| `GET` | `/api/tickets` | `REPORTER`, `AGENT`, `ADMIN` | Search tickets with dynamic filters, pagination, and sorting |
| `GET` | `/api/tickets/{id}` | `REPORTER`, `AGENT`, `ADMIN` | Retrieve single ticket details by ID |
| `POST` | `/api/tickets` | `REPORTER`, `AGENT`, `ADMIN` | Create a new ticket (status `OPEN`, logs `CREATED` activity) |
| `PUT` | `/api/tickets/{id}` | `AGENT`, `ADMIN` | Update ticket title, description, and priority |
| `PATCH` | `/api/tickets/{id}/assignee` | `AGENT`, `ADMIN` | Assign ticket to user (logs `ASSIGNED` activity) |
| `PATCH` | `/api/tickets/{id}/status` | `AGENT`, `ADMIN` | Transition ticket status (`OPEN` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED`) |
| `PATCH` | `/api/tickets/{id}/assign-and-start` | `AGENT`, `ADMIN` | Atomically assign ticket and set status to `IN_PROGRESS` |
| `GET` | `/api/tickets/{id}/activities` | `REPORTER`, `AGENT`, `ADMIN` | Retrieve audit history ordered newest to oldest |
| `POST` | `/api/tickets/{id}/comments` | `REPORTER`, `AGENT`, `ADMIN` | Add a discussion comment to a ticket |
| `GET` | `/api/tickets/{id}/comments` | `REPORTER`, `AGENT`, `ADMIN` | Retrieve paginated comments for a ticket (newest first) |
| `DELETE` | `/api/tickets/{id}` | `ADMIN` (`@PreAuthorize`) | Delete ticket (cascades to activities and comments) |

---

## Dynamic Filtering & Search

The search endpoint `GET /api/tickets` uses composable **Spring Data JPA Specifications** to generate optimized SQL `WHERE` clauses dynamically:

### Supported Query Parameters
- `status`: e.g. `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`
- `priority`: e.g. `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `projectId`: Filter by project ID
- `assigneeId`: Filter by assigned user ID
- `reporterId`: Filter by reporting user ID
- `search`: Case-insensitive text search matching `title` OR `description`
- `createdAfter` / `createdBefore`: ISO-8601 timestamps (`YYYY-MM-DDTHH:MM:SS`)
- `page`: Page index (default `0`)
- `size`: Page size (default `10`, max `100`)
- `sort`: Whitelisted sort field (`createdAt`, `updatedAt`, `priority`, `status`, `title`, `id`)
- `direction`: `asc` or `desc` (default `desc`)

---

## Global Exception Handling

All API errors are intercepted by `GlobalExceptionHandler` (`@RestControllerAdvice`) and formatted as a consistent `ApiErrorResponse`:

```json
{
  "timestamp": "2026-08-30T22:35:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied: insufficient permissions to access this resource",
  "path": "/api/tickets/1"
}
```

---

## Environment Configuration & Profiles

ResolveHub separates base configuration from environment-specific profiles. Hardcoded secrets are never stored in source control.

### Environment Variables
| Variable | Default Value | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://postgres:5432/resolvehub` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `password` | Database password |
| `HIBERNATE_DDL_AUTO` | `update` | Hibernate schema mode (`update`, `validate`, `none`) |
| `SHOW_SQL` | `false` | Enable SQL query printing |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active profile (`dev`, `prod`, `test`) |

---

## Automated Testing

ResolveHub includes 57 automated tests with 100% pass rate:

```bash
mvn clean test
```

### Test Suite Summary
1. **Security Integration Tests (`SecurityIntegrationTest`)**: 10 tests verifying 401 unauthenticated, 401 invalid credentials, 403 RBAC violations, 200 permitted role actions, Swagger doc availability, and password non-leakage.
2. **Service Unit Tests (`TicketServiceTest`)**: 23 Mockito tests verifying business rules, workflows, status transitions, activities, and comments.
3. **Controller Tests (`TicketControllerTest`)**: 14 Web-layer MockMvc tests with `@WithMockUser` verifying validation and DTO mapping.
4. **Repository Tests (`TicketRepositoryTest`)**: 9 `@DataJpaTest` tests verifying Specifications, date queries, and `@EntityGraph`.
5. **Workflow Integration Tests (`TicketWorkflowIntegrationTest`)**: 1 `@SpringBootTest` validating full multi-step ticket lifecycles.

---

## Docker Commands Cheat Sheet

| Command | Purpose |
|---|---|
| `docker compose up --build -d` | Build images, start containers in background, and create volumes |
| `docker compose ps` | View status and health of all orchestrated containers |
| `docker compose logs -f app` | Follow real-time application logs for ResolveHub |
| `docker compose logs -f postgres` | Follow real-time PostgreSQL database logs |
| `docker compose down` | Gracefully stop and remove containers and network (**preserves database data**) |
| `docker compose down -v` | Stop containers and **delete all database volumes** (wipes persistent storage) |

---

## Project Structure

```text
src/
├── main/
│   ├── java/com/ayesha/resolvehub/
│   │   ├── config/             # SecurityConfig, OpenApiConfig, SecurityDataInitializer
│   │   ├── controller/         # REST API endpoints & Swagger annotations
│   │   ├── dto/                # Request & Response DTOs with @Schema
│   │   ├── entity/             # JPA Entities (User, Role, Ticket, Project, Activity, Comment)
│   │   ├── exception/          # Domain exceptions & GlobalExceptionHandler
│   │   ├── repository/         # Spring Data repositories & JPA Specifications
│   │   │   ├── projection/     # Interface-based projections (TicketSummary)
│   │   │   └── specification/  # Composable Specifications (TicketSpecification)
│   │   ├── security/           # CustomUserDetailsService
│   │   └── service/            # Transactional business logic
│   └── resources/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── test/
    ├── java/com/ayesha/resolvehub/
    │   ├── controller/         # TicketControllerTest (MockMvc + @WithMockUser)
    │   ├── integration/        # SecurityIntegrationTest, TicketWorkflowIntegrationTest
    │   ├── repository/         # TicketRepositoryTest (DataJpaTest)
    │   └── service/            # TicketServiceTest (Mockito)
    └── resources/
        └── application.properties # Isolated H2 test config
├── .dockerignore               # Docker build context exclusion rules
├── .env.example                # Example environment configuration
├── Dockerfile                  # Multi-stage Java 21 build & runtime
├── docker-compose.yml          # PostgreSQL & App orchestration with healthcheck
└── pom.xml                     # Maven project definition
```

---

**ResolveHub** · Java 21 · Spring Boot 3.5.5 · Spring Security 6 · PostgreSQL 17 · Docker Compose · OpenAPI 3.0  
© 2026 Ayesha
