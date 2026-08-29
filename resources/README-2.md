# ResolveHub

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Build](https://img.shields.io/badge/Build-Maven-blue)](https://maven.apache.org/)
[![Database](https://img.shields.io/badge/Database-PostgreSQL-blue)](https://www.postgresql.org/)
[![ORM](https://img.shields.io/badge/ORM-Hibernate-brown)](https://hibernate.org/)

A backend issue-tracking and resolution platform built with **Spring Boot, Spring MVC, Spring Data JPA, Hibernate, and PostgreSQL**.

ResolveHub is designed to model the backend of a real-world ticket management system where users can create, manage, assign, filter, and track issues across projects.

The project goes beyond basic CRUD by exploring **REST API design, request validation, relational data modeling, JPA querying, pagination, projections, dynamic filtering, transaction management, and ORM performance optimization**.

---

## Highlights

- RESTful APIs for ticket management
- Layered architecture using Controller, Service, Repository, and DTOs
- Request validation using Jakarta Bean Validation
- Relational entity mapping using JPA and Hibernate
- Spring Data JPA derived query methods
- Custom JPQL and native SQL queries
- Dynamic filtering using JPA Specifications
- Pagination and sorting for large result sets
- Interface-based projections for optimized read operations
- Lazy loading and relationship management
- N+1 query problem analysis and optimization using `JOIN FETCH`
- Persistence Context and Hibernate dirty checking
- EntityManager-based persistence operations
- PostgreSQL-backed persistent storage

---

## System Architecture

```mermaid
flowchart TD
    Client["REST Client"] --> Controller["Controller Layer"]
    Controller --> DTO["DTOs<br/>Request / Response"]
    DTO --> Service["Service Layer"]
    Service --> Repository["Repository Layer"]
    Repository --> JPA["Spring Data JPA"]
    JPA --> Hibernate["Hibernate ORM"]
    Hibernate --> PostgreSQL["PostgreSQL"]
````

The application follows a layered architecture where each layer has a clear responsibility.

The **Controller** handles HTTP requests, the **Service** contains business logic, the **Repository** handles persistence, and **Hibernate/JPA** manages the interaction between Java entities and PostgreSQL.

DTOs are used to keep the API contract separate from the persistence model.

---

## Domain Model

ResolveHub currently revolves around three core entities:

```mermaid
flowchart TD
    Project["Project"] -->|1 : *| Ticket["Ticket"]
    Ticket -->|* Reporter| Reporter["User<br/>Reporter"]
    Ticket -->|* Assignee| Assignee["User<br/>Assignee"]
```

A ticket contains information such as:

```text
Ticket
├── id
├── title
├── description
├── status
├── priority
├── project
├── reporter
├── assignee
├── createdAt
└── updatedAt
```

---

## Key Features

### RESTful Ticket Management

ResolveHub exposes REST endpoints for managing tickets using standard HTTP methods.

```text
GET       → Retrieve tickets
POST      → Create tickets
PUT       → Update tickets
PATCH     → Partially update ticket state
DELETE    → Delete tickets
```

Example:

```http
GET /api/tickets/1
POST /api/tickets
PUT /api/tickets/1
PATCH /api/tickets/1/status
DELETE /api/tickets/1
```

The request flows through Spring MVC's `DispatcherServlet`, which routes the request to the appropriate controller method.

---

### Request Validation

Incoming API requests are represented using dedicated request DTOs.

Validation is handled using Jakarta Bean Validation.

```java
public class CreateTicketRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;
}
```

The controller validates incoming requests using:

```java
@Valid
```

This prevents invalid data from reaching the service and persistence layers.

---

### DTO-Based API Design

ResolveHub separates API models from JPA entities.

```text
                  Request
                     │
                     ▼
              CreateTicketRequest
                     │
                     ▼
                  Service
                     │
                     ▼
                  Ticket
                     │
                     ▼
               TicketResponse
                     │
                     ▼
                  Response
```

This prevents the API from directly exposing the persistence model and gives the application control over exactly what data is returned to clients.

---

## JPA & Hibernate

ResolveHub uses **Spring Data JPA with Hibernate** for persistence.

The application explores the complete persistence flow:

```text
Java Entity
     │
     ▼
JPA
     │
     ▼
Hibernate
     │
     ▼
SQL
     │
     ▼
PostgreSQL
```

The project also explores the JPA Persistence Context and Hibernate's dirty checking mechanism.

For example:

```java
ticket.setStatus("RESOLVED");
```

When the entity is managed inside a transaction, Hibernate can detect the modification and generate the corresponding `UPDATE` during flush/commit.

---

## Database Querying

ResolveHub intentionally demonstrates multiple approaches to querying relational data.

### Derived Query Methods

For simple queries, Spring Data JPA can derive SQL from repository method names.

```java
findByStatus(String status)
```

or:

```java
findByStatusAndPriority(
    String status,
    String priority
)
```

This keeps simple repository queries concise without manually writing SQL.

---

### JPQL

For more complex entity-oriented queries, ResolveHub uses JPQL.

```java
@Query("""
    SELECT t
    FROM Ticket t
    WHERE t.status = :status
""")
List<Ticket> findTicketsByStatus(
    @Param("status") String status
);
```

JPQL operates on **entities and their fields** rather than directly on database tables.

---

### Native SQL

Native queries are also used where writing database-specific SQL is useful.

```java
@Query(
    value = """
        SELECT *
        FROM tickets
        WHERE status = :status
    """,
    nativeQuery = true
)
List<Ticket> findNativeByStatus(
    @Param("status") String status
);
```

This demonstrates the distinction between:

```text
JPQL
→ Entity-oriented

Native SQL
→ Table/column-oriented
```

---

## Dynamic Filtering

Real ticket systems rarely need only one fixed filter.

ResolveHub supports combining optional filters using **JPA Specifications**.

Example:

```http
GET /api/tickets?status=OPEN&priority=HIGH&projectId=1
```

The query can dynamically combine:

```text
Status
   +
Priority
   +
Project
   +
Search
```

instead of creating a separate repository method for every possible combination.

Conceptually:

```text
Optional Filters
       │
       ▼
JPA Specification
       │
       ▼
Dynamic Query
       │
       ▼
PostgreSQL
```

This avoids repository methods such as:

```text
findByStatusAndPriorityAndProjectId(...)
findByStatusAndProjectId(...)
findByPriorityAndProjectId(...)
findByStatusAndPriority(...)
...
```

---

## Pagination & Sorting

Returning every ticket from the database is not scalable as the dataset grows.

ResolveHub uses Spring Data pagination:

```http
GET /api/tickets?page=0&size=10
```

Sorting can also be applied:

```java
Sort.by("createdAt").descending()
```

The API therefore supports controlled result sizes while still providing metadata such as:

```text
Total elements
Total pages
Current page
Page size
```

---

## Projections

For read-heavy endpoints, returning an entire entity may be unnecessary.

ResolveHub uses interface-based projections to retrieve only the fields required by a particular query.

Example:

```java
public interface TicketSummary {

    Long getId();

    String getTitle();

    String getStatus();

    String getPriority();

    String getProjectName();
}
```

Instead of:

```text
Database
   ↓
Complete Ticket Entity
```

a projection can retrieve:

```text
Database
   ↓
Only required fields
```

This reduces unnecessary data retrieval for lightweight read operations.

---

## ORM Performance

One of the goals of ResolveHub is to understand not only how JPA works, but also how seemingly simple ORM operations can result in inefficient database access.

### N+1 Query Problem

A naive relationship access pattern can result in:

```text
1 query
   ↓
Fetch N Tickets
   ↓
N additional queries
   ↓
Fetch related Projects / Users
```

Total:

```text
N + 1 database queries
```

This becomes increasingly expensive as the number of records grows.

---

### JOIN FETCH

For operations where related entities are required immediately, ResolveHub demonstrates `JOIN FETCH`.

```java
@Query("""
    SELECT t
    FROM Ticket t
    JOIN FETCH t.project
    JOIN FETCH t.reporter
    WHERE t.id = :id
""")
Optional<Ticket> findTicketWithProjectAndReporter(
    @Param("id") Long id
);
```

Conceptually:

```text
Without intentional fetching

Ticket Query
     │
     ├── Project Query
     ├── Reporter Query
     └── More Queries...


With JOIN FETCH

Ticket
   │
   ├── Project
   └── Reporter

Retrieved together
```

The goal is not to make every relationship eager, but to **choose the appropriate fetching strategy for each use case**.

---

## API Examples

### Retrieve all tickets

```http
GET /api/tickets
```

### Filter by status

```http
GET /api/tickets?status=OPEN
```

### Filter by priority

```http
GET /api/tickets?priority=HIGH
```

### Filter by project

```http
GET /api/tickets?projectId=1
```

### Search by title

```http
GET /api/tickets?search=payment
```

### Pagination

```http
GET /api/tickets?page=0&size=10
```

### Combine filters

```http
GET /api/tickets?status=OPEN&priority=HIGH&projectId=1&search=payment&page=0&size=10
```

---

## Project Structure

```text
src/
└── main/
    └── java/
        └── com/
            └── ayesha/
                └── resolvehub/
                    │
                    ├── controller/
                    │
                    ├── dto/
                    │
                    ├── entity/
                    │
                    ├── exception/
                    │
                    ├── repository/
                    │   └── projection/
                    │
                    └── service/
```

The codebase follows a layered structure to keep HTTP handling, business logic, persistence, and API models separated.

---

## Built With

| Technology         | Purpose                         |
| ------------------ | ------------------------------- |
| Java               | Core application development    |
| Spring Boot        | Application framework           |
| Spring MVC         | REST API and request handling   |
| Spring Data JPA    | Repository abstraction          |
| Hibernate          | ORM implementation              |
| PostgreSQL         | Relational database             |
| Maven              | Build and dependency management |
| Lombok             | Boilerplate reduction           |
| Jakarta Validation | Request validation              |

---

## Getting Started

### Prerequisites

* Java 25
* Maven
* PostgreSQL

### Clone the repository

```bash
git clone https://github.com/<your-username>/ResolveHub.git
cd ResolveHub
```

### Configure PostgreSQL

Create a PostgreSQL database for the application and configure the datasource in:

```text
src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/resolvehub
spring.datasource.username=postgres
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

---

## Current Learning Scope

ResolveHub is being developed incrementally to explore practical Spring Boot backend concepts.

```text
Spring Boot
      ↓
Spring MVC
      ↓
DispatcherServlet
      ↓
REST APIs
      ↓
Request Validation
      ↓
DTOs
      ↓
Service Layer
      ↓
Spring Data JPA
      ↓
Hibernate
      ↓
Persistence Context
      ↓
EntityManager
      ↓
Entity Relationships
      ↓
Cascading
      ↓
Derived Queries
      ↓
JPQL
      ↓
Native SQL
      ↓
Projections
      ↓
Pagination
      ↓
Specifications
      ↓
Lazy Loading
      ↓
N+1 Optimization
      ↓
JOIN FETCH
```

---

## Roadmap

The next stages will extend ResolveHub from a JPA-focused backend into a more complete issue-tracking system.

* [ ] Transactional ticket workflows
* [ ] Ticket assignment workflow
* [ ] Ticket activity/history
* [ ] Comments and discussions
* [ ] Business-level exception handling
* [ ] Consistent API response structure
* [ ] Global exception handling
* [ ] Automated unit and integration tests
* [ ] API documentation with OpenAPI / Swagger
* [ ] Logging and observability
* [ ] Dockerized PostgreSQL + Spring Boot setup
* [ ] Production-oriented configuration
* [ ] Final performance and database optimization

---

## What This Project Demonstrates

ResolveHub is primarily a hands-on exploration of backend engineering with Spring Boot.

The project focuses on understanding **why** different Spring and JPA features are used rather than simply using them.

Key concepts include:

* REST API architecture
* Dependency injection and Spring components
* Spring MVC request lifecycle
* DTO-based API design
* Request validation
* ORM and entity mapping
* Persistence Context
* Hibernate dirty checking
* Entity relationships
* Query optimization
* JPQL vs native SQL
* Dynamic query construction
* Pagination
* Projections
* Lazy loading
* N+1 query optimization
* Transaction management

---

## Future Direction

The long-term goal is to evolve ResolveHub into a production-style backend that demonstrates not only Spring Boot and JPA fundamentals, but also **transactional workflows, testing, observability, API documentation, containerization, and scalable backend design**.

---

Made with Java, Spring Boot, and a lot of debugging.

© 2026 Ayesha — ResolveHub

```

### One thing I'd change from your ThreadVault style

For **ResolveHub**, I actually like this slightly more detailed README because the project is specifically meant to be your **Spring Boot showcase**. A recruiter can skim the top half and immediately see:

> **REST + JPA + Hibernate + PostgreSQL + JPQL + Specifications + Pagination + Projections + N+1 optimization**

That's a **much stronger SDE story** than simply saying "built a ticket CRUD application."

And as we continue, we'll keep the README's **Roadmap** honest: once we implement transactions, activity history, tests, Swagger, Docker, etc., we'll move each item from `[ ]` to `[x]` and add the corresponding engineering explanation.
