# ADR-0002: Use PostgreSQL with JPA/Hibernate

## Status
Accepted

## Date
2025-12-28

## Context
The Spring AI application requires a reliable, production-grade relational database for storing customer data with ACID guarantees. We need to choose both a database system and an ORM (Object-Relational Mapping) framework that:
- Supports complex queries and relationships
- Provides data integrity and consistency
- Scales with application growth
- Integrates well with Spring Boot
- Offers good performance for read/write operations

## Decision
We will use **PostgreSQL 13.1** as the relational database and **JPA with Hibernate** as the ORM framework.

### Database Choice: PostgreSQL
- **Version**: PostgreSQL 13.1 (Alpine image for Docker)
- **Deployment**: Dockerized for development and production
- **Connection**: JDBC driver with connection pooling

### ORM Choice: JPA with Hibernate
- **Specification**: Java Persistence API (JPA) 3.1
- **Implementation**: Hibernate ORM (via Spring Data JPA)
- **Integration**: Spring Data JPA repositories

## Consequences

### Positive
**PostgreSQL Benefits**:
- **ACID Compliance**: Full transaction support with strong consistency
- **Advanced Features**: JSON support, full-text search, custom types
- **Performance**: Excellent query optimization and indexing
- **Reliability**: Proven track record in production environments
- **Community**: Large community and extensive documentation
- **Open Source**: No licensing costs, vendor-independent
- **Docker Support**: Easy containerization and deployment

**JPA/Hibernate Benefits**:
- **Database Independence**: Can switch databases with minimal code changes
- **Productivity**: Reduces boilerplate JDBC code
- **Spring Integration**: Seamless integration with Spring Boot
- **Repository Pattern**: Spring Data JPA provides automatic CRUD operations
- **Query Methods**: Derived queries from method names
- **Type Safety**: Compile-time type checking
- **Caching**: Second-level caching support
- **Lazy Loading**: Efficient data fetching strategies

### Negative
- **Learning Curve**: Developers must understand JPA/Hibernate concepts
- **N+1 Query Problem**: Risk of performance issues if not careful with relationships
- **Complexity**: Hibernate can be complex for advanced scenarios
- **Debugging**: Generated SQL can be hard to debug
- **Performance Overhead**: ORM layer adds slight performance cost
- **PostgreSQL Complexity**: More complex than lightweight databases (SQLite, H2)

### Trade-offs
- Chose PostgreSQL over MySQL for better standards compliance and advanced features
- Chose JPA/Hibernate over raw JDBC for productivity and maintainability
- Accepted ORM overhead for development speed and code quality
- Accepted PostgreSQL complexity for production-grade features

## Alternatives Considered

### 1. MySQL with JPA/Hibernate
- **Pros**: Popular, good ecosystem, similar to PostgreSQL
- **Cons**: Weaker standards compliance, fewer advanced features
- **Rejected**: PostgreSQL offers better JSON support and more robust ACID compliance

### 2. MongoDB (NoSQL)
- **Pros**: Flexible schema, horizontal scalability
- **Cons**: No ACID transactions across documents, eventual consistency
- **Rejected**: Customer data requires strict consistency and relationships

### 3. H2 Database (In-Memory)
- **Pros**: Lightweight, fast for testing
- **Cons**: Not suitable for production, limited features
- **Rejected**: Used only for testing; not production-ready

### 4. Raw JDBC without ORM
- **Pros**: Full control, potentially better performance
- **Cons**: Verbose code, manual mapping, more boilerplate
- **Rejected**: Development speed and maintainability are priorities

### 5. MyBatis (SQL Mapper)
- **Pros**: More control over SQL, less "magic"
- **Cons**: More boilerplate than JPA, manual mapping
- **Rejected**: Spring Data JPA provides better productivity

## Implementation Details

### Database Configuration

**pom.xml**:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**application.properties**:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/compose-postgres
spring.datasource.username=compose-postgres
spring.datasource.password=compose-postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### Docker Compose Setup
```yaml
db:
  image: 'postgres:13.1-alpine'
  ports:
    - "5432:5432"
  environment:
    - POSTGRES_USER=compose-postgres
    - POSTGRES_PASSWORD=compose-postgres
    - POSTGRES_DB=compose-postgres
```

### JPA Entity Example
```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(unique = true, nullable = false)
    private String email;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### Repository Example
```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.lastName) = LOWER(:lastName)")
    List<Customer> findByLastNameIgnoreCase(@Param("lastName") String lastName);
}
```

### Schema Management Strategy
- **Development**: `spring.jpa.hibernate.ddl-auto=update` (auto-update schema)
- **Production**: Consider using Flyway or Liquibase for version-controlled migrations
- **Testing**: H2 in-memory database with `ddl-auto=create-drop`

## Performance Considerations

### Optimization Strategies
1. **Connection Pooling**: Use HikariCP (Spring Boot default)
2. **Query Optimization**: Use `@Query` for complex queries
3. **Lazy Loading**: Default fetch type for collections
4. **Indexing**: Add database indexes on frequently queried columns
5. **Logging**: Enable SQL logging in development (`spring.jpa.show-sql=true`)

### Monitoring
- Enable SQL logging: `logging.level.org.hibernate.SQL=DEBUG`
- Monitor query performance with Spring Boot Actuator
- Use PostgreSQL's `EXPLAIN ANALYZE` for query optimization

## Testing Strategy
- **Unit Tests**: H2 in-memory database for fast tests
- **Integration Tests**: Testcontainers with PostgreSQL for realistic testing
- **Repository Tests**: `@DataJpaTest` annotation

## Migration Path
If PostgreSQL becomes a bottleneck:
1. **Vertical Scaling**: Increase database resources
2. **Read Replicas**: Add read replicas for read-heavy workloads
3. **Caching**: Implement Redis for frequently accessed data
4. **Sharding**: Partition data across multiple databases (last resort)

## Security Considerations
- Use environment variables for database credentials
- Never commit credentials to version control
- Use connection encryption (SSL/TLS) in production
- Implement principle of least privilege for database users
- Regular security updates and patches

## Related Decisions
- [ADR-0001: Use Layered Architecture](#)
- [ADR-0007: Use Constructor-Based Dependency Injection](#)

## References
- [PostgreSQL Official Documentation](https://www.postgresql.org/docs/)
- [Hibernate ORM Documentation](https://hibernate.org/orm/documentation/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Boot Docker Compose Support](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.docker-compose)

## Notes
- PostgreSQL 13.1 chosen for stability; consider upgrading to newer versions (15+) for performance
- Schema auto-update (`ddl-auto=update`) is convenient but risky for production
- Consider implementing database migration tools (Flyway/Liquibase) before production deployment
- Monitor query performance regularly and add indexes as needed
