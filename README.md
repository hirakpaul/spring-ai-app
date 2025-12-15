# Spring AI App

A Spring Boot REST API application with secure API key-based authentication using a custom reusable security library.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Database](#database)
- [Testing with Postman](#testing-with-postman)
- [Project Structure](#project-structure)

## Overview

This application provides a REST API for managing customer data with enterprise-grade security. It uses a custom `spring-security-starter` library for API key-based authentication and client authorization.

### Key Features
- ✅ RESTful API for Customer CRUD operations
- ✅ API Key-based authentication
- ✅ Client app authorization (WEB_PORTAL, MOBILE_APP, ADMIN_DASHBOARD, etc.)
- ✅ PostgreSQL database with JPA/Hibernate
- ✅ Docker Compose for easy deployment
- ✅ Actuator endpoints for monitoring
- ✅ Request validation
- ✅ Global exception handling

## Architecture

```
┌─────────────────────┐
│   Client Apps       │
│  (Postman, Web,     │
│   Mobile, PEGA)     │
└──────────┬──────────┘
           │ X-Auth-Token
           ▼
┌─────────────────────┐
│  Spring AI App      │
│  ┌───────────────┐  │
│  │ Controllers   │  │
│  └───────┬───────┘  │
│  ┌───────▼───────┐  │
│  │ Services      │  │
│  └───────┬───────┘  │
│  ┌───────▼───────┐  │
│  │ Repositories  │  │
│  └───────────────┘  │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│ Security Library    │
│ (spring-security-   │
│  starter)           │
│ ┌─────────────────┐ │
│ │ Authorization   │ │
│ │ Service         │ │
│ └─────────────────┘ │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│  PostgreSQL DB      │
│  - customers        │
│  - api_keys         │
└─────────────────────┘
```

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker & Docker Compose**
- **PostgreSQL** (via Docker)
- **spring-security-starter** library (must be installed in local Maven repository)

## Getting Started

### 1. Install Security Library

First, build and install the `spring-security-starter` library:

```bash
cd /home/paul/workspace/spring-security-starter
mvn clean install
```

### 2. Start the Database

Start PostgreSQL using Docker Compose:

```bash
cd /home/paul/workspace/spring-ai-app
docker compose -f src/main/docker/docker-compose.yml up -d
```

This will start PostgreSQL on `localhost:5432` with:
- Database: `compose-postgres`
- Username: `compose-postgres`
- Password: `compose-postgres`

### 3. Build the Application

```bash
mvn clean package
```

### 4. Run the Application

**Option A: Using Maven**
```bash
mvn spring-boot:run
```

**Option B: Using Java**
```bash
java -jar target/spring-ai-app-0.0.1-SNAPSHOT.jar
```

**Option C: Using Docker**
```bash
docker compose -f src/main/docker/docker-compose.yml up
```

The application will start on `http://localhost:8080`

### 5. Insert Sample API Keys

Connect to PostgreSQL and insert sample API keys:

```sql
-- Connect to database
docker exec -it postgres psql -U compose-postgres -d compose-postgres

-- Insert API keys
INSERT INTO api_keys (token, client_app, description, active, expires_at, created_at) VALUES
('web-portal-key-12345', 'WEB_PORTAL', 'Web Portal Client - Read access', true, NOW() + INTERVAL '1 year', NOW()),
('mobile-app-key-67890', 'MOBILE_APP', 'Mobile App Client - Read access', true, NOW() + INTERVAL '1 year', NOW()),
('admin-dashboard-key-11111', 'ADMIN_DASHBOARD', 'Admin Dashboard - Full CRUD', true, NOW() + INTERVAL '1 year', NOW()),
('internal-service-key-22222', 'INTERNAL_SERVICE', 'Internal Service - Full CRUD', true, NOW() + INTERVAL '1 year', NOW()),
('pega-integration-key-33333', 'PEGA', 'PEGA Integration', true, NOW() + INTERVAL '1 year', NOW()),
('external-api-key-44444', 'EXTERNAL_API', 'External API Client', true, NOW() + INTERVAL '1 year', NOW());

-- Verify
SELECT * FROM api_keys;
```

## API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Customer Endpoints

| Method | Endpoint | Description | Allowed Clients |
|--------|----------|-------------|----------------|
| GET | `/customers` | Get all customers | WEB_PORTAL, MOBILE_APP, ADMIN_DASHBOARD, INTERNAL_SERVICE |
| GET | `/customers/{id}` | Get customer by ID | WEB_PORTAL, MOBILE_APP, ADMIN_DASHBOARD, INTERNAL_SERVICE |
| GET | `/customers/email/{email}` | Get customer by email | WEB_PORTAL, MOBILE_APP, ADMIN_DASHBOARD, INTERNAL_SERVICE |
| GET | `/customers/search?term={term}` | Search customers | WEB_PORTAL, MOBILE_APP, ADMIN_DASHBOARD, INTERNAL_SERVICE |
| GET | `/customers/count` | Count customers | WEB_PORTAL, MOBILE_APP, ADMIN_DASHBOARD, INTERNAL_SERVICE |
| POST | `/customers` | Create customer | ADMIN_DASHBOARD, INTERNAL_SERVICE |
| PUT | `/customers/{id}` | Update customer | ADMIN_DASHBOARD, INTERNAL_SERVICE |
| DELETE | `/customers/{id}` | Delete customer | ADMIN_DASHBOARD, INTERNAL_SERVICE |

### Request/Response Examples

#### Create Customer (POST)
```json
POST /api/v1/customers
Headers:
  X-Auth-Token: admin-dashboard-key-11111
  Content-Type: application/json

Body:
{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890"
}

Response (201 Created):
{
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "createdAt": "2025-12-14T03:00:00",
    "updatedAt": "2025-12-14T03:00:00"
}
```

#### Get All Customers (GET)
```json
GET /api/v1/customers
Headers:
  X-Auth-Token: web-portal-key-12345

Response (200 OK):
[
    {
        "id": 1,
        "firstName": "John",
        "lastName": "Doe",
        "email": "john.doe@example.com",
        "phoneNumber": "+1234567890",
        "createdAt": "2025-12-14T03:00:00",
        "updatedAt": "2025-12-14T03:00:00"
    }
]
```

## Security

### Authentication

All API endpoints require the `X-Auth-Token` header:

```
X-Auth-Token: your-api-key-here
```

### Client Applications

The system supports different client application types:

- **WEB_PORTAL** - Read-only access to customer data
- **MOBILE_APP** - Read-only access to customer data
- **ADMIN_DASHBOARD** - Full CRUD access
- **INTERNAL_SERVICE** - Full CRUD access
- **PEGA** - Custom integration access
- **EXTERNAL_API** - Limited access

### Authorization Flow

1. Client sends request with `X-Auth-Token` header
2. `AuthTokenFilter` extracts the token from the header
3. `AuthorizationService` validates the token:
   - Checks if token exists in database
   - Verifies token is active and not expired
   - Validates client app has permission for the endpoint
4. If authorized, request proceeds; otherwise, returns 403 Forbidden

### Error Responses

**403 Forbidden - Unauthorized Client**
```json
{
    "timestamp": "2025-12-14T03:00:00",
    "status": 403,
    "error": "Forbidden",
    "message": "Client WEB_PORTAL is not authorized to access this endpoint"
}
```

**403 Forbidden - Invalid Token**
```json
{
    "timestamp": "2025-12-14T03:00:00",
    "status": 403,
    "error": "Forbidden",
    "message": "Invalid or expired API key"
}
```

## Database

### Schema

**customers** table:
- `id` (BIGSERIAL PRIMARY KEY)
- `first_name` (VARCHAR)
- `last_name` (VARCHAR)
- `email` (VARCHAR UNIQUE)
- `phone_number` (VARCHAR)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**api_keys** table (from security library):
- `id` (BIGSERIAL PRIMARY KEY)
- `token` (VARCHAR UNIQUE)
- `client_app` (VARCHAR)
- `description` (VARCHAR)
- `active` (BOOLEAN)
- `expires_at` (TIMESTAMP)
- `created_at` (TIMESTAMP)
- `last_used_at` (TIMESTAMP)

### Configuration

Database settings are in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/compose-postgres
spring.datasource.username=compose-postgres
spring.datasource.password=compose-postgres
spring.jpa.hibernate.ddl-auto=update
```

## Testing with Postman

### Quick Setup

1. **Import Base URL Variable**
   - Create environment variable: `baseURL` = `http://localhost:8080`

2. **Import API Key Variables**
   - `webToken` = `web-portal-key-12345`
   - `adminToken` = `admin-dashboard-key-11111`

3. **Add Header to All Requests**
   ```
   Key: X-Auth-Token
   Value: {{adminToken}}
   ```

### Example Requests

**Get All Customers (Read Access)**
```
GET {{baseURL}}/api/v1/customers
Headers:
  X-Auth-Token: {{webToken}}
```

**Create Customer (Admin Only)**
```
POST {{baseURL}}/api/v1/customers
Headers:
  X-Auth-Token: {{adminToken}}
  Content-Type: application/json
Body:
{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phoneNumber": "+1987654321"
}
```

## Project Structure

```
spring-ai-app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/rewritesolutions/ai/spring_ai_app/
│   │   │       ├── controller/       # REST controllers
│   │   │       ├── dto/              # Data Transfer Objects
│   │   │       ├── entity/           # JPA entities
│   │   │       ├── exception/        # Custom exceptions & handlers
│   │   │       ├── repository/       # Spring Data JPA repositories
│   │   │       ├── service/          # Business logic
│   │   │       └── SpringAiAppApplication.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   └── application-dev.properties
│   │   └── docker/
│   │       └── docker-compose.yml
│   └── test/
│       └── java/
│           └── com/rewritesolutions/ai/spring_ai_app/
│               └── repository/
├── pom.xml
├── .gitignore
├── README.md
└── LICENSE
```

## Dependencies

Key dependencies:
- Spring Boot 3.5.8
- Spring Data JPA
- PostgreSQL Driver
- Spring Boot Validation
- Spring Boot Actuator
- Lombok
- **spring-security-starter** (custom library)

## Monitoring

Health check endpoint:
```
GET http://localhost:8080/actuator/health
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

See [LICENSE](LICENSE) file for details.

## Contact

For questions or support, please contact the development team.

---

**Built with ❤️ using Spring Boot and the custom spring-security-starter library**
