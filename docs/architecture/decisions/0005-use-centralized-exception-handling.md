# ADR-0005: Use Centralized Exception Handling with @RestControllerAdvice

## Status
Accepted

## Date
2025-12-28

## Context
The Spring AI application needs a consistent approach to handling exceptions across all REST API endpoints. Without proper exception handling:
- Clients receive default Spring Boot error responses (not user-friendly)
- Different endpoints might return errors in different formats
- Stack traces might be exposed to clients (security risk)
- Error responses lack necessary context for debugging

We need a solution that:
- Provides consistent error responses across all endpoints
- Separates error handling logic from business logic
- Returns appropriate HTTP status codes
- Includes helpful error messages without exposing sensitive information
- Is easy to maintain and extend

## Decision
We will use **Centralized Exception Handling** with `@RestControllerAdvice` to handle all exceptions in a single global exception handler.

### Specific Implementation
- **Pattern**: Global Exception Handler
- **Spring Annotation**: `@RestControllerAdvice`
- **Exception Handlers**: `@ExceptionHandler` methods for specific exceptions
- **Custom Exceptions**: `CustomerNotFoundException` for domain-specific errors
- **Error Response**: Standardized `ErrorResponse` DTO

## Consequences

### Positive
- **Consistency**: All errors returned in the same format
- **Centralization**: Single place to manage all exception handling logic
- **Separation of Concerns**: Controllers focus on business logic, not error handling
- **Maintainability**: Easy to add new exception handlers
- **Security**: Control what information is exposed to clients
- **Logging**: Centralized logging of all exceptions
- **HTTP Status Codes**: Proper status codes for different error types
- **DRY Principle**: No repeated error handling code in controllers
- **Testing**: Easier to test exception scenarios

### Negative
- **Global Scope**: Handler applies to all controllers (might want more granular control)
- **Complexity**: Need to understand exception hierarchy
- **Hidden Behavior**: Error handling not visible in controller methods
- **Performance**: Slight overhead from exception handling mechanism

### Trade-offs
- Chose global handler over controller-specific handlers for consistency
- Accepted loss of fine-grained control for maintainability
- Prioritized developer experience over performance
- Traded explicit error handling for declarative approach

## Alternatives Considered

### 1. Controller-Level Exception Handling
- **Pros**: Fine-grained control per controller
- **Cons**: Code duplication, inconsistent error responses
- **Rejected**: Violates DRY principle, hard to maintain

### 2. Service-Level Exception Handling
- **Pros**: Business logic layer handles errors
- **Cons**: Mixes business logic with error handling, not REST-aware
- **Rejected**: Exception handling should be at API boundary

### 3. Filter-Based Exception Handling
- **Pros**: Can intercept exceptions before reaching controller
- **Cons**: More complex, less Spring-native
- **Rejected**: `@RestControllerAdvice` is the Spring-recommended approach

### 4. Problem Details (RFC 7807)
- **Pros**: Industry standard for error responses
- **Cons**: More complex, requires additional setup
- **Deferred**: Consider for future enhancement

## Implementation Details

### Global Exception Handler
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(
        CustomerNotFoundException ex) {

        log.error("Customer not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex) {

        log.error("Illegal argument: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

### Custom Exception
```java
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
```

### Error Response DTO
```java
@Data
@Builder
private static class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
}
```

## Exception Hierarchy

### Application Exceptions
```
Exception
├── RuntimeException
│   ├── CustomerNotFoundException (404)
│   ├── IllegalArgumentException (400)
│   └── [Future custom exceptions]
└── MethodArgumentNotValidException (400)
```

### HTTP Status Code Mapping
| Exception | HTTP Status | Use Case |
|-----------|-------------|----------|
| `CustomerNotFoundException` | 404 NOT FOUND | Resource not found |
| `IllegalArgumentException` | 400 BAD REQUEST | Invalid business logic |
| `MethodArgumentNotValidException` | 400 BAD REQUEST | Validation failure |
| `Exception` (catch-all) | 500 INTERNAL SERVER ERROR | Unexpected errors |

## Error Response Formats

### Standard Error Response
```json
{
    "timestamp": "2025-12-28T10:30:45",
    "status": 404,
    "error": "Not Found",
    "message": "Customer not found with ID: 123"
}
```

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

## Exception Handling Strategy

### 1. Domain-Specific Exceptions
Create custom exceptions for business logic errors:
```java
public class CustomerNotFoundException extends RuntimeException { }
public class DuplicateEmailException extends RuntimeException { }  // Future
```

### 2. Validation Exceptions
Handled automatically by Bean Validation:
- `MethodArgumentNotValidException` - Request body validation
- `ConstraintViolationException` - Path variable validation

### 3. System Exceptions
Catch-all handler for unexpected errors:
- Logs full stack trace for debugging
- Returns generic message to client (security)

## Logging Strategy

### Error Levels
- **ERROR**: Log all exceptions with context
- **WARN**: Log business rule violations
- **INFO**: Log handled exceptions (optional)

### Logging Format
```java
log.error("Customer not found: {}", ex.getMessage());  // Business error
log.error("Unexpected error occurred: {}", ex.getMessage(), ex);  // System error with stack trace
```

### Logging Best Practices
- Include correlation IDs for request tracing (future enhancement)
- Log full stack traces only for unexpected exceptions
- Sanitize sensitive data before logging
- Use structured logging for production

## Security Considerations

### 1. Information Disclosure
- **Never** expose stack traces to clients
- **Never** expose database errors or SQL
- **Always** log full details server-side
- **Always** return generic messages for system errors

### 2. Error Messages
```java
// Good: Generic message for clients
"An unexpected error occurred"

// Bad: Exposes implementation details
"PostgreSQL connection failed: password authentication failed"
```

### 3. Production Configuration
```properties
# Disable stack traces in error responses
server.error.include-stacktrace=never
server.error.include-message=always
server.error.include-binding-errors=always
```

## Testing Strategy

### Unit Tests
```java
@Test
void testCustomerNotFoundException() {
    CustomerNotFoundException ex = new CustomerNotFoundException("Customer not found with ID: 1");

    ResponseEntity<ErrorResponse> response = exceptionHandler.handleCustomerNotFoundException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().getMessage()).contains("Customer not found");
}
```

### Integration Tests
```java
@Test
void testNotFoundEndpoint() throws Exception {
    mockMvc.perform(get("/api/v1/customers/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.timestamp").exists());
}
```

## Future Enhancements

### 1. Problem Details (RFC 7807)
Implement RFC 7807 standard:
```json
{
    "type": "https://api.example.com/errors/customer-not-found",
    "title": "Customer Not Found",
    "status": 404,
    "detail": "Customer with ID 123 was not found",
    "instance": "/api/v1/customers/123"
}
```

### 2. Correlation IDs
Add request correlation for distributed tracing:
```json
{
    "timestamp": "2025-12-28T10:30:45",
    "correlationId": "550e8400-e29b-41d4-a716-446655440000",
    "status": 404,
    "error": "Not Found",
    "message": "Customer not found"
}
```

### 3. Internationalization (i18n)
Support multi-language error messages:
```java
@ExceptionHandler(CustomerNotFoundException.class)
public ResponseEntity<ErrorResponse> handle(CustomerNotFoundException ex, Locale locale) {
    String message = messageSource.getMessage("error.customer.notfound", null, locale);
    // Return error response
}
```

### 4. Custom Exception Hierarchy
```java
public abstract class BusinessException extends RuntimeException { }
public class CustomerNotFoundException extends BusinessException { }
public class DuplicateEmailException extends BusinessException { }
public class InsufficientPermissionException extends BusinessException { }
```

### 5. Error Codes
Add machine-readable error codes:
```json
{
    "errorCode": "CUSTOMER_NOT_FOUND",
    "message": "Customer not found with ID: 123"
}
```

## Best Practices

### 1. Exception Naming
- Use descriptive names: `CustomerNotFoundException` not `NotFoundException`
- Follow Java naming conventions: `*Exception` suffix
- Group related exceptions in the same package

### 2. Exception Messages
- Provide context: "Customer not found with ID: 123"
- Avoid technical jargon for client-facing messages
- Include identifiers for debugging

### 3. HTTP Status Codes
- **400 Bad Request**: Client error (validation, illegal arguments)
- **404 Not Found**: Resource doesn't exist
- **409 Conflict**: Business rule violation (duplicate email)
- **500 Internal Server Error**: Server error (unexpected exceptions)

### 4. Handler Ordering
- Most specific exceptions first
- Generic `Exception` handler last as catch-all

## Related Decisions
- [ADR-0004: Use Bean Validation for Request Validation](#)
- [ADR-0006: Use DTO Pattern for Layer Separation](#)
- [ADR-0001: Use Layered Architecture](#)

## References
- [Spring @RestControllerAdvice Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RestControllerAdvice.html)
- [RFC 7807: Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc7807)
- [Spring Boot Error Handling Guide](https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc)
- [OWASP Error Handling Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Error_Handling_Cheat_Sheet.html)

## Notes
- `@RestControllerAdvice` is a specialization of `@ControllerAdvice` for REST APIs
- Exception handling occurs after filters but before the response is sent
- Consider migrating to RFC 7807 Problem Details for better API standards compliance
- Always balance between helpful error messages and security (don't expose internals)
- Monitor error rates in production using metrics and logging
