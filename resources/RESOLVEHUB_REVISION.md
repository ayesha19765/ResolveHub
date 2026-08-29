# ResolveHub — Spring Boot Revision Notes

A compact revision guide for the concepts covered while building ResolveHub.

## 1. Spring Boot Request Flow

```text
Client
  ↓
DispatcherServlet
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
Hibernate / JPA
  ↓
PostgreSQL
```

The `DispatcherServlet` acts as the front controller of Spring MVC and routes requests to the appropriate controller.

## 2. REST APIs

| Method | Typical use |
|---|---|
| GET | Read |
| POST | Create |
| PUT | Replace/update |
| PATCH | Partial update |
| DELETE | Delete |

## 3. Validation

```java
@PostMapping
public TicketResponse createTicket(
        @Valid @RequestBody CreateTicketRequest request
) {
    ...
}
```

Flow:

```text
JSON → Request DTO → @Valid → Validation → Controller
```

## 4. JPA / Hibernate

ORM maps Java objects to relational database structures.

```text
Java Object ↔ Database Row
Java Field  ↔ Database Column
Relationship ↔ Foreign Key
```

```java
@Entity
public class Ticket {
    ...
}
```

`JpaRepository<Ticket, Long>` provides common operations such as `findAll()`, `findById()`, `save()`, and `delete()`.

## 5. Persistence Context

The Persistence Context contains entities currently managed by JPA/Hibernate.

Typical lifecycle:

```text
Transient → Managed → Detached
                    ↘ Removed
```

### Dirty Checking

For a managed entity:

```java
ticket.setStatus("RESOLVED");
```

Hibernate can detect the change and generate an `UPDATE` during flush/commit.

## 6. EntityManager

Example:

```java
entityManager.find(Ticket.class, id);
```

`EntityManager` provides lower-level JPA operations. Spring Data repositories are preferred for most normal repository work.

## 7. Relationships

ResolveHub uses relationships such as:

```text
Project 1 ───── * Ticket
User    1 ───── * Ticket (reporter)
User    1 ───── * Ticket (assignee)
```

Example:

```java
@ManyToOne(fetch = FetchType.LAZY)
private Project project;
```

## 8. Fetch Types

### LAZY

Relationship data is loaded when accessed.

### EAGER

Relationship data is requested immediately.

Do not make everything EAGER just to avoid N+1. Prefer intentional fetching.

## 9. Cascading

Cascade controls whether persistence operations propagate between related entities.

Examples:

```java
cascade = CascadeType.PERSIST
cascade = CascadeType.ALL
```

Use cascading according to the actual lifecycle relationship between entities.

## 10. Derived Queries

Spring Data derives queries from method names.

```java
findByStatus(String status)
findByStatusAndPriority(String status, String priority)
findByTitleContainingIgnoreCase(String keyword)
findByStatusIn(List<String> statuses)
```

Think:

```text
findByStatus
→ WHERE status = ?
```

## 11. JPQL

JPQL works with entities and entity fields.

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

Flow:

```text
JPQL → Hibernate → SQL → PostgreSQL
```

## 12. Native SQL

Native queries use actual database SQL.

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

### JPQL vs SQL

```text
JPQL → entity / field oriented
SQL  → table / column oriented
```

## 13. Projection

Use projections when a read operation needs only selected fields.

```java
public interface TicketSummary {
    Long getId();
    String getTitle();
    String getStatus();
    String getPriority();
    String getProjectName();
}
```

Main idea:

```text
Entity query → full entity
Projection   → selected fields
```

## 14. Pagination

Avoid returning huge result sets.

```java
Pageable pageable = PageRequest.of(
        page,
        size,
        Sort.by("createdAt").descending()
);
```

Example:

```http
GET /api/tickets?page=0&size=10
```

A `Page` provides content plus metadata such as total elements, total pages, page number, and page size.

## 15. Sorting

```java
Sort.by("createdAt").descending()
```

Use the entity property name in Spring Data queries.

## 16. N+1 Query Problem

Classic pattern:

```text
1 query → N Tickets
N queries → related records
```

Total:

```text
N + 1 queries
```

This becomes expensive as N grows.

## 17. JOIN FETCH

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

Use fetch joins when a particular operation needs related data immediately.

## 18. Specifications

Specifications are useful for optional/dynamic filters.

```java
Specification<Ticket> specification =
        Specification
                .where(TicketSpecification.hasStatus(status))
                .and(TicketSpecification.hasPriority(priority))
                .and(TicketSpecification.hasProjectId(projectId));
```

Think:

```text
Optional filters
      ↓
Dynamic WHERE conditions
```

## 19. DTOs

Prefer a separation between persistence entities and API models.

```text
Entity
  ↓
Mapping
  ↓
Response DTO
  ↓
JSON
```

Benefits:

- Controls the API contract
- Prevents accidental field exposure
- Avoids exposing persistence implementation details
- Reduces serialization problems with relationships
- Lets API and database models evolve independently

## 20. Transactions

A transaction groups database operations into one unit of work.

```text
BEGIN
  ↓
Operation 1
  ↓
Operation 2
  ↓
Operation 3
  ↓
COMMIT
```

If something fails:

```text
ROLLBACK
```

The next phase will use transactions for real multi-step ResolveHub workflows.

# Interview Quick Revision

### JPA vs Hibernate

```text
JPA      = specification
Hibernate = implementation
```

### Spring Data JPA

An abstraction that simplifies repository-based JPA access.

```text
Spring Data JPA
      ↓
JPA
      ↓
Hibernate
      ↓
Database
```

### JPQL vs SQL

```text
JPQL → entities and entity fields
SQL  → tables and columns
```

### N+1

One query loads the main records and N additional queries load related records.

### N+1 fixes

Common approaches:

```text
JOIN FETCH
EntityGraph
Projection
Intentional fetch planning
```

### Why pagination?

To avoid loading and returning unnecessarily large result sets.

### Why projections?

To retrieve only the fields needed by a read operation.

### Why DTOs?

To separate API contracts from persistence entities.

### Why Specifications?

To compose dynamic optional filters.

# Current Learning Map

```text
Spring Boot
    ↓
Spring MVC
    ↓
DispatcherServlet
    ↓
REST APIs
    ↓
Validation
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
Relationships
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
N+1
    ↓
JOIN FETCH
    ↓
NEXT: Transactions + Real Business Workflows
```
