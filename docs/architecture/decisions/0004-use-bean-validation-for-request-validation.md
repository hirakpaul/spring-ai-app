# ADR-0004: Use Bean Validation (JSR-303/380) for Request Validation

## Status
Accepted

## Date
2025-12-28

## Context
The Spring AI application receives customer data through REST API endpoints. We need to validate incoming requests to ensure:
- Data integrity (e.g., email format, required fields)
- Business rule compliance (e.g., phone number format)
- Prevent invalid data from reaching the database
- Provide clear error messages to API clients

We need a validation approach that:
- Validates data at the API boundary
- Provides declarative validation rules
- Integrates well with Spring Boot
- Returns consistent error responses

## Decision
We will use **Bean Validation (JSR-303/380)** with Hibernate Validator for declarative request validation at the controller layer.

### Specific Implementation
- **Specification**: Bean Validation 3.0 (JSR-380)
- **Implementation**: Hibernate Validator (bundled with Spring Boot)
- **Integration**: Spring Boot Validation Starter
- **Trigger**: `@Valid` annotation on controller method parameters
- **Error Handling**: Custom exception handler for validation errors

## Consequences

### Positive
- **Declarative Validation**: Rules defined as annotations on DTOs
- **Reusability**: Validation rules defined once, applied everywhere
- **Standard Compliance**: Follows Java EE/Jakarta EE standards
- **Type Safety**: Compile-time validation rule checking
- **Rich Annotation Set**: Built-in validators for common patterns
- **Custom Validators**: Easy to create custom validation logic
- **Integration**: Seamless Spring Boot integration
- **Error Messages**: Customizable validation error messages
- **Documentation**: Validation constraints visible in code
- **Swagger Integration**: Validation rules appear in OpenAPI docs

### Negative
- **Runtime Validation**: Validation happens at runtime, not compile-time
- **Annotation Overhead**: DTO classes have many annotations
- **Limited Complexity**: Complex business rules may need custom validators
- **Error Message Management**: Need to manage i18n for error messages
- **Performance**: Slight overhead for validation processing

### Trade-offs
- Chose declarative validation over imperative validation for clarity
- Accepted annotation overhead for explicit validation rules
- Prioritized developer experience over minimal dependencies
- Traded some flexibility for standardization

## Alternatives Considered

### 1. Manual Validation in Service Layer
- **Pros**: Full control, no annotations
- **Cons**: Repetitive code, error-prone, hard to maintain
- **Rejected**: Too much boilerplate, violates DRY principle

### 2. Spring Validation (Only @Validated)
- **Pros**: Spring-specific features
- **Cons**: Less standard, same as Bean Validation for our use case
- **Rejected**: Bean Validation is more standard and portable

### 3. Custom Validation Framework
- **Pros**: Tailored to specific needs
- **Cons**: Reinventing the wheel, maintenance burden
- **Rejected**: Standard solutions exist and work well

### 4. Client-Side Validation Only
- **Pros**: Better UX, immediate feedback
- **Cons**: Security risk, cannot trust client
- **Rejected**: Server-side validation is mandatory for security

## Implementation Details

### Maven Dependency
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### DTO Validation Annotations
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "Customer first name", example = "John")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Customer last name", example = "Doe")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Customer email address", example = "john.doe@email.com")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number should be valid")
    @Schema(description = "Customer phone number in E.164 format", example = "+12025550123")
    private String phoneNumber;
}
```

### Controller Usage
```java
@PostMapping
public ResponseEntity<CustomerResponse> createCustomer(
    @Valid @RequestBody CustomerRequest request) {
    // Validation happens before this code executes
    CustomerResponse response = customerService.createCustomer(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
}
```

### Exception Handler
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidationExceptions(
    MethodArgumentNotValidException ex) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
    });

    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now());
    response.put("status", HttpStatus.BAD_REQUEST.value());
    response.put("error", "Validation Failed");
    response.put("errors", errors);

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
}
```

## Validation Constraints Used

### Built-in Constraints
| Annotation | Purpose | Example |
|------------|---------|---------|
| `@NotNull` | Field must not be null | `@NotNull Long id` |
| `@NotBlank` | String must not be null, empty, or whitespace | `@NotBlank String firstName` |
| `@Email` | Must be valid email format | `@Email String email` |
| `@Pattern` | Must match regex pattern | `@Pattern(regexp = "...") String phone` |
| `@Size` | String/collection size constraints | `@Size(min = 2, max = 50) String name` |
| `@Min` / `@Max` | Numeric min/max values | `@Min(0) Integer age` |
| `@Past` / `@Future` | Date must be in past/future | `@Past LocalDate birthDate` |

### Custom Constraints (Future Enhancement)
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## Error Response Format

### Validation Error Response
```json
{
    "timestamp": "2025-12-28T10:30:45",
    "status": 400,
    "error": "Validation Failed",
    "errors": {
        "firstName": "First name is required",
        "email": "Email should be valid"
    }
}
```

### Single Field Error
```json
{
    "timestamp": "2025-12-28T10:30:45",
    "status": 400,
    "error": "Validation Failed",
    "errors": {
        "email": "Email should be valid"
    }
}
```

## Validation Layers

### 1. Controller Layer (Primary)
- **Trigger**: `@Valid` annotation
- **Purpose**: Validate incoming requests before processing
- **Implementation**: Bean Validation annotations on DTOs

### 2. Service Layer (Business Rules)
- **Trigger**: Manual validation or custom annotations
- **Purpose**: Complex business logic validation
- **Implementation**: `IllegalArgumentException` for business rule violations

Example:
```java
if (customerRepository.existsByEmail(request.getEmail())) {
    throw new IllegalArgumentException("Customer with email " + request.getEmail() + " already exists");
}
```

### 3. Database Layer (Constraints)
- **Trigger**: JPA annotations
- **Purpose**: Data integrity at database level
- **Implementation**: `@Column(unique = true, nullable = false)`

## Best Practices

### 1. Validation Placement
- **Always** validate at controller layer with `@Valid`
- **Sometimes** validate business rules in service layer
- **Never** rely only on database constraints

### 2. Error Messages
- Provide clear, actionable error messages
- Use `message` parameter in annotations
- Consider i18n for multi-language support

### 3. Group Validation (Advanced)
```java
public interface CreateValidation {}
public interface UpdateValidation {}

@NotNull(groups = UpdateValidation.class)
private Long id;

// Controller
@PostMapping
public ResponseEntity<?> create(@Validated(CreateValidation.class) @RequestBody CustomerRequest request) {
    // Implementation
}
```

### 4. Cross-Field Validation
For validations spanning multiple fields, create custom class-level validators:
```java
@ValidCustomer  // Custom annotation
public class CustomerRequest {
    // Fields
}
```

## Performance Considerations
- Validation overhead is minimal (microseconds per request)
- More complex regex patterns may have higher cost
- Custom validators should be optimized
- Consider caching validation results if applicable

## Testing Strategy

### Unit Tests
```java
@Test
void testInvalidEmail() {
    CustomerRequest request = CustomerRequest.builder()
        .firstName("John")
        .lastName("Doe")
        .email("invalid-email")  // Invalid format
        .build();

    Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(request);
    assertThat(violations).isNotEmpty();
    assertThat(violations.iterator().next().getMessage()).contains("Email should be valid");
}
```

### Integration Tests
```java
@Test
void testCreateCustomerWithInvalidData() throws Exception {
    mockMvc.perform(post("/api/v1/customers")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"firstName\":\"\",\"email\":\"invalid\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.firstName").exists())
        .andExpect(jsonPath("$.errors.email").exists());
}
```

## Internationalization (i18n)
For multi-language support:
```properties
# messages.properties
customer.email.invalid=Email address is invalid
customer.firstname.required=First name is required

# Usage
@Email(message = "{customer.email.invalid}")
private String email;
```

## Future Enhancements
1. **Group Validation**: Implement validation groups for create vs update
2. **Custom Validators**: Add complex custom validators (e.g., @UniqueEmail)
3. **Internationalization**: Add multi-language error messages
4. **Validation Profiles**: Different validation rules per environment
5. **Asynchronous Validation**: For expensive validations (e.g., external API calls)

## Related Decisions
- [ADR-0005: Use Centralized Exception Handling](#)
- [ADR-0006: Use DTO Pattern for Layer Separation](#)
- [ADR-0003: Use SpringDoc OpenAPI for API Documentation](#)

## References
- [Bean Validation 3.0 Specification (JSR-380)](https://beanvalidation.org/3.0/)
- [Hibernate Validator Documentation](https://hibernate.org/validator/documentation/)
- [Spring Boot Validation Guide](https://spring.io/guides/gs/validating-form-input/)
- [Jakarta Bean Validation](https://jakarta.ee/specifications/bean-validation/3.0/)

## Notes
- Bean Validation is the industry standard for Java validation
- Hibernate Validator is the reference implementation and comes bundled with Spring Boot
- Validation annotations also serve as documentation for API consumers
- Always validate on server side, even if client-side validation exists
- Consider creating a validation library if multiple microservices share validation logic
