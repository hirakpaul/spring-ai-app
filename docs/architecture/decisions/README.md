# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records (ADRs) for the Spring AI Customer Management application.

## What are ADRs?

Architecture Decision Records document important architectural decisions made during the development of this project. Each ADR describes:
- The context and problem being addressed
- The decision that was made
- The consequences of that decision
- Alternatives that were considered

## Why ADRs?

ADRs help us:
- **Remember why** decisions were made
- **Onboard new team members** quickly
- **Avoid revisiting** old discussions
- **Learn from** past decisions
- **Track evolution** of the architecture

## ADR Index

| ADR | Title | Status | Date |
|-----|-------|--------|------|
| [ADR-0001](0001-use-layered-architecture.md) | Use Layered Architecture Pattern | Accepted | 2025-12-28 |
| [ADR-0002](0002-use-postgresql-with-jpa-hibernate.md) | Use PostgreSQL with JPA/Hibernate | Accepted | 2025-12-28 |
| [ADR-0003](0003-use-springdoc-openapi-for-api-documentation.md) | Use SpringDoc OpenAPI for API Documentation | Accepted | 2025-12-28 |
| [ADR-0004](0004-use-bean-validation-for-request-validation.md) | Use Bean Validation for Request Validation | Accepted | 2025-12-28 |
| [ADR-0005](0005-use-centralized-exception-handling.md) | Use Centralized Exception Handling | Accepted | 2025-12-28 |
| [ADR-0006](0006-use-dto-pattern-for-layer-separation.md) | Use DTO Pattern for Layer Separation | Accepted | 2025-12-28 |
| [ADR-0007](0007-use-constructor-injection-with-lombok.md) | Use Constructor-Based Dependency Injection with Lombok | Accepted | 2025-12-28 |

## ADR Status

- **Proposed**: The ADR is under discussion
- **Accepted**: The decision has been made and implemented
- **Deprecated**: The decision has been superseded by a newer ADR
- **Rejected**: The decision was considered but rejected
- **Superseded**: Replaced by another ADR

## ADR Categories

### Architecture & Design Patterns
- [ADR-0001: Layered Architecture](0001-use-layered-architecture.md)
- [ADR-0006: DTO Pattern](0006-use-dto-pattern-for-layer-separation.md)

### Data & Persistence
- [ADR-0002: PostgreSQL with JPA/Hibernate](0002-use-postgresql-with-jpa-hibernate.md)

### API & Documentation
- [ADR-0003: SpringDoc OpenAPI](0003-use-springdoc-openapi-for-api-documentation.md)

### Validation & Error Handling
- [ADR-0004: Bean Validation](0004-use-bean-validation-for-request-validation.md)
- [ADR-0005: Centralized Exception Handling](0005-use-centralized-exception-handling.md)

### Dependency Management
- [ADR-0007: Constructor Injection with Lombok](0007-use-constructor-injection-with-lombok.md)

## Creating New ADRs

To create a new ADR:

1. Copy [ADR-TEMPLATE.md](ADR-TEMPLATE.md)
2. Rename to `XXXX-short-title.md` (use next number in sequence)
3. Fill in all sections
4. Submit for review
5. Update this index once accepted

## ADR Lifecycle

```
┌─────────────┐
│  Proposed   │
└──────┬──────┘
       │
       ▼
┌─────────────┐     ┌──────────────┐
│  Accepted   │────▶│  Deprecated  │
└──────┬──────┘     └──────────────┘
       │
       ▼
┌─────────────┐
│ Superseded  │
└─────────────┘
```

## Guidelines for Writing ADRs

### Keep them:
- **Concise**: 1-3 pages
- **Specific**: Address one decision
- **Timeless**: Explain context for future readers
- **Honest**: Include both pros and cons

### Include:
- **Context**: Why was this decision needed?
- **Decision**: What did we choose?
- **Consequences**: What are the impacts?
- **Alternatives**: What else was considered?

### Avoid:
- Implementation details (those go in code/docs)
- Temporary decisions (those go in tickets)
- Obvious choices (document only significant decisions)

## Related Documentation

- [Project README](../../../README.md)
- [API Documentation](http://localhost:8080/swagger-ui.html)
- [Integration Guide](../../../INTEGRATION.md)

## Revision History

| Date | Version | Changes | Author |
|------|---------|---------|--------|
| 2025-12-28 | 1.0 | Initial ADRs created | Rewrite Solutions |

## References

- [Architecture Decision Records by Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
- [ADR GitHub Organization](https://adr.github.io/)
- [When Should I Write an Architecture Decision Record](https://engineering.atspotify.com/2020/04/when-should-i-write-an-architecture-decision-record/)

---

**Note**: ADRs are living documents. As the architecture evolves, new ADRs may supersede old ones, but we never delete ADRs—they serve as a historical record of our architectural journey.
