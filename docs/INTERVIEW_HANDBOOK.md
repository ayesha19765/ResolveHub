# ResolveHub — Master Technical Interview & Revision Handbook

> **A Senior Engineer's Deep-Dive Guide to Spring Boot 3, Spring Security 6, Hibernate 6, PostgreSQL 17, React 18, and Docker Architecture.**
> 
> *Written specifically from the concrete implementation of the ResolveHub codebase.*

---

## Table of Contents

- [PART 1 — ResolveHub Project Overview & Architecture](#part-1--resolvehub-project-overview--architecture)
- [PART 2 — Spring Core, IoC & Dependency Injection](#part-2--spring-core-ioc--dependency-injection)
- [PART 3 — Spring MVC & The HTTP Request Lifecycle](#part-3--spring-mvc--the-http-request-lifecycle)
- [PART 4 — DTOs, Bean Validation & Error Contracts](#part-4--dtos-bean-validation--error-contracts)
- [PART 5 — JPA, Hibernate & ORM Fundamentals](#part-5--jpa-hibernate--orm-fundamentals)
- [PART 6 — Relational Entity Mappings & Ownership](#part-6--relational-entity-mappings--ownership)
- [PART 7 — Persistence Context, EntityManager & Dirty Checking](#part-7--persistence-context-entitymanager--dirty-checking)
- [PART 8 — Spring Data JPA Repositories & Query Mechanisms](#part-8--spring-data-jpa-repositories--query-mechanisms)
- [PART 9 — JPA Specifications & Dynamic Search Architecture](#part-9--jpa-specifications--dynamic-search-architecture)
- [PART 10 — Pagination, Whitelist Sorting & Interface Projections](#part-10--pagination-whitelist-sorting--interface-projections)
- [PART 11 — The N+1 Query Problem & Fetching Optimizations](#part-11--the-n1-query-problem--fetching-optimizations)
- [PART 12 — Transaction Management (@Transactional Internals)](#part-12--transaction-management-transactional-internals)
- [PART 13 — Domain Business Logic & State Machine Workflows](#part-13--domain-business-logic--state-machine-workflows)
- [PART 14 — Comments & Audit Activity History System](#part-14--comments--audit-activity-history-system)
- [PART 15 — Centralized Global Exception Handling](#part-15--centralized-global-exception-handling)
- [PART 16 — Spring Security 6 & Role-Based Access Control](#part-16--spring-security-6--role-based-access-control)
- [PART 17 — Multi-Layered Automated Testing Strategy](#part-17--multi-layered-automated-testing-strategy)
- [PART 18 — Database Indexing & Query Performance](#part-18--database-indexing--query-performance)
- [PART 19 — Docker Containerization & Orchestration](#part-19--docker-containerization--orchestration)
- [PART 20 — Frontend Integration (React + TypeScript + Vite)](#part-20--frontend-integration-react--typescript--vite)
- [PART 21 — Architectural Trade-Offs ("Why Did We Choose This?")](#part-21--architectural-trade-offs-why-did-we-choose-this)
- [PART 22 — Comprehensive Interview Question Bank](#part-22--comprehensive-interview-question-bank)
  - [Level 1 — Beginner (20 Questions)](#level-1--beginner)
  - [Level 2 — Intermediate (30 Questions)](#level-2--intermediate)
  - [Level 3 — Advanced (30 Questions)](#level-3--advanced)
- [PART 23 — 30 "Why Did You Choose X Instead of Y?" Questions](#part-23--30-why-did-you-choose-x-instead-of-y-questions)
- [PART 24 — 20 Real-World Production Scenario Questions](#part-24--20-real-world-production-scenario-questions)
- [PART 25 — Resume & Project Deep-Dive Questions](#part-25--resume--project-deep-dive-questions)
- [PART 26 — Rapid "1-Hour Before Interview" Cheat Sheet](#part-26--rapid-1-hour-before-interview-cheat-sheet)
- [FINAL SECTION — Full Mock Interview Simulation (7 Rounds)](#final-section--full-mock-interview-simulation)

---

# PART 1 — RESOLVEHUB PROJECT OVERVIEW & ARCHITECTURE

### 1.1 What Problem ResolveHub Solves
In modern engineering and customer support organizations, issues, bug reports, and service requests arrive continuously from multiple channels. Without a centralized, secure, and transactional issue-tracking system:
1. Issues get lost or duplicated.
2. State transitions (e.g., trying to close a ticket that was never worked on) cause chaos.
3. Access permissions are violated (e.g., standard reporters closing or deleting critical production bug tickets).
4. No immutable audit trail exists to track *who* changed *what*, *when*, and *why*.
5. Searching through tens of thousands of tickets causes severe database lag or N+1 query bottlenecks.

**ResolveHub** is an enterprise-grade issue tracking and lifecycle resolution platform built to solve these exact operational and technical problems.

---

### 1.2 Core Feature Matrix
- **Role-Based Access Control (RBAC)**: Enforces three distinct roles (`REPORTER`, `AGENT`, `ADMIN`) using Spring Security 6, HTTP Basic authentication, and salted BCrypt password hashing.
- **Transactional State Machine**: Enforces a strict, deterministic ticket lifecycle (`OPEN` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED`) preventing illegal transitions.
- **Dynamic Multi-Criteria Search**: Powered by Spring Data JPA Specifications and Criteria API—combining keyword text matching, status, priority, project, assignee, and date boundaries into a single optimized query without repository method explosion.
- **Safe Server-Side Pagination & Whitelist Sorting**: Database-level `LIMIT`/`OFFSET` queries with strict sort column whitelisting to eliminate memory exhaustion and SQL injection risks.
- **Paginated Discussion Threads**: Full commenting subsystem with author metadata and N+1 query prevention using JPA `@EntityGraph`.
- **Append-Only Audit Activity History**: Automatically logs ticket creation, assignment changes, priority changes, and status transitions for complete regulatory traceability.
- **Centralized REST Error Handling**: Uniform `ApiErrorResponse` JSON contracts for validation failures, 401 unauthenticated, 403 forbidden, and 404 not found errors.
- **Dockerized Multi-Stage Environment**: Complete isolation with Java 21 non-root runtime, PostgreSQL 17 database, health checks (`pg_isready`), and persistent Docker volumes.
- **Modern React 18 SPA**: A clean TypeScript dashboard with role-aware UI controls, dynamic filtering, server pagination, and real-time status management.

---

### 1.3 Technology Stack

| Layer | Technologies / Frameworks |
|---|---|
| **Language & Runtime** | Java 21 (LTS), Eclipse Temurin OpenJDK |
| **Backend Framework** | Spring Boot 3.5.5, Spring MVC, Spring Security 6 |
| **Data Persistence** | Spring Data JPA, Hibernate 6 ORM, Jakarta Persistence 3.1 |
| **Database Engine** | PostgreSQL 17 (Production/Compose), H2 In-Memory (Test Isolation) |
| **API Documentation** | Springdoc OpenAPI 3.0, Swagger UI |
| **Containerization** | Docker (Multi-stage), Docker Compose v2 |
| **Frontend SPA** | React 18, TypeScript 5.6, Vite 6, Tailwind CSS 3.4, Lucide Icons |
| **Testing Toolchain** | JUnit 5, Mockito, Spring Security Test, MockMvc, AssertJ |

---

### 1.4 Architectural Flow & Layer Responsibilities

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           REACT 18 FRONTEND (SPA)                           │
│  [AuthContext: HTTP Basic Auth] ──> [API Client: Fetch + TypeScript DTOs]   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ HTTP / JSON
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SPRING SECURITY FILTER CHAIN                        │
│  1. CorsFilter (Allowed Origins: localhost:5173, localhost:3000)            │
│  2. BasicAuthenticationFilter (Decodes Authorization Header, BCrypt Check)  │
│  3. AuthorizationFilter (Enforces URL matchers & @PreAuthorize rules)       │
│  * Failure Handlers: AuthenticationEntryPoint (401), AccessDeniedHandler(403)│
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Authenticated Principal
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        SPRING MVC WEB LAYER (REST API)                      │
│  DispatcherServlet ──> HandlerMapping ──> TicketController                  │
│  * Validates Incoming Request DTOs (@Valid, @NotNull, @NotBlank, @Size)     │
│  * Intercepts Exceptions via GlobalExceptionHandler (@RestControllerAdvice) │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Request DTOs / Path Variables
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SERVICE LAYER (BUSINESS LOGIC)                    │
│  TicketService (@Service, @Transactional)                                   │
│  * Validates State Machine Transitions (OPEN -> IN_PROGRESS -> RESOLVED)    │
│  * Coordinates Audit Activities & Discussion Comments                       │
│  * Builds JPA Specifications (TicketSpecification via CriteriaBuilder)      │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Domain Entities / Specifications
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         REPOSITORY LAYER (SPRING DATA JPA)                  │
│  TicketRepository, UserRepository, ProjectRepository,                       │
│  TicketCommentRepository (@EntityGraph), TicketActivityRepository           │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ JPA Operations & JPQL Queries
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      HIBERNATE 6 ORM & PERSISTENCE CONTEXT                  │
│  EntityManager ──> 1st Level Cache ──> Dirty Checking ──> SQL Generator     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Native SQL Queries over JDBC
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        POSTGRESQL 17 RELATIONAL DATABASE                    │
│  Tables: users, projects, tickets, ticket_comments, ticket_activities       │
│  Indexes: idx_tickets_status, idx_tickets_priority, idx_tickets_created_at  │
│  Storage: Docker Named Volume (postgres_data)                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

# PART 2 — SPRING CORE, IOC & DEPENDENCY INJECTION

### 2.1 Inversion of Control (IoC)
- **WHAT**: In traditional programming, code directly controls the instantiation and lifecycle of its dependencies (`new TicketServiceImpl(new TicketRepositoryImpl(...))`). Inversion of Control delegates the responsibility of instantiating, configuring, assembling, and managing the lifecycle of objects (Beans) to a container.
- **WHY**: Direct instantiation tightly couples classes, makes mock testing impossible without rewriting code, and leads to messy lifecycle management. IoC decouples the *usage* of a service from its *creation*.
- **HOW**: Spring provides the `ApplicationContext` (IoC Container). On application startup, Spring scans designated packages, reads metadata (annotations or configuration classes), constructs beans, resolves their dependencies, and holds them in memory.
- **INTERNAL MECHANISM**:
  1. **Bean Definition Scanning**: Spring parses classes annotated with `@Component` (or its stereotypes `@Service`, `@Repository`, `@RestController`, `@Configuration`).
  2. **Bean Definition Registry**: Creates `BeanDefinition` metadata objects describing bean class, scope, constructor arguments, and autowiring modes.
  3. **Instantiation & Post-Processing**: Spring instantiates beans via reflection (or CGLIB bytecode generation), runs `BeanFactoryPostProcessor` and `BeanPostProcessor` hooks, resolves dependencies, and stores beans in the `DefaultSingletonBeanRegistry` map.

---

### 2.2 Dependency Injection (DI) Strategies

```text
1. FIELD INJECTION (Anti-pattern - NOT used in ResolveHub):
   @Autowired private TicketService ticketService; 
   ❌ Hidden dependencies, impossible to test without reflection, violates immutability.

2. SETTER INJECTION (Used only for optional/mutable dependencies):
   @Autowired public void setTicketService(TicketService service) { ... }
   ⚠️ Object can be left in an uninitialized/partial state.

3. CONSTRUCTOR INJECTION (The Gold Standard - Used 100% in ResolveHub):
   public TicketController(TicketService ticketService) {
       this.ticketService = ticketService;
   }
   ✅ Guarantees non-null dependencies, allows 'final' immutable fields, eliminates 
      reflection in unit tests, prevents circular dependencies at startup.
```

#### ResolveHub Concrete Example:
```java
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService; // Immutable & final

    // Spring Boot automatically injects TicketService without requiring explicit @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }
}
```

---

### 2.3 Spring Bean Stereotypes

| Annotation | Layer / Purpose | Why Not Just `@Component`? |
|---|---|---|
| `@Component` | Generic Spring-managed bean | Base annotation for all managed beans |
| `@Service` | Service / Business Layer | Semantic marker for business operations and transactional boundaries |
| `@Repository` | Data Access Layer | Translates database-specific SQLExceptions into Spring's unified `DataAccessException` hierarchy |
| `@RestController` | Web / Presentation Layer | Convenience composite: `@Controller` + `@ResponseBody`, ensuring all method return values serialize to JSON |
| `@Configuration` | Java Config Class | Declares `@Bean` factory methods evaluated via CGLIB proxies to maintain singleton semantics |

---

### 2.4 Bean Lifecycle & Singleton Scope

```text
┌────────────────────────────┐
│      1. Instantiation      │ ──> Constructor invoked via reflection
└──────────────┬─────────────┘
               ▼
┌────────────────────────────┐
│  2. Populate Properties    │ ──> Dependencies injected (DI)
└──────────────┬─────────────┘
               ▼
┌────────────────────────────┐
│   3. BeanName / Context    │ ──> Aware interfaces invoked (if implemented)
└──────────────┬─────────────┘
               ▼
┌────────────────────────────┐
│ 4. Pre-Initialization BPP  │ ──> BeanPostProcessor.postProcessBeforeInitialization()
└──────────────┬─────────────┘
               ▼
┌────────────────────────────┐
│   5. Initialization Hook   │ ──> @PostConstruct method called
└──────────────┬─────────────┘
               ▼
┌────────────────────────────┐
│ 6. Post-Initialization BPP │ ──> AOP Proxies created (Security, @Transactional)
└──────────────┬─────────────┘
               ▼
┌────────────────────────────┐
│     7. Bean Ready to Use   │ ──> Stored in ApplicationContext Singleton Cache
└──────────────┬─────────────┘
               ▼
┌────────────────────────────┐
│      8. Destruction Hook   │ ──> @PreDestroy called on shutdown
└────────────────────────────┘
```

> **Interview Tip**: In Spring, default bean scope is **Singleton** (one shared instance per Spring `ApplicationContext` container, *not* per classloader). ResolveHub services and controllers are stateless singletons, making them thread-safe as long as they do not store mutable instance state.

---

# PART 3 — SPRING MVC & THE HTTP REQUEST LIFECYCLE

### 3.1 Complete Deep-Dive: Lifecycle of `GET /api/tickets?status=OPEN&page=0`

```text
1. CLIENT REQUEST
   HTTP GET http://localhost:8081/api/tickets?status=OPEN&page=0
   Header: Authorization: Basic cmVwb3J0ZXJAY...
        │
        ▼
2. TOMCAT EMBEDDED CONNECTOR
   Accepts TCP socket connection, creates HttpServletRequest & HttpServletResponse
        │
        ▼
3. SPRING SECURITY FILTER CHAIN
   - CorsFilter: Checks Origin header against allowed list
   - BasicAuthenticationFilter: Extracts Basic auth token, decodes base64, authenticates user
   - AuthorizationFilter: Verifies ROLE_REPORTER has access to GET /api/tickets
        │
        ▼
4. DISPATCHERSERVLET (Front Controller)
   Acts as the central traffic cop for the entire Web MVC layer
        │
        ▼
5. HANDLER MAPPING (RequestMappingHandlerMapping)
   Inspects URL pattern `/api/tickets` and HTTP Method `GET`
   Resolves handler method: TicketController.searchTickets(...)
        │
        ▼
6. HANDLER ADAPTER (RequestMappingHandlerAdapter)
   Coordinates invocation of the controller method
        │
        ▼
7. HANDLER METHOD ARGUMENT RESOLVERS
   - Resolves @RequestParam("status") -> "OPEN"
   - Resolves @RequestParam(defaultValue = "0") int page -> 0
   - Applies validation if present (@Valid)
        │
        ▼
8. CONTROLLER INVOCATION
   TicketController.searchTickets("OPEN", null, null, ...) is executed
        │
        ▼
9. SERVICE & REPOSITORY EXECUTION
   TicketService.searchTickets() -> TicketSpecification -> TicketRepository.findAll(...)
   Hibernate executes SQL SELECT over JDBC -> Maps PostgreSQL rows to Ticket entities
   Service maps Ticket entities to Page<TicketResponse> DTOs
        │
        ▼
10. HANDLER METHOD RETURN VALUE HANDLER (RequestResponseBodyMethodProcessor)
    Detects @ResponseBody on @RestController
    Invokes MappingJackson2HttpMessageConverter
    Jackson serializes Page<TicketResponse> into raw JSON bytes
        │
        ▼
11. HTTP RESPONSE TRANSMISSION
    Status: 200 OK, Content-Type: application/json
    Payload flushed back through Tomcat to Client
```

---

### 3.2 Request Parameter Mapping Annotations Compared

| Annotation | Source in HTTP Request | Concrete ResolveHub Usage | Example |
|---|---|---|---|
| `@PathVariable` | URI Path Segment | Identifying specific resource IDs | `@GetMapping("/{id}")` $\rightarrow$ `/api/tickets/42` |
| `@RequestParam` | Query String parameter | Optional filtering, search keywords, pagination | `GET /api/tickets?status=OPEN&page=0` |
| `@RequestBody` | HTTP Request Payload (JSON) | Deserializing complex JSON bodies into DTOs | `@PostMapping public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest req)` |

---

# PART 4 — DTOS, BEAN VALIDATION & ERROR CONTRACTS

### 4.1 Why DTOs Exist & Why Returning Entities is Dangerous
- **WHAT**: A Data Transfer Object (DTO) is a plain Java object used exclusively to transport data between the client and the server across the network.
- **WHY**: Exposing JPA `@Entity` classes directly from controllers causes severe architectural flaws:
  1. **LazyInitializationException**: Jackson attempts to serialize uninitialized lazy relationships (`comments`, `activities`, `assignee`) outside of an active database session.
  2. **Security & Sensitive Data Leakage**: Entities contain internal fields (e.g., `User.password`). Returning entities risks serializing password hashes or internal IDs over the wire.
  3. **Circular Reference Infinite Loops**: Bidirectional relationships (`Ticket` $\leftrightarrow$ `TicketComment`) cause Jackson to loop infinitely (`StackOverflowError`) during JSON serialization.
  4. **Tight Coupling**: Any internal database refactoring (renaming columns, changing relationships) immediately breaks external client APIs.

#### ResolveHub Solution:
- **`CreateTicketRequest`**: Contains only what the client is allowed to submit (`title`, `description`, `priority`, `projectId`, `reporterId`).
- **`TicketResponse`**: Clean flat view with computed names (`projectName`, `reporterName`, `assigneeName`) and zero entity proxy attachments.

---

### 4.2 Bean Validation Flow & Exception Resolution

```text
HTTP POST /api/tickets
Payload: {"title": "", "priority": "HIGH", "projectId": 1, "reporterId": 3}
       │
       ▼
DispatcherServlet -> RequestMappingHandlerAdapter
       │
       ▼
Validator (Hibernate Validator engine) evaluates @NotBlank on CreateTicketRequest.title
Validation Fails! Title is empty.
       │
       ▼
Spring throws MethodArgumentNotValidException (containing BindingResult & FieldErrors)
       │
       ▼
GlobalExceptionHandler catches MethodArgumentNotValidException via @ExceptionHandler
       │
       ▼
Maps FieldErrors into Map<String, String>: {"title": "Title must not be blank"}
Constructs ApiErrorResponse (status: 400, error: "Validation Failed")
       │
       ▼
Returns structured HTTP 400 Bad Request JSON to client
```

#### ResolveHub Standard Error Structure (`ApiErrorResponse`):
```json
{
  "timestamp": "2026-08-30T22:35:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: 1 error(s) found",
  "path": "/api/tickets",
  "fieldErrors": {
    "title": "Title must not be blank"
  }
}
```

---

# PART 5 — JPA, HIBERNATE & ORM FUNDAMENTALS

### 5.1 Distinguishing JPA vs. Hibernate vs. Spring Data JPA

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SPRING DATA JPA                                   │
│  Repository abstraction layer (JpaRepository, CrudRepository). Generates    │
│  implementations dynamically at runtime. NOT an ORM.                        │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Implements
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         JAKARTA PERSISTENCE API (JPA)                       │
│  The Java standard specification (interfaces, annotations like @Entity,      │
│  @Id, EntityManager, EntityManagerFactory). Contains NO execution code.     │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Implemented by
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            HIBERNATE 6 ORM                                  │
│  The concrete ORM engine. Translates object graph operations into SQL,      │
│  manages First-Level Cache, Dirty Checking, Session, and JDBC connection.   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ Executes SQL via
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                                 JDBC DRIVER                                 │
│  Low-level PostgreSQL JDBC Driver communicating with the PostgreSQL DB.     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 5.2 Core Entity Annotations in ResolveHub

```java
@Entity                           // Marks class as a JPA-managed relational entity
@Table(name = "tickets",          // Maps entity to "tickets" database table
       indexes = {                // Declares database indexes for fast query lookups
           @Index(name = "idx_tickets_status", columnList = "status"),
           @Index(name = "idx_tickets_priority", columnList = "priority"),
           @Index(name = "idx_tickets_created_at", columnList = "createdAt")
       })
public class Ticket {

    @Id                           // Declares primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Uses PostgreSQL BIGSERIAL / IDENTITY column
    private Long id;

    @Column(nullable = false)     // Enforces NOT NULL constraint in DDL
    private String title;

    @Enumerated(EnumType.STRING)  // Stores enum as readable VARCHAR ("ADMIN") instead of ordinal integer (0)
    private Role role;            // Prevents bugs if enum values are reordered in Java code

    @PrePersist                   // JPA Lifecycle Callback: executes right before SQL INSERT
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate                    // JPA Lifecycle Callback: executes right before SQL UPDATE
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

---

# PART 6 — RELATIONAL ENTITY MAPPINGS & OWNERSHIP

### 6.1 ResolveHub Entity Relationship Diagram

```text
    ┌──────────────┐ 1          * ┌──────────────┐
    │     User     │─────────────<│   Project    │ (Owner)
    └──────┬───────┘              └──────┬───────┘
           │ 1                           │ 1
           │                             │
           │ * (Reporter / Assignee)     │ * (Project Tickets)
           ▼                             ▼
    ┌────────────────────────────────────────────┐
    │                   Ticket                   │
    └──────┬─────────────────────────────┬───────┘
           │ 1                           │ 1
           │                             │
           │ *                           │ *
           ▼                             ▼
    ┌──────────────┐              ┌──────────────┐
    │ TicketComment│              │TicketActivity│
    └──────────────┘              └──────────────┘
```

---

### 6.2 Relational Mapping Deep-Dive

#### 1. Owning Side vs. Inverse Side (`mappedBy`)
- **Owning Side**: The entity that physically contains the Foreign Key column in the database table. In JPA, `@JoinColumn` is *always* placed on the owning side.
- **Inverse Side**: Marked with `mappedBy = "fieldName"`. It tells Hibernate: *"I do not hold the foreign key; look at the field in the target entity to find the mapping."*

#### 2. Cascade Types & Orphan Removal
- **`CascadeType.ALL`**: Operations performed on the parent (`persist`, `merge`, `remove`) automatically propagate to child collections.
- **`orphanRemoval = true`**: If a child entity is removed from the parent's collection (`ticket.getComments().remove(c)`), Hibernate automatically issues an SQL `DELETE` for that orphaned row.

#### 3. Why All `@ManyToOne` Relationships in ResolveHub are `FetchType.LAZY`
- In JPA, `@ManyToOne` defaults to `FetchType.EAGER`.
- **The Danger**: If `Ticket` has `EAGER` mappings to `Project`, `Reporter`, and `Assignee`, loading 100 tickets causes Hibernate to immediately join or issue queries for all associated projects and users, causing severe database slowdowns.
- **ResolveHub Strategy**: Explicitly set `fetch = FetchType.LAZY` across every single relationship. We fetch associations explicitly using `@EntityGraph` or `JOIN FETCH` only when needed.

---

# PART 7 — PERSISTENCE CONTEXT, ENTITYMANAGER & DIRTY CHECKING

### 7.1 Entity Lifecycle States

```text
                        ┌──────────────┐
                        │  TRANSIENT   │ (New Java object, no DB identity, not in persistence context)
                        └──────┬───────┘
                               │ entityManager.persist(e) / repository.save(e)
                               ▼
┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│   REMOVED    │<───────│   MANAGED    │───────>│   DETACHED   │
│ (Marked for  │ remove │ (Tracked in  │ detach │ (Has DB ID,  │
│  SQL DELETE) │        │  1st cache)  │ close  │  not tracked)│
└──────────────┘        └──────┬───────┘        └──────┬───────┘
                               ▲                       │
                               └───────────────────────┘
                                    entityManager.merge(e)
```

---

### 7.2 The Magic of Dirty Checking

#### What Happens When You Run:
```java
@Transactional
public TicketResponse updateTicketStatus(Long ticketId, TicketStatus newStatus) {
    Ticket ticket = ticketRepository.findById(ticketId)
        .orElseThrow(() -> new TicketNotFoundException(ticketId));
    
    ticket.setStatus(newStatus); // Notice: NO repository.save(ticket) is called!
    return mapToResponse(ticket);
}
```

#### Step-by-Step Internal Execution:
1. **Load Entity**: `findById` executes SQL `SELECT` and puts the `Ticket` entity into Hibernate's **First-Level Cache** (Persistence Context).
2. **Snapshot Creation**: Hibernate creates an immutable internal **Snapshot Copy** of the entity's current state.
3. **Entity Mutation**: `ticket.setStatus(newStatus)` changes the Java property in memory.
4. **Transaction Commit Trigger**: At the end of the method, Spring's `@Transactional` aspect triggers `EntityManager.flush()`.
5. **Dirty Checking Comparison**: Hibernate iterates over all managed entities in the First-Level Cache and compares their current field values against the original Snapshot.
6. **SQL Generation**: Hibernate detects that `status` has changed. It automatically generates and executes:
   ```sql
   UPDATE tickets SET status = 'IN_PROGRESS', updated_at = '2026-08-30 ...' WHERE id = 1;
   ```
7. **Database Commit**: The JDBC connection commits the transaction.

> **Key Interview Takeaway**: You do *not* need to call `repository.save()` when modifying a managed entity inside a `@Transactional` service method. Calling `save()` is redundant because dirty checking handles persistence automatically.

---

# PART 8 — SPRING DATA JPA REPOSITORIES & QUERY MECHANISMS

### 8.1 Comparison: Derived Queries vs. JPQL vs. Native SQL

| Feature | Derived Query Methods | JPQL (`@Query`) | Native SQL (`nativeQuery = true`) |
|---|---|---|---|
| **Syntax Target** | Method name parsing | Entity names & Java properties | Physical SQL tables & columns |
| **Type Safety** | High (validated at startup) | High (validated at startup) | Low (errors caught at runtime) |
| **Portability** | 100% DB Independent | 100% DB Independent | Tied to PostgreSQL Dialect |
| **Optimization** | Basic filters | Complex joins, `JOIN FETCH` | Database-specific functions, CTEs |
| **Best Used For** | Simple single-condition CRUD | Multi-table joins, DTO projections | High-performance bulk/native operations |

#### ResolveHub Concrete Examples:
```java
// 1. Derived Query Method:
List<Ticket> findByStatus(String status);

// 2. Custom JPQL with JOIN FETCH (Avoids N+1):
@Query("SELECT t FROM Ticket t JOIN FETCH t.project WHERE t.id = :id")
Optional<Ticket> findByIdWithDetails(@Param("id") Long id);

// 3. Native SQL Query:
@Query(value = "SELECT * FROM tickets WHERE status = :status AND priority = :priority", nativeQuery = true)
List<Ticket> findByStatusAndPriorityNative(@Param("status") String status, @Param("priority") String priority);
```

---

# PART 9 — JPA SPECIFICATIONS & DYNAMIC SEARCH ARCHITECTURE

### 9.1 The Problem with Combinatorial Repository Methods
Imagine supporting 6 optional search filters: `status`, `priority`, `projectId`, `assigneeId`, `reporterId`, `keyword`.
Using static repository methods requires writing $2^6 = 64$ different method signatures:
- `findByStatusAndPriority(...)`
- `findByStatusAndProjectId(...)`
- `findByStatusAndPriorityAndProjectId(...)`
- ... This is completely unmaintainable.

---

### 9.2 The Solution: JPA Criteria API & `Specification<T>`
A **Specification** is a predicate generator based on the Domain-Driven Design (DDD) specification pattern:
```java
public interface Specification<T> {
    Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
```

#### How ResolveHub Builds Dynamic Search (`TicketSpecification.java`):
```java
public class TicketSpecification {

    public static Specification<Ticket> hasStatus(String status) {
        return (root, query, cb) -> 
            status == null || status.isBlank() ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> search(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) return null;
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}
```

#### Combining Predicates in `TicketService`:
```java
Specification<Ticket> spec = Specification.where(TicketSpecification.hasStatus(status))
    .and(TicketSpecification.hasPriority(priority))
    .and(TicketSpecification.hasProjectId(projectId))
    .and(TicketSpecification.hasAssigneeId(assigneeId))
    .and(TicketSpecification.search(searchKeyword))
    .and(TicketSpecification.createdAfter(createdAfter))
    .and(TicketSpecification.createdBefore(createdBefore));

// Executes a single optimized dynamic query with database pagination
Page<Ticket> pageResult = ticketRepository.findAll(spec, pageable);
```

Hibernate translates this cleanly into:
```sql
SELECT t.* FROM tickets t 
WHERE t.status = 'OPEN' 
  AND t.priority = 'HIGH' 
  AND (LOWER(t.title) LIKE '%payment%' OR LOWER(t.description) LIKE '%payment%')
ORDER BY t.created_at DESC 
LIMIT 10 OFFSET 0;
```

---

# PART 10 — PAGINATION, WHITELIST SORTING & INTERFACE PROJECTIONS

### 10.1 Safe Server-Side Pagination
Loading all records into memory (`List<Ticket> all = repository.findAll()`) and slicing in Java exhausts heap memory when database rows reach millions.

#### How ResolveHub Paginates at Database Level:
1. **`Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));`**
2. **Page Size Clamping**: Clamps requested `size` between 1 and 100 to prevent Denial of Service attacks (`size=1000000`).
3. **Sort Whitelisting**:
   ```java
   private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
       "createdAt", "updatedAt", "priority", "status", "title", "id"
   );

   if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
       throw new InvalidSortingException("Invalid sort field: " + sortField);
   }
   ```
   *Why?* Passing arbitrary client strings into `Sort.by()` can trigger property expression exceptions or expose internal database column names.

---

### 10.2 Interface-Based Projections (`TicketSummary.java`)
- **WHAT**: Spring Data JPA interface containing getter methods for a subset of entity columns.
- **WHY**: When rendering lightweight dashboard tables, loading full entity graphs (including unused descriptions, audit collections, and relationships) wastes memory and network I/O.
- **HOW IT WORKS**:
  ```java
  public interface TicketSummary {
      Long getId();
      String getTitle();
      String getStatus();
      String getPriority();
      @Value("#{target.project.name}")
      String getProjectName();
  }
  ```
  Hibernate generates a targeted SQL query selecting *only* the columns defined in the projection.

---

# PART 11 — THE N+1 QUERY PROBLEM & FETCHING OPTIMIZATIONS

### 11.1 What is the N+1 Query Problem?
Suppose you fetch 10 discussion comments for a ticket. Each comment has an `@ManyToOne` relationship to its `author` (User).

```text
Step 1: Hibernate runs 1 query to fetch 10 comments:
        SELECT * FROM ticket_comments WHERE ticket_id = 1 LIMIT 10;
        
Step 2: When mapping comment.getAuthor().getName(), Hibernate discovers that 'author' is a lazy proxy.
        It executes 10 individual queries to fetch each author:
        SELECT * FROM users WHERE id = 2;
        SELECT * FROM users WHERE id = 3;
        SELECT * FROM users WHERE id = 2;
        ... (N separate queries!)

Total Queries Executed: 1 + N = 11 queries for 10 records!
```

---

### 11.2 The Three Solutions Compared

| Solution | Mechanism | Pros | Cons |
|---|---|---|---|
| **EAGER Fetching** | Global `@ManyToOne(fetch = EAGER)` | Simple | ❌ Severe anti-pattern. Fetches related data everywhere even when not needed. |
| **`JOIN FETCH` (JPQL)** | `SELECT c FROM TicketComment c JOIN FETCH c.author` | Fast single SQL `JOIN` | Tied to explicit JPQL query string |
| **`@EntityGraph`** | `@EntityGraph(attributePaths = {"author"})` | ✅ Declarative, works seamlessly with Spring Data method names | Requires defining attribute paths |

#### ResolveHub Implementation (`TicketCommentRepository.java`):
```java
@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    @EntityGraph(attributePaths = {"author"}) // Single SQL LEFT JOIN fetches comment + author!
    Page<TicketComment> findByTicketIdOrderByCreatedAtDesc(Long ticketId, Pageable pageable);
}
```

#### Resulting SQL Executed:
```sql
SELECT c.id, c.content, c.created_at, u.id, u.name, u.email, u.role
FROM ticket_comments c
LEFT OUTER JOIN users u ON c.author_id = u.id
WHERE c.ticket_id = 1
ORDER BY c.created_at DESC
LIMIT 10 OFFSET 0;
```
**Total queries executed: Exactly 1.**

---

# PART 12 — TRANSACTION MANAGEMENT (@TRANSACTIONAL INTERNALS)

### 12.1 ACID Properties in ResolveHub
- **Atomicity**: In `assignTicketAndStart()`, assigning the user and changing status to `IN_PROGRESS` and logging the audit activity succeed together or rollback completely.
- **Consistency**: Database foreign keys and status transition rules are strictly preserved.
- **Isolation**: Concurrent transactions cannot read dirty uncommitted ticket updates.
- **Durability**: Once a ticket status transaction commits, changes are persisted in PostgreSQL WAL (Write-Ahead Logging).

---

### 12.2 How `@Transactional` Works Internally via Spring AOP

```text
Caller (TicketController)
       │
       ▼
1. Spring CGLIB Dynamic Proxy Intercepts Call
       │
       ▼
2. TransactionInterceptor invokes PlatformTransactionManager
       │
       ▼
3. Opens DB Connection, Disables Auto-Commit (connection.setAutoCommit(false))
   Binds Connection & EntityManager to ThreadLocal storage
       │
       ▼
4. Target Method Executes (TicketServiceImpl.updateTicketStatus)
       │
       ├─────────────────────────────────────────┐
       ▼ (Success)                               ▼ (RuntimeException Thrown)
5. EntityManager.flush()                   5. TransactionManager issues ROLLBACK
   connection.commit()                        connection.rollback()
   Dirty check updates saved to DB            Releases DB connection
```

---

### 12.3 Classic `@Transactional` Interview Traps

#### Trap 1: Calling `@Transactional` on a `private` method
- **Why it fails**: Spring AOP creates proxies by extending the target class or implementing its interfaces. Private methods cannot be overridden by proxies, so `@Transactional` is silently ignored.

#### Trap 2: Self-Invocation within the same class
```java
@Service
public class TicketService {
    public void publicMethod() {
        this.transactionalMethod(); // ❌ Calls 'this' directly, bypassing the Spring Proxy!
    }

    @Transactional
    public void transactionalMethod() { ... }
}
```
- **Fix**: Place transactional methods in separate services or inject `TicketService` lazily.

#### Trap 3: Checked vs. Unchecked Exceptions
- By default, Spring rolls back **only on Unchecked Exceptions** (`RuntimeException` and `Error`).
- If your method throws a checked `Exception` (e.g. `IOException`), Spring commits by default unless configured with `@Transactional(rollbackFor = Exception.class)`.

---

# PART 13 — DOMAIN BUSINESS LOGIC & STATE MACHINE WORKFLOWS

### 13.1 Ticket State Machine Rules

```text
┌──────────────┐
│     OPEN     │ (Initial creation state)
└──────┬───────┘
       │ Allowed: IN_PROGRESS
       ▼
┌──────────────┐
│ IN_PROGRESS  │ (Work actively underway by Assignee)
└──────┬───────┘
       │ Allowed: RESOLVED
       ▼
┌──────────────┐
│   RESOLVED   │ (Fix verified and completed)
└──────┬───────┘
       │ Allowed: CLOSED
       ▼
┌──────────────┐
│    CLOSED    │ (Final terminal state)
└──────────────┘
```

#### Prohibited Transitions:
- `CLOSED` $\rightarrow$ `OPEN` (Cannot reopen closed tickets)
- `RESOLVED` $\rightarrow$ `OPEN` (Must progress forward)
- `OPEN` $\rightarrow$ `CLOSED` (Cannot skip development and verification)

#### ResolveHub Validation Logic (`TicketService.java`):
```java
public void validateStatusTransition(TicketStatus current, TicketStatus next) {
    if (current == next) return;
    
    boolean isValid = switch (current) {
        case OPEN -> next == TicketStatus.IN_PROGRESS;
        case IN_PROGRESS -> next == TicketStatus.RESOLVED;
        case RESOLVED -> next == TicketStatus.CLOSED;
        case CLOSED -> false; // Terminal state
    };

    if (!isValid) {
        throw new InvalidTicketStatusTransitionException(
            "Cannot transition ticket status from " + current + " to " + next
        );
    }
}
```

---

# PART 14 — COMMENTS & AUDIT ACTIVITY HISTORY SYSTEM

### 14.1 Audit Activity History Pattern
- **WHAT**: An append-only audit trail capturing every structural change on a ticket.
- **Entity Schema (`TicketActivity.java`)**:
  - `id`: Unique audit event ID
  - `ticket`: Foreign key reference to parent `Ticket`
  - `action`: `CREATED`, `STATUS_CHANGED`, `ASSIGNED`, `PRIORITY_CHANGED`
  - `oldValue`: Previous state (e.g. `OPEN`)
  - `newValue`: Updated state (e.g. `IN_PROGRESS`)
  - `createdAt`: Immutable audit timestamp (`updatable = false`)

---

# PART 15 — CENTRALIZED GLOBAL EXCEPTION HANDLING

### 15.1 Architecture of `@RestControllerAdvice`
Instead of scattering `try-catch` blocks across every controller method, Spring MVC provides `@RestControllerAdvice` to centralize exception translation into consistent JSON contracts.

```text
Controller throws ResourceNotFoundException
                      │
                      ▼
DispatcherServlet catches exception
                      │
                      ▼
ExceptionHandlerExceptionResolver searches @RestControllerAdvice beans
                      │
                      ▼
Executes matching @ExceptionHandler(ResourceNotFoundException.class)
                      │
                      ▼
Builds ApiErrorResponse DTO with HTTP 404
                      │
                      ▼
Jackson serializes to JSON response
```

---

# PART 16 — SPRING SECURITY 6 & ROLE-BASED ACCESS CONTROL

### 16.1 Authentication vs. Authorization

| Concept | Question Answered | Failure Code | ResolveHub Component |
|---|---|---|---|
| **Authentication** | *"Who are you?"* | **`401 Unauthorized`** | `CustomUserDetailsService` + `BCryptPasswordEncoder` |
| **Authorization** | *"What are you allowed to do?"* | **`403 Forbidden`** | `SecurityConfig` rules + `@PreAuthorize("hasRole('ADMIN')")` |

---

### 16.2 ResolveHub Implemented Authorization Matrix

| Endpoint | HTTP Method | Allowed Roles | Enforced By |
|---|---|---|---|
| `/v3/api-docs/**`, `/swagger-ui/**` | `GET` | `PUBLIC` (PermitAll) | `SecurityConfig.requestMatchers` |
| `/api/tickets` (Search & List) | `GET` | `REPORTER`, `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets/{id}` | `GET` | `REPORTER`, `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets` (Create Ticket) | `POST` | `REPORTER`, `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets/{id}/comments` | `GET`, `POST` | `REPORTER`, `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets/{id}/activities` | `GET` | `REPORTER`, `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets/{id}` (Update) | `PUT` | `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets/{id}/status` | `PATCH` | `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets/{id}/assignee` | `PATCH` | `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets/{id}/assign-and-start`| `PATCH` | `AGENT`, `ADMIN` | `SecurityConfig.requestMatchers` |
| `/api/tickets/{id}` (Delete Ticket) | `DELETE` | `ADMIN` Only | `@PreAuthorize("hasRole('ADMIN')")` |

---

### 16.3 BCrypt Password Hashing Internals
- **Why Never MD5 or SHA-256?**: MD5 and plain SHA-256 are extremely fast, making them vulnerable to brute-force and rainbow table attacks.
- **Why BCrypt?**:
  1. **Salt Inclusion**: Generates a random 16-byte salt per password and embeds it directly in the hash string (`$2a$10$...`), preventing rainbow table lookups.
  2. **Configurable Work Factor (Cost parameter)**: Uses an adaptive key derivation function with $2^{\text{cost}}$ iterations. As computing power grows, the work factor can be increased to resist modern GPU cracking.

---

### 16.4 Why CSRF is Disabled in ResolveHub
- Cross-Site Request Forgery (CSRF) exploits ambient credentials (cookies sent automatically by browsers).
- ResolveHub uses **Stateless REST APIs** with HTTP Basic authentication headers. The browser does not store or automatically attach session cookies. Therefore, disabling CSRF (`csrf.disable()`) is standard and secure for stateless token/header-based architectures.

---

# PART 17 — MULTI-LAYERED AUTOMATED TESTING STRATEGY

### 17.1 Test Pyramid in ResolveHub (57 Total Tests)

```text
                    ▲
                   / \
                  /   \     1 End-to-End Workflow Integration Test (@SpringBootTest)
                 /─────\
                /       \    10 Security Integration Tests (MockMvc + Spring Security)
               /─────────\
              /           \   9 Repository Data Tests (@DataJpaTest + H2)
             /─────────────\
            /               \  14 Controller WebMvc Tests (@WebMvcTest + MockMvc)
           /─────────────────\
          /                   \ 23 Service Unit Tests (Mockito + Isolated Mocks)
         ───────────────────────
```

---

### 17.2 Testing Layer Matrix

| Layer | Annotation | Database | Scope Tested |
|---|---|---|---|
| **Unit** | `@ExtendWith(MockitoExtension.class)` | None (Mocks) | Business rules, state transitions, calculations |
| **Web** | `@WebMvcTest(TicketController.class)` | None (Mocked Service)| Request routing, JSON serialization, validation |
| **JPA** | `@DataJpaTest` | In-Memory H2 | Derived queries, `@EntityGraph`, JPA Specifications |
| **Security**| `@SpringBootTest` + `@AutoConfigureMockMvc` | H2 | HTTP Basic auth, 401/403 responses, role boundaries |
| **E2E** | `@SpringBootTest` | H2 | Multi-step full ticket lifecycles |

---

# PART 18 — DATABASE INDEXING & QUERY PERFORMANCE

### 18.1 Target Indexes in ResolveHub

```java
@Table(name = "tickets", indexes = {
    @Index(name = "idx_tickets_status", columnList = "status"),
    @Index(name = "idx_tickets_priority", columnList = "priority"),
    @Index(name = "idx_tickets_project_id", columnList = "project_id"),
    @Index(name = "idx_tickets_assignee_id", columnList = "assignee_id"),
    @Index(name = "idx_tickets_reporter_id", columnList = "reporter_id"),
    @Index(name = "idx_tickets_created_at", columnList = "createdAt")
})
```

- **Why Index Foreign Keys?**: PostgreSQL does *not* automatically create indexes on foreign key columns (`project_id`, `assignee_id`). Adding indexes speeds up table joins and relationship traversals.
- **Why Index `created_at`?**: Resolves server-side sorting (`ORDER BY created_at DESC`) directly from the B-Tree index without requiring expensive in-memory database file sorts.
- **Trade-off**: Indexes speed up `SELECT` queries but introduce write overhead on `INSERT`, `UPDATE`, and `DELETE` operations because the B-Tree must be rebalanced.

---

# PART 19 — DOCKER CONTAINERIZATION & ORCHESTRATION

### 19.1 Multi-Stage Dockerfile Architecture

```dockerfile
# Stage 1: Compilation & Packaging (Heavy builder ~800MB)
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Production Runtime (~200MB)
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=builder --chown=spring:spring /app/target/resolvehub-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 19.2 Container Networking: Why `postgres` Instead of `localhost`?
- **The Core Concept**: Each container has its own network namespace and loopback interface (`localhost`). Inside `resolvehub-app`, `localhost:5432` points to the application container itself, where PostgreSQL is *not* running.
- **The Compose Bridge Network**: Docker Compose spins up an isolated bridge network (`resolvehub_default`) with an embedded DNS server. Services resolve each other by container service name:
  ```text
  DB_URL=jdbc:postgresql://postgres:5432/resolvehub
  ```

---

# PART 20 — FRONTEND INTEGRATION (REACT + TYPESCRIPT + VITE)

### 20.1 Frontend Architecture & Role Awareness
- **Typed API Client Layer**: Centralized `ticketApi.ts`, `commentApi.ts`, and `activityApi.ts` using native `fetch` and typed interfaces.
- **Role-Aware UI vs. Backend Security**: The React frontend hides UI buttons (e.g. hiding Delete button for Reporters) purely for **User Experience (UX)**. The **Spring Security backend** remains the authoritative security boundary—if a malicious user sends a `DELETE /api/tickets/1` request directly, the backend immediately rejects it with `403 Forbidden`.

---

# PART 21 — ARCHITECTURAL TRADE-OFFS ("WHY DID WE CHOOSE THIS?")

### 1. Spring Boot 3 vs. Microservices
- **Problem**: Designing an issue-tracking system.
- **Chosen Approach**: Modular Monolith.
- **Why**: ResolveHub's domain has highly relational data dependencies (Tickets $\rightarrow$ Projects $\rightarrow$ Users $\rightarrow$ Activities). A modular monolith provides ACID transactions, zero network latency between layers, and simple single-container deployment without the operational overhead of Kubernetes or distributed tracing.

### 2. JPA Specifications vs. Combinatorial Repositories
- **Problem**: Dynamic multi-criteria filtering.
- **Chosen Approach**: Spring Data JPA Specifications.
- **Why**: Eliminates 64+ combinatorial repository methods while generating type-safe, database-optimized SQL `WHERE` clauses at runtime.

### 3. Named Volume vs. Ephemeral Container Storage
- **Problem**: Database persistence across container restarts.
- **Chosen Approach**: Docker named volume `postgres_data`.
- **Why**: Prevents catastrophic data loss during container upgrades and restarts (`docker compose down`).

---

# PART 22 — COMPREHENSIVE INTERVIEW QUESTION BANK

## Level 1 — Beginner

#### Q1: What is Spring Boot and how does it differ from Spring Framework?
> **Answer**: Spring Framework provides core IoC and DI features but requires extensive XML or Java boilerplate configuration. Spring Boot is an opinionated framework built on top of Spring that provides auto-configuration, starter POMs, embedded web servers (Tomcat), and production metrics.

#### Q2: What is Dependency Injection?
> **Answer**: DI is a design pattern where an object receives its dependencies from an external assembler (Spring IoC container) rather than creating them itself.

#### Q3: What is the purpose of `@RestController`?
> **Answer**: It is a composite annotation combining `@Controller` and `@ResponseBody`. It marks a class as a web controller and automatically serializes returned Java objects into JSON HTTP response bodies using Jackson.

#### Q4: What is a DTO and why do we use it?
> **Answer**: A Data Transfer Object decouples the internal database entity model from the external API contract, preventing lazy loading exceptions, infinite loops, and sensitive data leakage.

#### Q5: What is the role of `pom.xml` in Maven?
> **Answer**: The Project Object Model file defines project dependencies, plugins, Java versions, build lifecycles, and packaging configurations.

---

## Level 2 — Intermediate

#### Q21: How does `DispatcherServlet` process an incoming request?
> **Answer**: `DispatcherServlet` receives the request, delegates to `HandlerMapping` to find the matching controller method, uses `HandlerAdapter` and argument resolvers to bind request parameters, invokes the controller, and uses `HttpMessageConverter` (Jackson) to write the JSON response.

#### Q22: Explain Hibernate Dirty Checking.
> **Answer**: When an entity is loaded into the Persistence Context, Hibernate creates a snapshot of its state. When the transaction commits, Hibernate compares the current entity against the snapshot. Any modified properties are automatically flushed to the database as SQL `UPDATE` statements without calling `save()`.

#### Q23: How do JPA Specifications work under the hood?
> **Answer**: Specifications wrap the JPA Criteria API. They define a `toPredicate(Root, CriteriaQuery, CriteriaBuilder)` method. Spring Data compiles these predicates into a single SQL `WHERE` clause dynamically based on non-null parameters.

#### Q24: What is the difference between `@EntityGraph` and `JOIN FETCH`?
> **Answer**: Both solve the N+1 query problem by executing an SQL `JOIN`. `JOIN FETCH` is written manually in JPQL queries, while `@EntityGraph` is a declarative annotation that overrides lazy fetching on Spring Data repository methods.

#### Q25: Why is BCrypt preferred over MD5 or SHA-256 for passwords?
> **Answer**: BCrypt automatically incorporates a per-password random salt to defeat rainbow tables and uses an adaptive work factor (iteration cost) to resist hardware brute-force attacks.

---

## Level 3 — Advanced

#### Q51: What happens internally when a `@Transactional` method is called?
> **Answer**: Spring's CGLIB proxy intercepts the invocation. It asks `PlatformTransactionManager` to obtain a JDBC connection from the connection pool, sets `autoCommit = false`, and binds the connection and `EntityManager` to a `ThreadLocal`. If the method completes successfully, it triggers `flush()` and `commit()`. If an unchecked exception occurs, it executes `rollback()`.

#### Q52: Why does `@Transactional` fail during self-invocation within the same class?
> **Answer**: Spring's declarative transaction management relies on AOP proxies. When method A calls method B on `this`, the call bypasses the Spring proxy and executes directly on the raw instance, meaning the `TransactionInterceptor` is never invoked.

#### Q53: Explain the difference between HTTP 401 and HTTP 403.
> **Answer**: HTTP 401 Unauthorized means the client has **not authenticated** or provided invalid credentials (identity unknown). HTTP 403 Forbidden means the client is successfully authenticated, but their assigned roles/permissions **do not grant access** to the requested resource.

#### Q54: How does Docker Compose resolve service hostnames like `postgres`?
> **Answer**: Docker Compose creates an isolated bridge network and spins up an embedded DNS server at `127.0.0.11`. When a container queries the hostname `postgres`, the DNS server resolves it to the private IP address assigned to the PostgreSQL container within that virtual network.

---

# PART 23 — 30 "WHY DID YOU CHOOSE X INSTEAD OF Y?" QUESTIONS

1. **Why Constructor Injection instead of Field Injection (`@Autowired`)?**
   > Field injection hides dependencies and prevents immutable `final` fields. Constructor injection guarantees dependencies cannot be null, allows unit testing without reflection, and detects circular dependencies on startup.

2. **Why PostgreSQL instead of MongoDB?**
   > ResolveHub's data is inherently relational (Tickets belong to Projects, created by Users, containing Comments and Activities). PostgreSQL provides strict foreign key constraints, ACID transactional guarantees, and fast B-Tree indexing.

3. **Why JPA Specifications instead of QueryDSL?**
   > JPA Specifications are built directly into Spring Data JPA without requiring third-party annotation processors or Maven code generation plugins (like QueryDSL's `Q` classes).

4. **Why HTTP Basic Authentication instead of JWT for this demo?**
   > HTTP Basic provides a clean, stateless, and standard authentication model without the complexity of token signing, refresh tokens, and secret rotation, while keeping the security focus on RBAC and BCrypt hashing.

5. **Why `FetchType.LAZY` on `@ManyToOne` instead of the default `EAGER`?**
   > Default `EAGER` loading leads to unconstrained Cartesian joins and severe N+1 performance issues across large entity graphs. `LAZY` ensures associations are loaded only when explicitly requested.

---

# PART 24 — 20 REAL-WORLD PRODUCTION SCENARIO QUESTIONS

#### Scenario 1: "A client reports that `GET /api/tickets` takes 6 seconds under load. How would you investigate?"
> **Diagnostic Approach**:
> 1. **Check SQL Execution**: Enable `spring.jpa.show-sql=false` in production, but inspect slow query logs in PostgreSQL (`pg_stat_statements`).
> 2. **Check N+1 Queries**: Look for repetitive single-row `SELECT` statements for projects or users.
> 3. **Verify Indexing**: Run `EXPLAIN ANALYZE` on the generated query to check if PostgreSQL is executing a Sequential Scan instead of an Index Scan on `status` or `created_at`.
> 4. **Check Pagination**: Verify the client is not passing an excessively large `size` parameter.

#### Scenario 2: "A user reports receiving HTTP 403 when trying to delete a ticket. What would you check?"
> **Diagnostic Approach**:
> 1. Check the user's role in the `users` table (`role` column).
> 2. Inspect `@PreAuthorize("hasRole('ADMIN')")` on `TicketController.deleteTicket()`.
> 3. Confirm that users with `REPORTER` or `AGENT` roles are intentionally blocked from deleting tickets by design.

---

# PART 25 — RESUME & PROJECT DEEP-DIVE QUESTIONS

#### Q: "Walk me through ResolveHub."
> **Strong Answer**:
> "ResolveHub is a full-stack issue tracking and lifecycle resolution platform I built using Spring Boot 3, Spring Security 6, PostgreSQL 17, and React 18.
> On the backend, I designed a layered architecture with strict DTO separation, a transactional state machine enforcing valid ticket lifecycles (`OPEN` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `RESOLVED` $\rightarrow$ `CLOSED`), and dynamic multi-criteria search using JPA Specifications.
> For security, I implemented Role-Based Access Control with BCrypt password hashing, distinguishing permissions between Reporters, Support Agents, and Admins.
> To ensure high performance, I audited entity relationships, eliminated N+1 query bottlenecks using `@EntityGraph`, added database indexes, and built a comprehensive test suite of 57 automated tests covering unit, MockMvc, JPA, and security layers.
> The entire application is containerized using multi-stage Docker builds and orchestrated with Docker Compose."

---

# PART 26 — RAPID "1-HOUR BEFORE INTERVIEW" CHEAT SHEET

```text
┌───────────────────────────────┬────────────────────────────────────────────────────────┐
│ TOPIC                         │ CORE TAKEAWAY                                          │
├───────────────────────────────┼────────────────────────────────────────────────────────┤
│ Spring Boot Core              │ IoC manages beans; Constructor Injection for DI.       │
│ Spring MVC Flow               │ Request -> DispatcherServlet -> Mapping -> Controller. │
│ DTOs & Validation             │ Never expose entities; @Valid triggers BindingResult.  │
│ Dirty Checking                │ Managed entities auto-update on commit without save(). │
│ N+1 Prevention                │ Lazy by default; use @EntityGraph or JOIN FETCH.       │
│ Specifications                │ Dynamic CriteriaBuilder predicates combined with .and().│
│ Transactions                  │ AOP proxy wraps method; auto-rollback on RuntimeEx.    │
│ State Machine                 │ Enforced in Service layer: OPEN -> IN_PROG -> RESOLVED.│
│ Security & RBAC               │ 401 = Unauthenticated; 403 = Forbidden (Role check).   │
│ BCrypt                        │ Salted adaptive hashing; never store plaintext.        │
│ Docker Networking             │ Containers resolve by service name (postgres:5432).    │
│ Test Pyramid                  │ Unit (Service), MockMvc (Web), @DataJpaTest (Repo).    │
└───────────────────────────────┴────────────────────────────────────────────────────────┘
```

---

# FINAL SECTION — FULL MOCK INTERVIEW SIMULATION

### Round 1: Project Introduction
- **Question**: "Why did you build ResolveHub and what makes its architecture solid?"
- **Ideal Answer**: "I built ResolveHub to model a real-world, high-concurrency issue tracking system where data consistency, auditability, and role security are non-negotiable. Its architectural strength lies in strict layer separation, transactional state machines, dynamic JPA search without query explosion, and elimination of N+1 database bottlenecks."
- **Follow-up**: "What was the most challenging technical decision?"
- **Strong Follow-up Answer**: "Designing the dynamic search engine. Rather than writing dozens of combinatorial repository methods, I implemented JPA Specifications to compose Criteria API predicates dynamically while maintaining safe server-side pagination and whitelist sorting."

---

*(End of Revision Handbook)*
