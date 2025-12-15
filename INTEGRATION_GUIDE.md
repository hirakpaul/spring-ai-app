# Spring AI App - Integration Guide

Complete guide for integrating with and extending the Spring AI App Customer Management API.

## Overview

This guide provides detailed information for developers who want to:
- Integrate with the Customer API
- Extend the application with new features
- Understand the application architecture
- Deploy the application to production

## Table of Contents
- [Overview](#overview)
- [API Integration](#api-integration)
- [Application Architecture](#application-architecture)
- [Development Guide](#development-guide)
- [Testing](#testing)
- [Production Deployment](#production-deployment)
- [Troubleshooting](#troubleshooting)

## API Integration

### Base URL

```
http://localhost:8080/api/v1
```

### Content Type

All requests and responses use `application/json`.

### Available Endpoints

#### Customer Operations

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/customers` | Get all customers | - |
| GET | `/customers/{id}` | Get customer by ID | - |
| GET | `/customers/email/{email}` | Get customer by email | - |
| GET | `/customers/search?term={term}` | Search customers | - |
| GET | `/customers/count` | Get total count | - |
| POST | `/customers` | Create customer | CustomerRequest |
| PUT | `/customers/{id}` | Update customer | CustomerRequest |
| DELETE | `/customers/{id}` | Delete customer | - |

### Request/Response Models

#### CustomerRequest
```json
{
  "firstName": "string (required)",
  "lastName": "string (required)",
  "email": "string (required, valid email)",
  "phoneNumber": "string (optional)"
}
```

#### CustomerResponse
```json
{
  "id": "long",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phoneNumber": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Validation Rules

- **firstName**: Required, not blank
- **lastName**: Required, not blank
- **email**: Required, valid email format, unique across customers
- **phoneNumber**: Optional

### Error Handling

The API returns standard HTTP status codes:

- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **204 No Content** - Successful deletion
- **400 Bad Request** - Validation error
- **404 Not Found** - Resource not found
- **409 Conflict** - Duplicate email
- **500 Internal Server Error** - Server error

#### Error Response Format
```json
{
  "timestamp": "2025-12-14T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": {
    "email": "must be a well-formed email address"
  }
}
```

## Application Architecture

### Layered Architecture

The application follows a clean layered architecture:

```
┌─────────────────────────────────┐
│     Presentation Layer          │
│  (Controllers, DTOs, Mappers)   │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│      Service Layer              │
│   (Business Logic)              │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│    Persistence Layer            │
│  (Repositories, Entities)       │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│       Database                  │
│      (PostgreSQL)               │
└─────────────────────────────────┘
```

### Package Structure

```
com.rewritesolutions.ai.spring_ai_app
├── controller/          # REST controllers
│   └── CustomerController.java
├── dto/                 # Data Transfer Objects
│   ├── CustomerRequest.java
│   ├── CustomerResponse.java
│   └── CustomerMapper.java
├── entity/              # JPA entities
│   └── Customer.java
├── exception/           # Exception handling
│   ├── GlobalExceptionHandler.java
│   ├── CustomerNotFoundException.java
│   └── DuplicateEmailException.java
├── repository/          # Spring Data repositories
│   └── CustomerRepository.java
├── service/             # Business logic
│   ├── CustomerService.java
│   └── CustomerServiceImpl.java
└── SpringAiAppApplication.java
```

### Key Components

#### Controllers
- Handle HTTP requests and responses
- Validate input using Bean Validation
- Map between DTOs and entities
- Return appropriate HTTP status codes

#### Services
- Contain business logic
- Manage transactions
- Throw business exceptions
- Coordinate between repositories

#### Repositories
- Extend Spring Data JPA repositories
- Provide database operations
- Support custom queries

#### Exception Handler
- Global exception handling using `@ControllerAdvice`
- Converts exceptions to appropriate HTTP responses
- Provides consistent error format

## Development Guide

### Adding New Endpoints

1. **Create Entity** (if needed)
```java
@Entity
@Table(name = "your_entity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YourEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Add fields
}
```

2. **Create Repository**
```java
@Repository
public interface YourRepository extends JpaRepository<YourEntity, Long> {
    // Add custom queries if needed
}
```

3. **Create Service**
```java
@Service
public class YourService {
    private final YourRepository repository;

    // Implement business logic
}
```

4. **Create Controller**
```java
@RestController
@RequestMapping("/api/v1/your-resource")
public class YourController {
    private final YourService service;

    // Implement endpoints
}
```

### Database Migrations

For production, consider using:
- **Flyway** - Version-controlled database migrations
- **Liquibase** - Database schema change management

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### Adding Validation

Use Bean Validation annotations:
```java
public class YourRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Min(value = 0, message = "Must be positive")
    private Integer age;
}
```

### Custom Queries

Add custom queries to repositories:
```java
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE LOWER(c.email) = LOWER(:email)")
    Optional<Customer> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Customer> searchByName(@Param("term") String term);
}
```

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CustomerRepositoryTest

# Run tests with coverage
mvn clean test jacoco:report
```

### Test Structure

```
src/test/java/
└── com/rewritesolutions/ai/spring_ai_app/
    ├── repository/
    │   └── CustomerRepositoryTest.java
    ├── service/
    │   └── CustomerServiceTest.java
    └── controller/
        └── CustomerControllerTest.java
```

### Writing Integration Tests

```java
@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository repository;

    @Test
    void shouldSaveCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john@example.com");

        Customer saved = repository.save(customer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
    }
}
```

## Production Deployment

### Checklist

Before deploying to production:

- [ ] Update `application.properties` for production
- [ ] Configure database connection pooling
- [ ] Enable HTTPS/TLS
- [ ] Set up monitoring and logging
- [ ] Configure actuator endpoints
- [ ] Set up database backups
- [ ] Review security settings
- [ ] Configure CORS if needed
- [ ] Set up CI/CD pipeline
- [ ] Prepare rollback strategy

### Production Configuration

**application-prod.properties:**
```properties
# Server
server.port=8080

# Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Actuator
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized

# Logging
logging.level.root=INFO
logging.level.com.rewritesolutions=INFO
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

### Docker Deployment

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/spring-ai-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and run:**
```bash
# Build application
mvn clean package

# Build Docker image
docker build -t spring-ai-app:latest .

# Run with Docker Compose
docker compose up -d
```

### Environment Variables

Set these environment variables in production:

```bash
DB_URL=jdbc:postgresql://localhost:5432/production_db
DB_USERNAME=prod_user
DB_PASSWORD=secure_password
SPRING_PROFILES_ACTIVE=prod
```

### Monitoring

#### Health Checks
```bash
curl http://localhost:8080/actuator/health
```

#### Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

### Logging

Configure logging for production:

```properties
# File logging
logging.file.name=/var/log/spring-ai-app/application.log
logging.file.max-size=10MB
logging.file.max-history=30

# Log patterns
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

## Troubleshooting

### Common Issues

#### Database Connection Failed

**Symptoms:**
- Application fails to start
- Error: "Unable to connect to database"

**Solutions:**
1. Verify PostgreSQL is running: `docker ps`
2. Check database credentials in `application.properties`
3. Ensure database exists: `docker exec -it postgres psql -U compose-postgres -l`
4. Check port 5432 is not in use: `netstat -an | grep 5432`

#### Port Already in Use

**Symptoms:**
- Error: "Port 8080 is already in use"

**Solutions:**
1. Check what's using the port: `lsof -i :8080` (Mac/Linux) or `netstat -ano | findstr :8080` (Windows)
2. Kill the process or change the port in `application.properties`
3. Use a different port: `server.port=8081`

#### Validation Errors

**Symptoms:**
- 400 Bad Request responses
- Validation error messages

**Solutions:**
1. Check request body matches required format
2. Ensure all required fields are present
3. Verify email format is valid
4. Check for duplicate emails

#### Application Crashes

**Symptoms:**
- Application stops unexpectedly
- Out of memory errors

**Solutions:**
1. Check application logs: `docker logs <container-name>`
2. Increase JVM memory: `java -Xmx512m -jar app.jar`
3. Check for memory leaks
4. Review database connection pool settings

### Getting Help

1. Check application logs
2. Review this integration guide
3. Check the main [README.md](README.md)
4. Search for similar issues in the project repository
5. Contact the development team

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Spring AI App - Customer Management API**
