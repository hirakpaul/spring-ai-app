# Spring AI App - Security Integration Guide

This application has been integrated with the **spring-security-starter** library for API Key-based authentication and Client Application authorization.

## 🎯 What Was Done

### 1. Added Security Library Dependency

The `spring-security-starter` library has been added to [pom.xml:39-44](pom.xml#L39-L44):

```xml
<dependency>
    <groupId>com.rewritesolutions</groupId>
    <artifactId>spring-security-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. Removed Duplicate Security Code

All security-related code has been moved to the reusable library:
- ✅ Removed local `ApiKey`, `ClientApp`, `AuthorizationService`, etc.
- ✅ Using library's `AuthTokenFilter`, `@AuthorizeClient`, and more
- ✅ Clean separation between business logic and security concerns

### 3. Updated Configuration

**SecurityConfig** - [SecurityConfig.java](src/main/java/com/rewritesolutions/ai/spring_ai_app/config/SecurityConfig.java)
- Now imports `AuthTokenFilter` from security library
- Configured to use API key authentication via `X-Auth-Token` header

**GlobalExceptionHandler** - [GlobalExceptionHandler.java](src/main/java/com/rewritesolutions/ai/spring_ai_app/exception/GlobalExceptionHandler.java)
- Updated to handle `UnauthorizedClientException` from security library

### 4. Created Example Controllers

**ApiKeyManagementController** - [ApiKeyManagementController.java](src/main/java/com/rewritesolutions/ai/spring_ai_app/controller/ApiKeyManagementController.java)
- Admin-only endpoints for CRUD operations on API keys
- Create, list, revoke, and delete API keys

**ExternalApiController** - [ExternalApiController.java](src/main/java/com/rewritesolutions/ai/spring_ai_app/controller/ExternalApiController.java)
- Demonstrates **both authorization patterns**:
  1. **Declarative**: `@AuthorizeClient(ClientApp.PEGA)` (recommended)
  2. **Programmatic**: `authorizationService.authorize(...)` (like PEGA example)

### 5. Sample API Keys (Dev Profile)

**DataInitializer** - [DataInitializer.java](src/main/java/com/rewritesolutions/ai/spring_ai_app/config/DataInitializer.java)
- Automatically creates 6 sample API keys on startup (dev profile only)

## 🚀 How to Use

### Start the Application

```bash
mvn clean install
mvn spring-boot:run
```

The app starts with `dev` profile active (see [application.properties](src/main/resources/application.properties#L4))

### Sample API Keys Created on Startup

```
Mobile App:       MOBILE_DEV_TOKEN_67890
Web Portal:       WEB_DEV_TOKEN_ABCDE
External API:     EXTERNAL_DEV_TOKEN_FGHIJ
Admin Dashboard:  ADMIN_DEV_TOKEN_KLMNO
Internal Service: INTERNAL_DEV_TOKEN_PQRST
```

### Test the API

#### 1. Register an Admin User (for Basic Auth)

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com",
    "roles": ["ADMIN"]
  }'
```

#### 2. Access Customer Endpoints (Requires User/Admin Role)

```bash
# Using Basic Auth
curl -X GET http://localhost:8080/api/v1/customers \
  -u admin:admin123
```

#### 3. Access External API Endpoints (Requires API Key)

**Pattern 1: Declarative with @AuthorizeClient**

```bash
# PEGA endpoint (only PEGA token works)
curl -X GET http://localhost:8080/api/v1/external/pega-customers \
  -H "X-Auth-Token: PEGA_DEV_TOKEN_12345"

# Mobile/Web endpoint (both tokens work)
curl -X GET http://localhost:8080/api/v1/external/customers/1 \
  -H "X-Auth-Token: MOBILE_DEV_TOKEN_67890"

curl -X GET http://localhost:8080/api/v1/external/customers/1 \
  -H "X-Auth-Token: WEB_DEV_TOKEN_ABCDE"
```

**Pattern 2: Programmatic (like PEGA example)**

```bash
# Manual authorization in controller
curl -X GET "http://localhost:8080/api/v1/external/pega-data?param1=value1" \
  -H "X-Auth-Token: PEGA_DEV_TOKEN_12345"
```

#### 4. Manage API Keys (Admin Only)

```bash
# List all API keys
curl -X GET http://localhost:8080/api/v1/api-keys \
  -u admin:admin123

# Create new API key
curl -X POST http://localhost:8080/api/v1/api-keys \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "clientApp": "PEGA",
    "description": "Production PEGA integration",
    "allowedEndpoints": ["/api/v1/external/pega-.*"],
    "expiresAt": "2026-12-31T23:59:59"
  }'

# Revoke an API key
curl -X PUT http://localhost:8080/api/v1/api-keys/1/revoke \
  -u admin:admin123

# Delete an API key
curl -X DELETE http://localhost:8080/api/v1/api-keys/1 \
  -u admin:admin123
```

## 📋 Authorization Patterns

### Pattern 1: Declarative (Recommended)

Clean, readable, and easy to maintain:

```java
@GetMapping("/pega-customers")
@AuthorizeClient(ClientApp.PEGA)
public ResponseEntity<List<CustomerResponse>> getPegaCustomers() {
    return ResponseEntity.ok(customerService.getAllCustomers());
}
```

### Pattern 2: Programmatic (For Complex Logic)

More control, useful when migrating from existing patterns:

```java
@GetMapping("/pega-data")
public ResponseEntity<?> getPegaData(HttpServletRequest request) {
    String[] acceptedSources = {ClientApp.PEGA.getAppName()};
    authorizationService.authorize(
        acceptedSources,
        (String) request.getAttribute("authToken"),
        request.getRequestURI()
    );

    // Your business logic
    return ResponseEntity.ok(data);
}
```

## 🔒 Security Configuration

### Current Setup

1. **Basic Auth** - For user authentication (admin, regular users)
2. **API Keys** - For external client authentication (PEGA, Mobile, etc.)
3. **@PreAuthorize** - For role-based authorization on endpoints
4. **@AuthorizeClient** - For client app-based authorization

### Public Endpoints

- `/api/v1/auth/**` - Registration and login
- `/actuator/**` - Health and metrics

### Protected Endpoints

- `/api/v1/customers/**` - Requires USER or ADMIN role (Basic Auth)
- `/api/v1/api-keys/**` - Requires ADMIN role (Basic Auth)
- `/api/v1/external/**` - Requires valid API key (X-Auth-Token header)

## 📊 Error Responses

### 401 Unauthorized (No credentials)
```json
{
  "timestamp": "2025-12-13T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

### 403 Forbidden (Invalid API key)
```json
{
  "timestamp": "2025-12-13T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Invalid or inactive auth token"
}
```

### 403 Forbidden (Wrong client)
```json
{
  "timestamp": "2025-12-13T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Client 'MOBILE_APP' is not authorized to access this endpoint. Accepted sources: [PEGA]"
}
```

## 🎓 Next Steps

### For Development

1. **Test all endpoints** using the sample API keys
2. **Create additional API keys** via the management controller
3. **Add new external endpoints** to `ExternalApiController`
4. **Customize endpoint restrictions** per API key

### For Production

1. **Change profile to `prod`** in application.properties
2. **Generate secure API keys** (DataInitializer won't run in prod)
3. **Set appropriate expiration dates** for API keys
4. **Enable HTTPS** for secure token transmission
5. **Add rate limiting** (consider Spring Cloud Gateway or similar)
6. **Monitor API key usage** via `lastUsedAt` timestamps

### Additional Features to Consider

1. **JWT Authentication** - For user sessions (implement separately)
2. **API Key Rotation** - Automated key rotation mechanism
3. **Usage Analytics** - Track API calls per client
4. **Rate Limiting** - Per-client rate limits
5. **Webhook Support** - Event notifications to clients

## 📚 Documentation

- **Security Library**: `/home/paul/workspace/spring-security-starter/README.md`
- **Usage Guide**: `/home/paul/workspace/spring-security-starter/USAGE_GUIDE.md`

## 🐛 Troubleshooting

### Issue: API key not working

**Solution**: Check that:
1. Token is sent in `X-Auth-Token` header (not Authorization)
2. API key is active (not revoked)
3. API key hasn't expired
4. Endpoint matches allowed patterns

### Issue: "No HTTP request found"

**Solution**: Make sure you're using `@AuthorizeClient` on a controller method (not service method)

### Issue: ClassNotFoundException for security classes

**Solution**: Run `mvn clean install` in both projects:
```bash
cd /home/paul/workspace/spring-security-starter
mvn clean install

cd /home/paul/workspace/spring-ai-app
mvn clean install
```

## ✅ Summary

Your application now has:
- ✅ Reusable security library integrated
- ✅ API key-based authentication for external clients
- ✅ Client app authorization with endpoint restrictions
- ✅ Both declarative and programmatic patterns
- ✅ Admin interface for API key management
- ✅ Sample data for testing
- ✅ Production-ready security configuration

Everything is ready to use! 🚀
