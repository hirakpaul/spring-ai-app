# ADR-0003: Use SpringDoc OpenAPI for API Documentation

## Status
Accepted

## Date
2025-12-28

## Context
The Spring AI application exposes REST API endpoints that need to be documented for:
- Frontend developers consuming the API
- Third-party integrators
- Internal testing and development
- API contract definition

We need a solution that:
- Automatically generates API documentation from code
- Provides an interactive API testing interface
- Follows industry standards (OpenAPI/Swagger)
- Integrates seamlessly with Spring Boot
- Requires minimal maintenance overhead

## Decision
We will use **SpringDoc OpenAPI 3** (formerly Springfox) with Swagger UI for automatic API documentation generation.

### Specific Choice
- **Library**: `springdoc-openapi-starter-webmvc-ui` version 2.7.0
- **Specification**: OpenAPI 3.0
- **UI**: Swagger UI (bundled)
- **Documentation Style**: Annotations on controllers and DTOs

## Consequences

### Positive
- **Automatic Generation**: API docs generated automatically from code
- **Interactive UI**: Swagger UI allows testing endpoints without external tools
- **Standard Compliance**: Follows OpenAPI 3.0 specification
- **Type Safety**: Documentation tied to actual code, reducing doc drift
- **Developer Experience**: Developers can explore and test APIs easily
- **Client Code Generation**: OpenAPI spec can generate client libraries
- **No Separate Documentation**: Single source of truth (code = docs)
- **Active Maintenance**: SpringDoc is actively maintained for Spring Boot 3+
- **Zero Configuration**: Works out-of-the-box with Spring Boot

### Negative
- **Runtime Dependency**: Adds library dependency to production build
- **Annotation Overhead**: Requires additional annotations in code
- **Learning Curve**: Developers need to learn OpenAPI annotations
- **Performance Impact**: Minimal overhead for documentation generation
- **UI Exposure Risk**: Swagger UI should be disabled/secured in production

### Trade-offs
- Chose automatic generation over manual documentation for consistency
- Accepted annotation overhead for always-current documentation
- Prioritized developer experience over minimal dependencies
- Traded some code verbosity for comprehensive API docs

## Alternatives Considered

### 1. Springfox (Legacy Swagger)
- **Pros**: Mature, widely used
- **Cons**: Not actively maintained, incompatible with Spring Boot 3+
- **Rejected**: SpringDoc is the modern replacement for Spring Boot 3

### 2. Manual API Documentation (Markdown)
- **Pros**: Full control, no dependencies
- **Cons**: High maintenance, docs drift from code, no interactive UI
- **Rejected**: Too much manual effort, prone to becoming outdated

### 3. Postman Collections
- **Pros**: Interactive, widely used for testing
- **Cons**: Not code-generated, requires separate maintenance
- **Rejected**: Doesn't auto-generate from code

### 4. AsyncAPI (for async APIs)
- **Pros**: Specialized for event-driven APIs
- **Cons**: Our API is REST-based, not event-driven
- **Rejected**: Not applicable for REST APIs

### 5. Spring REST Docs
- **Pros**: Test-driven documentation, production-safe
- **Cons**: Requires writing tests, more complex setup
- **Rejected**: Higher complexity for current needs; may reconsider later

## Implementation Details

### Maven Dependency
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

### Configuration (application.properties)
```properties
# Swagger UI path
springdoc.swagger-ui.path=/swagger-ui.html

# API docs JSON path
springdoc.api-docs.path=/v3/api-docs

# Enable Swagger UI
springdoc.swagger-ui.enabled=true

# Packages to scan
springdoc.packages-to-scan=com.rewritesolutions.ai.spring_ai_app.controller

# Paths to include
springdoc.paths-to-match=/api/**
```

### Controller Annotations
```java
@RestController
@Tag(name = "Customer API", description = "Customer management operations")
@RequestMapping("/api/v1/customers")
public class CustomerController {

    @Operation(
        summary = "Create a new customer",
        description = "Creates a new customer and returns the created customer details"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Customer created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
        @Valid @RequestBody CustomerRequest request) {
        // Implementation
    }
}
```

### DTO Annotations
```java
@Schema(name = "CustomerRequest", description = "Request payload for creating or updating a customer")
public class CustomerRequest {

    @NotBlank(message = "First name is required")
    @Schema(
        description = "Customer first name",
        example = "John",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String firstName;
}
```

### Access URLs
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

## Best Practices

### 1. Comprehensive Annotations
- Use `@Tag` to group related endpoints
- Use `@Operation` for endpoint descriptions
- Use `@ApiResponses` to document all possible responses
- Use `@Schema` on DTOs for field descriptions

### 2. Example Values
- Provide realistic `example` values in `@Schema` annotations
- Examples help API consumers understand expected formats

### 3. Error Documentation
- Document all error responses (400, 404, 500, etc.)
- Include error response schemas

### 4. Security Documentation
- Document authentication/authorization requirements (when implemented)
- Use `@SecurityRequirement` annotations

### 5. Production Configuration
```properties
# Disable Swagger UI in production
springdoc.swagger-ui.enabled=false
# Or secure it with Spring Security
```

## Documentation Structure

### API Information
Configured programmatically or via annotations:
```java
@OpenAPIDefinition(
    info = @Info(
        title = "Customer Management API",
        version = "1.0",
        description = "REST API for managing customer data",
        contact = @Contact(name = "Rewrite Solutions", email = "support@rewritesolutions.com")
    )
)
```

### Tags Organization
- **Customer API**: All customer-related endpoints
- Future tags: Authentication, Admin, Reports, etc.

## Security Considerations

### Production Deployment
1. **Disable Swagger UI in Production** (or secure with authentication)
2. **API Key Protection**: Add Spring Security if exposing OpenAPI spec
3. **Rate Limiting**: Implement rate limiting on documentation endpoints
4. **CORS Configuration**: Configure CORS appropriately

### Development vs Production
```properties
# Development (application-dev.properties)
springdoc.swagger-ui.enabled=true

# Production (application-prod.properties)
springdoc.swagger-ui.enabled=false
```

## Maintenance and Evolution

### Keeping Documentation Current
1. **Code Reviews**: Ensure annotations are updated with code changes
2. **CI/CD Integration**: Validate OpenAPI spec in CI pipeline
3. **Breaking Changes**: Use semantic versioning for API versions
4. **Deprecation**: Use `@Deprecated` annotations and mark in docs

### Version Management
- Consider API versioning strategy (URL versioning: `/api/v1`, `/api/v2`)
- Document deprecation timelines
- Maintain backward compatibility within major versions

## Integration with Development Workflow

### Local Development
1. Start application
2. Navigate to `http://localhost:8080/swagger-ui.html`
3. Test endpoints interactively
4. Export OpenAPI spec for client generation

### CI/CD Pipeline
- **Optional**: Generate static HTML documentation from OpenAPI spec
- **Optional**: Publish API docs to documentation portal
- **Optional**: Use contract testing against OpenAPI spec

## Code Generation
OpenAPI spec enables:
- Client SDK generation (JavaScript, Python, Java, etc.)
- Server stub generation
- API testing tools integration
- Validation and mocking

## Related Decisions
- [ADR-0001: Use Layered Architecture](#)
- [ADR-0004: Use Bean Validation for Request Validation](#)
- [ADR-0006: Use DTO Pattern for Layer Separation](#)

## References
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification 3.0](https://spec.openapi.org/oas/v3.0.0)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
- [Spring Boot Integration Guide](https://springdoc.org/#spring-boot-integration)

## Notes
- SpringDoc OpenAPI 2.7.0 is compatible with Spring Boot 3.5.8
- Swagger UI provides a better developer experience than raw Postman
- Consider migrating to Spring REST Docs if test-driven documentation becomes important
- Monitor library updates for security patches and new features
