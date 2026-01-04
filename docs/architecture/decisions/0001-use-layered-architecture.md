# ADR-0001: Use Layered Architecture Pattern

## Status
Accepted

## Date
2025-12-28

## Context
We need to define a clear architectural pattern for organizing the Spring AI customer management application that promotes separation of concerns, maintainability, and testability. The application needs to handle REST API requests, business logic, and data persistence in a scalable manner.

## Decision
We will adopt a **Layered Architecture** pattern with four distinct layers:

1. **Controller Layer** (`com.rewritesolutions.ai.spring_ai_app.controller`)
   - Handles HTTP requests and responses
   - Performs request validation
   - Maps between HTTP and service layer
   - Returns appropriate HTTP status codes

2. **Service Layer** (`com.rewritesolutions.ai.spring_ai_app.service`)
   - Contains business logic
   - Orchestrates operations across multiple repositories
   - Handles transactions
   - Enforces business rules

3. **Repository Layer** (`com.rewritesolutions.ai.spring_ai_app.repository`)
   - Provides data access abstraction
   - Uses Spring Data JPA for database operations
   - Contains custom query methods

4. **Domain Layer** (`com.rewritesolutions.ai.spring_ai_app.entity`)
   - Represents the core business domain
   - Contains JPA entities
   - Includes domain-specific logic (e.g., lifecycle callbacks)

Additionally, we use:
- **DTO Layer** (`com.rewritesolutions.ai.spring_ai_app.dto`) for data transfer between layers
- **Mapper Layer** (`com.rewritesolutions.ai.spring_ai_app.mapper`) for entity-DTO conversions
- **Exception Layer** (`com.rewritesolutions.ai.spring_ai_app.exception`) for centralized error handling

## Consequences

### Positive
- **Clear Separation of Concerns**: Each layer has a well-defined responsibility
- **Testability**: Layers can be tested independently with mocking
- **Maintainability**: Changes in one layer have minimal impact on others
- **Scalability**: Easy to add new features by extending existing layers
- **Team Collaboration**: Different teams can work on different layers
- **Reusability**: Business logic in services can be reused by multiple controllers
- **Framework Alignment**: Aligns naturally with Spring Boot's component model

### Negative
- **Boilerplate Code**: Requires mapping between layers (entities ↔ DTOs)
- **Performance Overhead**: Additional object creation and mapping
- **Complexity for Simple Operations**: CRUD operations require code in all layers
- **Learning Curve**: New developers need to understand layer responsibilities

### Trade-offs
- Chose clarity and maintainability over reducing boilerplate
- Accepted performance overhead for better separation of concerns
- Prioritized long-term maintainability over short-term development speed

## Alternatives Considered

### 1. Monolithic Single-Layer Approach
- **Pros**: Less code, faster initial development
- **Cons**: Poor separation of concerns, difficult to test, hard to maintain
- **Rejected**: Not suitable for enterprise applications

### 2. Hexagonal Architecture (Ports & Adapters)
- **Pros**: Strong decoupling, technology-independent core
- **Cons**: Higher complexity, steeper learning curve
- **Rejected**: Overkill for current application size

### 3. Domain-Driven Design (DDD) with Aggregates
- **Pros**: Rich domain model, better business logic encapsulation
- **Cons**: Increased complexity, requires deep domain knowledge
- **Rejected**: Current domain is simple; DDD would add unnecessary complexity

## Implementation Details

### Package Structure
```
com.rewritesolutions.ai.spring_ai_app/
├── controller/       # REST API endpoints
├── service/          # Business logic interfaces
│   └── impl/         # Business logic implementations
├── repository/       # Data access layer
├── entity/           # JPA entities
├── dto/              # Data Transfer Objects
├── mapper/           # Entity-DTO mappers
└── exception/        # Custom exceptions and handlers
```

### Layer Dependencies
```
Controller → Service → Repository → Entity
     ↓          ↓
    DTO     Mapper
```

### Layer Responsibilities

**Controllers**:
- Annotated with `@RestController`
- Handle HTTP-specific concerns
- Delegate to services
- Return ResponseEntity with appropriate status codes

**Services**:
- Annotated with `@Service`
- Transactional boundaries (`@Transactional`)
- Contain business rules and validations
- Return DTOs

**Repositories**:
- Extend `JpaRepository`
- Contain custom query methods
- Return entities

**Entities**:
- Annotated with `@Entity`
- Map to database tables
- Contain JPA annotations

## Related Decisions
- [ADR-0006: Use DTO Pattern for Layer Separation](#)
- [ADR-0007: Use Constructor-Based Dependency Injection](#)

## References
- [Spring Boot Layered Architecture Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Martin Fowler - Presentation Domain Data Layering](https://martinfowler.com/bliki/PresentationDomainDataLayering.html)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

## Notes
- This decision can be revisited if the application grows significantly in complexity
- If microservices are adopted, each service should maintain this layered structure
- Consider moving to hexagonal architecture if business logic becomes more complex
