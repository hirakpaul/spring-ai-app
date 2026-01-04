# ADR-0006: Use DTO Pattern for Layer Separation

## Status
Accepted

## Date
2025-12-28

## Context
The Spring AI application uses JPA entities for database persistence and needs to expose data through REST APIs. We need to decide how to handle data transfer between layers (Controller ↔ Service ↔ Repository).

Key considerations:
- Entities contain JPA annotations and persistence logic
- API clients should not depend on database structure
- Different views of data may be needed (create vs read vs update)
- Security: Avoid exposing sensitive fields or internal IDs
- Flexibility: API structure should evolve independently of database schema

## Decision
We will use the **Data Transfer Object (DTO) pattern** to separate API representations from domain entities, with dedicated mapper components for conversions.

### Specific Implementation
- **Request DTOs**: `CustomerRequest` for API input (POST/PUT)
- **Response DTOs**: `CustomerResponse` for API output (GET)
- **Mapper Components**: `CustomerMapper` for entity ↔ DTO conversions
- **Separation**: Entities never exposed directly through APIs

## Consequences

### Positive
- **Decoupling**: API structure independent of database schema
- **Flexibility**: Can change database without affecting API
- **Security**: Control what data is exposed to clients
- **Versioning**: Support multiple API versions with same entities
- **Validation**: Different validation rules for create vs update
- **Documentation**: DTOs clearly define API contract
- **Evolution**: Add/remove fields in DTOs without changing entities
- **Testing**: Easier to mock and test with simple POJOs
- **Performance**: Can optimize DTOs for specific use cases

### Negative
- **Boilerplate Code**: Requires mapping logic between entities and DTOs
- **Object Creation**: Additional objects created per request
- **Maintenance**: Need to update both entities and DTOs
- **Mapping Logic**: Manual or framework-assisted mapping required
- **Performance Overhead**: Extra object creation and field copying
- **Complexity**: More classes to maintain

### Trade-offs
- Chose explicit mapping over automatic mapping (e.g., ModelMapper) for clarity
- Accepted boilerplate for better control and debugging
- Prioritized maintainability over minimal code
- Traded performance overhead for flexibility

## Alternatives Considered

### 1. Expose Entities Directly
- **Pros**: Less code, no mapping needed
- **Cons**: Tight coupling, security risks, inflexible
- **Rejected**: Poor separation of concerns, hard to evolve independently

**Example Problem**:
```java
@Entity
public class Customer {
    private String password;  // Would be exposed if entity used directly!
}
```

### 2. Use Record Classes for DTOs
- **Pros**: Less boilerplate (Java 14+), immutability
- **Cons**: Lombok provides similar benefits, records less flexible
- **Deferred**: Consider when Java 21 features are more widely adopted

### 3. Automatic Mapping Frameworks (ModelMapper, MapStruct)
- **Pros**: Less mapping code, automatic field copying
- **Cons**: "Magic" behavior, harder to debug, reflection overhead
- **Rejected**: Prefer explicit mapping for clarity and control

### 4. GraphQL (Different API Paradigm)
- **Pros**: Clients specify exact fields needed
- **Cons**: Different paradigm, higher complexity
- **Rejected**: REST is simpler for current use case

## Implementation Details

### Request DTO (CustomerRequest)
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    private String phoneNumber;

    // Note: No ID field - clients don't provide IDs on creation
    // Note: No timestamps - managed by server
}
```

### Response DTO (CustomerResponse)
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Includes read-only fields (ID, timestamps)
    // May exclude sensitive fields if needed
}
```

### JPA Entity (Customer)
```java
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Contains JPA annotations and persistence logic
}
```

### Mapper Component (CustomerMapper)
```java
@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .build();
    }

    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
            .id(customer.getId())
            .firstName(customer.getFirstName())
            .lastName(customer.getLastName())
            .email(customer.getEmail())
            .phoneNumber(customer.getPhoneNumber())
            .createdAt(customer.getCreatedAt())
            .updatedAt(customer.getUpdatedAt())
            .build();
    }

    public void updateEntityFromRequest(CustomerRequest request, Customer customer) {
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
    }
}
```

## Data Flow

### Create Customer Flow
```
1. Client sends JSON → CustomerRequest (DTO)
2. Controller validates CustomerRequest
3. Service calls mapper.toEntity(request) → Customer (Entity)
4. Repository saves Customer entity
5. Service calls mapper.toResponse(savedCustomer) → CustomerResponse (DTO)
6. Controller returns CustomerResponse as JSON
```

### Update Customer Flow
```
1. Client sends JSON → CustomerRequest (DTO)
2. Controller validates CustomerRequest
3. Service retrieves existing Customer entity
4. Service calls mapper.updateEntityFromRequest(request, customer)
5. Repository saves updated Customer
6. Service calls mapper.toResponse(customer) → CustomerResponse (DTO)
7. Controller returns CustomerResponse as JSON
```

## DTO Design Principles

### 1. Single Responsibility
- **Request DTOs**: Represent client input
- **Response DTOs**: Represent server output
- **Don't mix**: Separate DTOs for different purposes

### 2. Validation
- **Request DTOs**: Include validation annotations
- **Response DTOs**: No validation needed (server controls output)

### 3. Immutability (Optional)
- Consider using `@Value` (Lombok) for immutable DTOs
- Use builder pattern for flexible construction

### 4. Naming Conventions
- `*Request`: For incoming data (POST, PUT)
- `*Response`: For outgoing data (GET)
- `*DTO`: Generic suffix (less preferred)

### 5. Field Selection
- **Include**: Fields relevant to API consumers
- **Exclude**: JPA metadata, internal IDs, sensitive data
- **Transform**: Convert internal representations (e.g., enums)

## Use Case Examples

### Example 1: Hiding Sensitive Data
```java
// Entity (has password)
@Entity
public class User {
    private Long id;
    private String email;
    private String passwordHash;  // Sensitive!
}

// Response DTO (no password)
public class UserResponse {
    private Long id;
    private String email;
    // Password intentionally excluded
}
```

### Example 2: Different Views
```java
// Summary DTO (for list views)
public class CustomerSummary {
    private Long id;
    private String fullName;  // Computed: firstName + lastName
    private String email;
}

// Detail DTO (for single item view)
public class CustomerDetail {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderResponse> recentOrders;  // Include related data
}
```

### Example 3: Versioned APIs
```java
// API v1
public class CustomerResponseV1 {
    private Long id;
    private String name;  // Single field
}

// API v2
public class CustomerResponseV2 {
    private Long id;
    private String firstName;  // Split into two fields
    private String lastName;
}
```

## Performance Considerations

### Object Creation Overhead
- **Impact**: Minimal for typical CRUD operations
- **Mitigation**: Not necessary unless high-throughput scenarios
- **Alternative**: Object pooling (only if profiling shows bottleneck)

### Mapping Performance
- **Manual Mapping**: Fast, predictable
- **Reflection-Based**: Slower but more flexible (ModelMapper, etc.)
- **Code Generation**: Fast runtime, slower build (MapStruct)

### Optimization Strategies
1. Reuse mapper instances (Spring singleton beans)
2. Avoid unnecessary conversions
3. Use projection queries for specific fields (future enhancement)

## Testing Strategy

### Mapper Unit Tests
```java
@Test
void testToEntity() {
    CustomerRequest request = CustomerRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("john.doe@example.com")
        .build();

    Customer entity = customerMapper.toEntity(request);

    assertThat(entity.getFirstName()).isEqualTo("John");
    assertThat(entity.getId()).isNull();  // Not set from request
}

@Test
void testToResponse() {
    Customer customer = Customer.builder()
        .id(1L)
        .firstName("John")
        .lastName("Doe")
        .createdAt(LocalDateTime.now())
        .build();

    CustomerResponse response = customerMapper.toResponse(customer);

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getFirstName()).isEqualTo("John");
}
```

### Integration Tests
```java
@Test
void testCreateCustomerEndToEnd() throws Exception {
    String requestJson = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john@example.com\"}";

    mockMvc.perform(post("/api/v1/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.createdAt").exists());
}
```

## Future Enhancements

### 1. MapStruct Integration
For larger projects with many DTOs:
```java
@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer toEntity(CustomerRequest request);
    CustomerResponse toResponse(Customer customer);
}
```

### 2. Projection DTOs
For optimized database queries:
```java
public interface CustomerSummaryProjection {
    Long getId();
    String getEmail();
}

// Repository
List<CustomerSummaryProjection> findAllProjectedBy();
```

### 3. Patch DTOs
For partial updates (PATCH requests):
```java
public class CustomerPatch {
    private Optional<String> firstName;
    private Optional<String> email;
    // Only fields that can be patched
}
```

### 4. Versioned DTOs
For API versioning:
```java
// v1/CustomerResponse.java
// v2/CustomerResponse.java
```

## Best Practices

### 1. Keep DTOs Simple
- Plain Java objects (POJOs)
- No business logic
- Only getters/setters/builders

### 2. One-Way Dependencies
- Controllers depend on DTOs
- Services work with entities
- Mappers bridge the gap
- Entities don't know about DTOs

### 3. Consistent Naming
- Use consistent suffixes: `*Request`, `*Response`
- Group related DTOs in `dto` package
- Mirror entity names where appropriate

### 4. Documentation
- Add JavaDoc to DTOs
- Include Swagger/OpenAPI annotations
- Provide example values

### 5. Validation
- Validate request DTOs at controller layer
- Don't validate response DTOs (server controls output)

## Related Decisions
- [ADR-0001: Use Layered Architecture](#)
- [ADR-0004: Use Bean Validation for Request Validation](#)
- [ADR-0003: Use SpringDoc OpenAPI for API Documentation](#)

## References
- [Martin Fowler - DTO Pattern](https://martinfowler.com/eaaCatalog/dataTransferObject.html)
- [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [MapStruct Documentation](https://mapstruct.org/)
- [Java Records (JEP 395)](https://openjdk.org/jeps/395)

## Notes
- DTOs are essential for API/database decoupling
- Manual mapping provides clarity and control
- Consider MapStruct if mapper code becomes too verbose
- DTOs also serve as API documentation
- The overhead of DTOs is worth the architectural benefits
- Entities should never be exposed directly through REST APIs
