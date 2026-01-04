# ADR-0007: Use Constructor-Based Dependency Injection with Lombok

## Status
Accepted

## Date
2025-12-28

## Context
The Spring AI application uses dependency injection for managing component dependencies (controllers, services, repositories). Spring supports three types of dependency injection:
1. Constructor injection
2. Setter injection
3. Field injection

We need to choose a consistent approach that:
- Promotes immutability and testability
- Reduces boilerplate code
- Aligns with Spring best practices
- Makes dependencies explicit
- Supports unit testing without Spring context

## Decision
We will use **Constructor-Based Dependency Injection** with **Lombok's `@RequiredArgsConstructor`** annotation to automatically generate constructor code.

### Specific Implementation
- **Pattern**: Constructor Injection
- **Boilerplate Reduction**: Lombok `@RequiredArgsConstructor`
- **Dependency Marking**: `final` fields for required dependencies
- **Spring Integration**: Automatic autowiring (Spring 4.3+)

## Consequences

### Positive
- **Immutability**: Dependencies are `final`, preventing modification
- **Explicit Dependencies**: All dependencies visible in constructor
- **Testability**: Easy to instantiate classes in unit tests
- **Null Safety**: Required dependencies cannot be null
- **Framework Independence**: Works without Spring annotations
- **Less Boilerplate**: Lombok generates constructor code
- **Maintainability**: Clear dependency graph
- **Fail-Fast**: Application won't start with missing dependencies
- **Best Practice**: Recommended by Spring team

### Negative
- **Lombok Dependency**: Requires Lombok library
- **Constructor Size**: Many dependencies = large constructor (code smell)
- **Backward Compatibility**: Can't add optional dependencies easily
- **IDE Support**: Requires Lombok plugin for IDE

### Trade-offs
- Chose constructor injection over field injection for testability
- Accepted Lombok dependency for reduced boilerplate
- Prioritized immutability over flexibility
- Traded some flexibility for better design

## Alternatives Considered

### 1. Field Injection with @Autowired
```java
@Service
public class CustomerService {
    @Autowired
    private CustomerRepository repository;  // Not recommended
}
```

**Rejected because**:
- Difficult to unit test (need reflection or Spring context)
- Dependencies hidden from API
- Can't make fields `final` (mutability risk)
- Not considered best practice by Spring team

### 2. Setter Injection
```java
@Service
public class CustomerService {
    private CustomerRepository repository;

    @Autowired
    public void setRepository(CustomerRepository repository) {
        this.repository = repository;
    }
}
```

**Rejected because**:
- Allows changing dependencies after construction
- More verbose than constructor injection
- Mainly useful for optional dependencies (rare)

### 3. Manual Constructor Without Lombok
```java
@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public CustomerService(CustomerRepository repository, CustomerMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
}
```

**Rejected because**:
- Lots of boilerplate code
- Error-prone (easy to forget field assignments)
- Lombok provides same benefits with less code

### 4. @Inject (JSR-330)
```java
@Service
public class CustomerService {
    @Inject
    public CustomerService(CustomerRepository repository) { }
}
```

**Rejected because**:
- Spring 4.3+ auto-wires single constructor
- `@Inject` adds unnecessary dependency
- No additional benefits

## Implementation Details

### Service Layer Example
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        // Use dependencies
        Customer customer = customerMapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }
}
```

**Lombok generates**:
```java
public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper) {
    this.customerRepository = customerRepository;
    this.customerMapper = customerMapper;
}
```

### Controller Layer Example
```java
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
```

### Mapper Component Example
```java
@Component
public class CustomerMapper {

    // No dependencies - no constructor needed

    public Customer toEntity(CustomerRequest request) {
        // Mapping logic
    }
}
```

## Lombok Configuration

### Maven Dependency
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### IDE Setup
- **IntelliJ IDEA**: Install Lombok plugin + enable annotation processing
- **Eclipse**: Install Lombok jar
- **VS Code**: Install Lombok extension

### Build Configuration
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <excludes>
                    <exclude>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                    </exclude>
                </excludes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Dependency Injection Patterns

### Single Dependency
```java
@Service
@RequiredArgsConstructor
public class SimpleService {
    private final SomeRepository repository;
}
```

### Multiple Dependencies
```java
@Service
@RequiredArgsConstructor
public class ComplexService {
    private final FirstRepository firstRepository;
    private final SecondRepository secondRepository;
    private final SomeMapper mapper;
    private final ExternalApiClient client;
}
```

### Optional Dependencies (Not Recommended)
If truly needed, use setter injection:
```java
@Service
@RequiredArgsConstructor
public class ServiceWithOptional {
    private final RequiredDependency required;
    private OptionalDependency optional;  // Not final

    @Autowired(required = false)
    public void setOptional(OptionalDependency optional) {
        this.optional = optional;
    }
}
```

## Testing Benefits

### Unit Testing Without Spring
```java
class CustomerServiceImplTest {

    private CustomerRepository customerRepository;
    private CustomerMapper customerMapper;
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        customerMapper = mock(CustomerMapper.class);

        // Easy to instantiate with mocks
        customerService = new CustomerServiceImpl(customerRepository, customerMapper);
    }

    @Test
    void testCreateCustomer() {
        // Test implementation
    }
}
```

### Integration Testing With Spring
```java
@SpringBootTest
class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerService customerService;  // Spring autowires

    @Test
    void testCreateCustomer() {
        // Test with real dependencies
    }
}
```

## Best Practices

### 1. Use Final Fields
```java
private final CustomerRepository repository;  // Good: immutable
private CustomerRepository repository;        // Bad: mutable
```

### 2. Keep Constructor Small
```java
// Good: 2-4 dependencies
@RequiredArgsConstructor
public class Service {
    private final Repository1 repo1;
    private final Repository2 repo2;
    private final Mapper mapper;
}

// Bad: Too many dependencies (code smell - consider refactoring)
@RequiredArgsConstructor
public class GodService {
    private final Dep1 dep1;
    private final Dep2 dep2;
    // ... 10 more dependencies
    // Consider splitting into smaller services
}
```

### 3. Avoid Circular Dependencies
```java
// Bad: Circular dependency
@Service
@RequiredArgsConstructor
public class ServiceA {
    private final ServiceB serviceB;  // ServiceA → ServiceB
}

@Service
@RequiredArgsConstructor
public class ServiceB {
    private final ServiceA serviceA;  // ServiceB → ServiceA (CIRCULAR!)
}
```

**Solution**: Refactor to eliminate circular dependency or use `@Lazy`

### 4. Don't Mix Injection Types
```java
// Bad: Mixing injection types
@Service
@RequiredArgsConstructor
public class InconsistentService {
    private final Repository repository;  // Constructor injection

    @Autowired
    private Mapper mapper;  // Field injection (inconsistent!)
}
```

## Lombok Alternatives

### Spring 4.3+ Implicit Constructor
```java
// No @RequiredArgsConstructor needed if single constructor
@Service
public class CustomerService {
    private final CustomerRepository repository;

    // Spring auto-wires this constructor
    public CustomerService(CustomerRepository repository) {
        this.repository = repository;
    }
}
```

### Java 14+ Records (Future)
```java
// Potential future approach with records
@Service
public record CustomerService(CustomerRepository repository) {
    // Constructor auto-generated, fields are final
}
```

## Design Guidelines

### When Dependencies Are Growing
If a class has more than 5-7 dependencies:
1. **Review Single Responsibility Principle**: Is the class doing too much?
2. **Consider Facade Pattern**: Group related dependencies
3. **Split Into Smaller Services**: Create more focused components
4. **Use Events**: Decouple with Spring Events or messaging

### Dependency Organization
Order dependencies logically:
```java
@RequiredArgsConstructor
public class CustomerService {
    // Repositories first
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    // Then mappers/converters
    private final CustomerMapper customerMapper;

    // Then external services
    private final EmailService emailService;
}
```

## Security Considerations
- Dependencies are immutable, reducing risk of malicious modification
- Clear dependency graph makes security audits easier
- Constructor injection prevents null pointer exceptions

## Performance Considerations
- **Startup Time**: Constructor injection happens at application startup
- **Runtime**: No performance difference vs field injection
- **Memory**: Negligible overhead from final fields
- **Testing**: Faster unit tests (no Spring context needed)

## Troubleshooting

### Circular Dependency Error
```
Error creating bean with name 'serviceA':
Requested bean is currently in creation: Is there an unresolvable circular reference?
```

**Solutions**:
1. Refactor to remove circular dependency (preferred)
2. Use `@Lazy` annotation (workaround)
3. Redesign component relationships

### Lombok Not Working
- Install IDE plugin
- Enable annotation processing
- Rebuild project
- Check Lombok version compatibility with Java version

## Related Decisions
- [ADR-0001: Use Layered Architecture](#)
- [ADR-0002: Use PostgreSQL with JPA/Hibernate](#)
- [ADR-0006: Use DTO Pattern for Layer Separation](#)

## References
- [Spring Dependency Injection Best Practices](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#beans-constructor-injection)
- [Lombok @RequiredArgsConstructor](https://projectlombok.org/features/constructor)
- [Why Constructor Injection](https://blog.marcnuri.com/field-injection-is-not-recommended)
- [Spring Team on Constructor Injection](https://spring.io/blog/2007/07/11/setter-injection-versus-constructor-injection-and-the-use-of-required)

## Notes
- Constructor injection is the preferred approach by Spring team since 2007
- Spring 4.3+ auto-wires single constructor, no `@Autowired` needed
- Lombok reduces boilerplate but requires IDE plugin
- If constructor grows beyond 5-7 dependencies, review class design
- Final fields provide compile-time safety and immutability
- Constructor injection makes testing easier without Spring framework
