# ResolveHub — Technical Architecture & Interview Revision Guide

A comprehensive, engineering-focused technical summary of ResolveHub designed for fast revision prior to technical interviews.

---

## Table of Contents
1. [Why ResolveHub Exists](#1-why-resolvehub-exists)
2. [Layered Architecture](#2-layered-architecture)
3. [HTTP Request Lifecycle](#3-http-request-lifecycle)
4. [Spring MVC & DispatcherServlet](#4-spring-mvc--dispatcherservlet)
5. [Dependency Injection & IoC](#5-dependency-injection--ioc)
6. [JPA & Hibernate ORM](#6-jpa--hibernate-orm)
7. [Persistence Context & Dirty Checking](#7-persistence-context--dirty-checking)
8. [EntityManager](#8-entitymanager)
9. [Relational Entity Mappings](#9-relational-entity-mappings)
10. [Fetch Strategies (LAZY vs EAGER)](#10-fetch-strategies-lazy-vs-eager)
11. [N+1 Query Problem & Solutions](#11-n1-query-problem--solutions)
12. [Query Strategies (Derived, JPQL, Criteria)](#12-query-strategies-derived-jpql-criteria)
13. [Dynamic JPA Specifications](#13-dynamic-jpa-specifications)
14. [Database Pagination & Whitelist Sorting](#14-database-pagination--whitelist-sorting)
15. [Interface-Based Projections](#15-interface-based-projections)
16. [Transaction Management & Boundaries](#16-transaction-management--boundaries)
17. [Ticket State Machine Workflow](#17-ticket-state-machine-workflow)
18. [Ticket Comments & Discussions](#18-ticket-comments--discussions)
19. [Audit Activity Log (History Trail)](#19-audit-activity-log-history-trail)
20. [Centralized Global Exception Handling](#20-centralized-global-exception-handling)
21. [Spring Security 6 Architecture](#21-spring-security-6-architecture)
22. [Authentication vs Authorization](#22-authentication-vs-authorization)
23. [Docker & Container Orchestration](#23-docker--container-orchestration)
24. [Automated Testing Strategy](#24-automated-testing-strategy)
25. [Key Design Tradeoffs & Decisions](#25-key-design-tradeoffs--decisions)

---

### 1. Why ResolveHub Exists
- **WHAT**: A production-grade issue tracking and resolution backend.
- **WHY**: Unifies issue tracking, status workflows, team assignment, discussions, and audit logs into a high-performance RESTful service with role-based security.
- **HOW**: Implemented with Java 21, Spring Boot 3.5.5, Spring Security 6, Spring Data JPA, Hibernate, PostgreSQL, and Docker Compose.

---

### 2. Layered Architecture
- **WHAT**: Strict separation across Controller $\rightarrow$ DTO $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ JPA/Hibernate $\rightarrow$ PostgreSQL.
- **WHY**: Eliminates tight coupling, isolates business rules from HTTP/transport concerns, and prevents database entity proxies from leaking across API boundaries.
- **HOW**:
  - `Controller`: Routing, validation (`@Valid`), OpenAPI documentation.
  - `DTO`: Request/Response contracts protecting internal entity structure.
  - `Service`: Business logic, state machines, atomic transactions (`@Transactional`).
  - `Repository`: Data access abstractions using Spring Data interfaces.

---

### 3. HTTP Request Lifecycle
- **WHAT**: Complete path taken by an incoming HTTP request.
- **WHY**: Predictable request handling, error interception, and authentication.
- **HOW**:
  ```text
  HTTP Request 
    ↓ [SecurityFilterChain: Auth check, Credentials verification]
  DispatcherServlet
    ↓ [HandlerMapping: Resolves Controller Method]
  HandlerAdapter
    ↓ [Validation: @Valid on Request DTO]
  Controller
    ↓ [Maps DTO to Parameters]
  Service Layer (@Transactional)
    ↓ [Business rules, Entity mutations]
  Repository (Spring Data JPA)
    ↓ [Hibernate SQL generation]
  PostgreSQL Database
    ↓ [Result mapped back to Entities]
  Service (Maps Entities -> Response DTO)
    ↓
  Controller -> HTTP 200/201/400/401/403/404 JSON Response
  ```

---

### 4. Spring MVC & DispatcherServlet
- **WHAT**: Front controller pattern orchestrating incoming web traffic.
- **WHY**: Centralizes request routing, parameter binding, content negotiation, and exception resolution.
- **HOW**: `DispatcherServlet` routes matching URI patterns via `RequestMappingHandlerMapping` to `@RestController` methods, employing Jackson for JSON serialization.

---

### 5. Dependency Injection & IoC
- **WHAT**: Inversion of Control container managing object creation and lifecycle.
- **WHY**: Enables loose coupling, testability with mock dependencies, and single-responsibility components.
- **HOW**: Constructor injection throughout all Controllers, Services, and Configurations (e.g. `public TicketController(TicketService ticketService) { ... }`).

---

### 6. JPA & Hibernate ORM
- **WHAT**: Jakarta Persistence API standard implemented by Hibernate Object-Relational Mapper.
- **WHY**: Bridges the object-oriented domain model with PostgreSQL relational tables, managing SQL generation and type conversions.
- **HOW**: Annotating entity classes (`@Entity`, `@Table`, `@Id`, `@ManyToOne`, `@OneToMany`) and executing CRUD through `JpaRepository`.

---

### 7. Persistence Context & Dirty Checking
- **WHAT**: First-level transactional cache managed by Hibernate.
- **WHY**: Minimizes database round-trips and eliminates explicit `repository.save()` calls on modified attached entities.
- **HOW**: When a transaction commits, Hibernate compares entity snapshots against current state (dirty checking) and automatically issues necessary SQL `UPDATE` statements.

---

### 8. EntityManager
- **WHAT**: Core JPA interface interacting directly with the persistence context.
- **WHY**: Allows low-level query execution, manual entity lifecycle management (persist, merge, remove, detach), and custom JPQL queries.
- **HOW**: Injected via `@PersistenceContext private EntityManager entityManager;` in `TicketService` for internal demonstrations.

---

### 9. Relational Entity Mappings
- **WHAT**: Object relationships representing foreign keys and collections.
- **WHY**: Maintains database integrity while enabling navigation between related entities.
- **HOW**:
  - `Ticket` $\rightarrow$ `Project` (`@ManyToOne(fetch = FetchType.LAZY)`)
  - `Ticket` $\rightarrow$ `User` (`reporter`, `assignee`) (`@ManyToOne(fetch = FetchType.LAZY)`)
  - `Ticket` $\rightarrow$ `TicketComment` (`@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)`)
  - `Ticket` $\rightarrow$ `TicketActivity` (`@OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)`)

---

### 10. Fetch Strategies (LAZY vs EAGER)
- **WHAT**: Controlling when associated entity records are loaded from the database.
- **WHY**: Default `EAGER` loading causes accidental table joins, massive memory consumption, and severe N+1 performance bottlenecks.
- **HOW**: All `@ManyToOne` and `@OneToMany` relationships configured with `FetchType.LAZY`. Data is loaded on-demand or eagerly fetched intentionally.

---

### 11. N+1 Query Problem & Solutions
- **WHAT**: Executing 1 query to fetch $N$ parent records, followed by $N$ secondary queries to fetch child associations.
- **WHY**: Destroys database performance and exhausts connection pools under load.
- **HOW**:
  1. **`@EntityGraph`**: Eagerly fetches associations in a single SQL `JOIN` on repository methods (e.g. `TicketCommentRepository.findByTicketIdOrderByCreatedAtDesc` with `attributePaths = {"author"}`).
  2. **`JOIN FETCH` in JPQL**: Directly fetches parent and related entities in custom queries (e.g. `findAllWithProjects`).
  3. **Projections**: Queries only needed scalar columns without loading full entities.

---

### 12. Query Strategies (Derived, JPQL, Criteria)
- **WHAT**: Multiple query mechanisms for different complexity levels.
- **WHY**: Balances simplicity for basic CRUD with high performance for complex dynamic queries.
- **HOW**:
  - *Derived Queries*: `findByStatus(String status)` parsed from method name.
  - *JPQL Queries*: `@Query("SELECT t FROM Ticket t WHERE ...")` for explicit joins and filters.
  - *Criteria API*: programmatic SQL generation for dynamic search.

---

### 13. Dynamic JPA Specifications
- **WHAT**: Composable `Specification<Ticket>` predicates utilizing `CriteriaBuilder`.
- **WHY**: Avoids writing $2^N$ combinatorial repository query methods for multi-criteria search.
- **HOW**: Static methods in `TicketSpecification` (`hasStatus`, `hasPriority`, `hasProjectId`, `hasAssigneeId`, `hasReporterId`, `search`, `createdAfter`, `createdBefore`) combined seamlessly with `.and()`.

---

### 14. Database Pagination & Whitelist Sorting
- **WHAT**: Efficient `LIMIT` and `OFFSET` queries with strict sort field validation.
- **WHY**: Prevents memory exhaustion from loading large datasets into RAM and prevents SQL injection via sort parameters.
- **HOW**: `PageRequest.of(page, size, Sort.by(...))` coupled with a whitelist `Set.of("createdAt", "updatedAt", "priority", "status", "title", "id")`.

---

### 15. Interface-Based Projections
- **WHAT**: Spring Data JPA lightweight interface selecting specific columns.
- **WHY**: Avoids loading full entity graphs when only a few fields are needed (e.g., ticket summary dashboards).
- **HOW**: `TicketSummary` interface exposing `getId()`, `getTitle()`, `getStatus()`, `getPriority()`, `getProjectName()`.

---

### 16. Transaction Management & Boundaries
- **WHAT**: `@Transactional` boundaries at the service layer.
- **WHY**: Guarantees ACID compliance—multi-table mutations (e.g. updating ticket status + appending audit activity log) succeed or fail as an atomic unit.
- **HOW**: `@Transactional` placed on service methods (`createTicket`, `updateTicketStatus`, `assignTicketAndStart`, `createComment`).

---

### 17. Ticket State Machine Workflow
- **WHAT**: Enforced lifecycle transitions: `OPEN` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED`.
- **WHY**: Prevents invalid business states (e.g., skipping from `OPEN` straight to `CLOSED` or reopening `CLOSED` tickets).
- **HOW**: Validated in `TicketService.validateStatusTransition()`, throwing `InvalidTicketStatusTransitionException` (HTTP 400) on violations.

---

### 18. Ticket Comments & Discussions
- **WHAT**: Paginated discussion comments attached to tickets.
- **WHY**: Allows collaboration between reporters, agents, and team leads.
- **HOW**: `POST /api/tickets/{id}/comments` and `GET /api/tickets/{id}/comments` (paginated, newest first, author eagerly fetched with `@EntityGraph`).

---

### 19. Audit Activity Log (History Trail)
- **WHAT**: Append-only immutable log recording state changes.
- **WHY**: Accountability and transparency tracking who changed what and when.
- **HOW**: `TicketActivity` entity capturing `action` (`CREATED`, `STATUS_CHANGED`, `ASSIGNED`, `PRIORITY_CHANGED`), `oldValue`, `newValue`, and timestamp.

---

### 20. Centralized Global Exception Handling
- **WHAT**: `@RestControllerAdvice` intercepting exceptions and returning structured JSON error bodies.
- **WHY**: Consistent API contracts across all errors (validation, not found, security, type mismatch, internal errors).
- **HOW**: `GlobalExceptionHandler` mapping exceptions to standard `ApiErrorResponse` DTO with timestamps, status codes, paths, and field-level validation error maps.

---

### 21. Spring Security 6 Architecture
- **WHAT**: Security filter chain providing authentication and role-based access control.
- **WHY**: Secures APIs, authenticates users, and prevents unauthorized access to sensitive endpoints.
- **HOW**: `SecurityFilterChain` configuring `BCryptPasswordEncoder`, stateless session policy, HTTP Basic authentication, custom JSON entry points, and `@PreAuthorize` method security.

---

### 22. Authentication vs Authorization
- **WHAT**:
  - **Authentication**: *"Who are you?"* $\rightarrow$ Identifies user via email & BCrypt password. Failure: **HTTP 401 Unauthorized**.
  - **Authorization**: *"What are you allowed to do?"* $\rightarrow$ Enforces roles (`REPORTER`, `AGENT`, `ADMIN`). Failure: **HTTP 403 Forbidden**.
- **WHY**: Separates identity verification from permission enforcement.
- **HOW**: Handled by `CustomUserDetailsService` and URL/method rules (`hasRole('ADMIN')`).

---

### 23. Docker & Container Orchestration
- **WHAT**: Multi-stage Dockerfile and Docker Compose setup with PostgreSQL 17.
- **WHY**: Reproducible, zero-configuration local and production deployment.
- **HOW**: Compose runs `resolvehub-app` and `resolvehub-postgres` on a private bridge network with health checks (`pg_isready`) and named volume `postgres_data`.

---

### 24. Automated Testing Strategy
- **WHAT**: Multi-layered test suite (57 tests, 100% pass rate).
- **WHY**: Validates business logic, web contracts, database queries, and security rules without regressions.
- **HOW**:
  - **Unit**: Mockito testing `TicketService` in isolation.
  - **Web**: MockMvc testing `TicketController` routing and validation.
  - **JPA**: `@DataJpaTest` testing `TicketRepository` and Specifications.
  - **Integration**: `@SpringBootTest` testing full ticket lifecycles.
  - **Security**: MockMvc testing HTTP Basic auth, RBAC permissions, and 401/403 responses.

---

### 25. Key Design Tradeoffs & Decisions
1. **Stateless REST vs Session Cookies**: Chose stateless HTTP Basic/Token architecture; explicitly disabled CSRF because ambient session cookies are not used.
2. **DTOs vs Direct Entity Exposure**: Used strict Request/Response DTOs to avoid lazy loading proxy serialization failures and hide sensitive fields like passwords.
3. **JPA Specifications vs Hardcoded JPQL**: Used Criteria Specifications for multi-criteria search to eliminate combinatorial repository query explosion.
4. **Targeted Fetching vs Global Eager**: Kept all mappings `LAZY` and selectively applied `@EntityGraph` / `JOIN FETCH` to eliminate N+1 queries without memory bloat.
