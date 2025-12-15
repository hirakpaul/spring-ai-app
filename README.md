# Spring AI App

A Spring Boot REST API application for managing customer data with PostgreSQL database integration.

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Database](#database)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Monitoring](#monitoring)
- [Contributing](#contributing)

## Overview

This application provides a REST API for managing customer data with a clean, modern architecture using Spring Boot and PostgreSQL.

### Key Features
- ✅ RESTful API for Customer CRUD operations
- ✅ PostgreSQL database with JPA/Hibernate
- ✅ Docker Compose for easy deployment
- ✅ Actuator endpoints for monitoring
- ✅ Request validation with Bean Validation
- ✅ Global exception handling
- ✅ Clean layered architecture (Controller → Service → Repository)

## Architecture

```
┌─────────────────────┐
│   Client Apps       │
│  (Postman, Web,     │
│   Mobile, etc.)     │
└──────────┬──────────┘
           │ HTTP REST API
           ▼
┌─────────────────────┐
│  Spring AI App      │
│  ┌───────────────┐  │
│  │ Controllers   │  │
│  │  - Customer   │  │
│  └───────┬───────┘  │
│  ┌───────▼───────┐  │
│  │ Services      │  │
│  │  - Business   │  │
│  │    Logic      │  │
│  └───────┬───────┘  │
│  ┌───────▼───────┐  │
│  │ Repositories  │  │
│  │  - JPA/       │  │
│  │    Hibernate  │  │
│  └───────┬───────┘  │
└──────────┬──────────┘
           │
┌──────────▼──────────┐
│  PostgreSQL DB      │
│  ┌───────────────┐  │
│  │ customers     │  │
│  │  - id         │  │
│  │  - firstName  │  │
│  │  - lastName   │  │
│  │  - email      │  │
│  │  - phone      │  │
│  └───────────────┘  │
└─────────────────────┘
```

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker & Docker Compose**
- **PostgreSQL** (via Docker)

## Getting Started

### Quick Start

```bash
# 1. Start PostgreSQL
docker compose -f src/main/docker/docker-compose.yml up -d

# 2. Run the application
mvn spring-boot:run
```
# 3. Run the application using UI
Right click on docker-compose.yml. Select compose up.
it will start the application, Postgress database

The application will be available at `http://localhost:8080`

### Full Setup Guide

#### 1. Start the Database

Start PostgreSQL using Docker Compose:

```bash
cd /home/paul/workspace/spring-ai-app
docker compose -f src/main/docker/docker-compose.yml up -d
```

This will start PostgreSQL on `localhost:5432` with:
- Database: `compose-postgres`
- Username: `compose-postgres`
- Password: `compose-postgres`

#### 2. Build the Application

```bash
mvn clean package
```

#### 3. Run the Application

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

#### 4. (Optional) Insert Sample Data

Connect to PostgreSQL and insert sample customer data:

```sql
-- Connect to database
docker exec -it postgres psql -U compose-postgres -d compose-postgres

-- Insert sample customers
INSERT INTO customers (first_name, last_name, email, phone_number, created_at, updated_at) VALUES
('John', 'Doe', 'john.doe@example.com', '+1234567890', NOW(), NOW()),
('Jane', 'Smith', 'jane.smith@example.com', '+1987654321', NOW(), NOW()),
('Bob', 'Johnson', 'bob.johnson@example.com', '+1555555555', NOW(), NOW());

-- Verify
SELECT * FROM customers;
```

## API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Customer Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/customers` | Get all customers |
| GET | `/customers/{id}` | Get customer by ID |
| GET | `/customers/email/{email}` | Get customer by email |
| GET | `/customers/search?term={term}` | Search customers by name or email |
| GET | `/customers/count` | Get total count of customers |
| POST | `/customers` | Create a new customer |
| PUT | `/customers/{id}` | Update an existing customer |
| DELETE | `/customers/{id}` | Delete a customer |

### Request/Response Examples

#### Create Customer (POST)
```http
POST /api/v1/customers
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890"
}
```

**Response (201 Created):**
```json
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
```http
GET /api/v1/customers
```

**Response (200 OK):**
```json
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

#### Update Customer (PUT)
```http
PUT /api/v1/customers/1
Content-Type: application/json

{
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "phoneNumber": "+1234567890"
}
```

**Response (200 OK):**
```json
{
    "id": 1,
    "firstName": "John",
    "lastName": "Smith",
    "email": "john.smith@example.com",
    "phoneNumber": "+1234567890",
    "createdAt": "2025-12-14T03:00:00",
    "updatedAt": "2025-12-14T03:10:00"
}
```

#### Delete Customer (DELETE)
```http
DELETE /api/v1/customers/1
```

**Response (204 No Content)**

### Error Responses

**400 Bad Request - Validation Error**
```json
{
    "timestamp": "2025-12-14T03:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "errors": {
        "email": "must be a well-formed email address"
    }
}
```

**404 Not Found**
```json
{
    "timestamp": "2025-12-14T03:00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Customer not found with id: 999"
}
```

**409 Conflict - Duplicate Email**
```json
{
    "timestamp": "2025-12-14T03:00:00",
    "status": 409,
    "error": "Conflict",
    "message": "Customer with email john.doe@example.com already exists"
}
```

## Database

### Schema

**customers** table:
- `id` (BIGSERIAL PRIMARY KEY) - Unique identifier
- `first_name` (VARCHAR NOT NULL) - Customer's first name
- `last_name` (VARCHAR NOT NULL) - Customer's last name
- `email` (VARCHAR UNIQUE NOT NULL) - Customer's email address (must be unique)
- `phone_number` (VARCHAR) - Customer's phone number
- `created_at` (TIMESTAMP) - Record creation timestamp
- `updated_at` (TIMESTAMP) - Last update timestamp

### Configuration

Database settings are in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/compose-postgres
spring.datasource.username=compose-postgres
spring.datasource.password=compose-postgres
spring.jpa.hibernate.ddl-auto=update
```

## Testing

### Using cURL

**Get All Customers**
```bash
curl -X GET http://localhost:8080/api/v1/customers
```

**Get Customer by ID**
```bash
curl -X GET http://localhost:8080/api/v1/customers/1
```

**Get Customer by Email**
```bash
curl -X GET http://localhost:8080/api/v1/customers/email/john.doe@example.com
```

**Search Customers**
```bash
curl -X GET "http://localhost:8080/api/v1/customers/search?term=john"
```

**Get Customer Count**
```bash
curl -X GET http://localhost:8080/api/v1/customers/count
```

**Create Customer**
```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phoneNumber": "+1987654321"
  }'
```

**Update Customer**
```bash
curl -X PUT http://localhost:8080/api/v1/customers/1 \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe-Smith",
    "email": "jane.smith@example.com",
    "phoneNumber": "+1987654321"
  }'
```

**Delete Customer**
```bash
curl -X DELETE http://localhost:8080/api/v1/customers/1
```

### Using Postman

1. **Create Environment Variable**
   - `baseURL` = `http://localhost:8080`

2. **Create Requests**
   - Use the cURL examples above and import them into Postman
   - Or manually create requests using the endpoints from the API Documentation section

3. **Example Collection Structure**
   ```
   Customer API
   ├── Get All Customers (GET {{baseURL}}/api/v1/customers)
   ├── Get Customer by ID (GET {{baseURL}}/api/v1/customers/1)
   ├── Create Customer (POST {{baseURL}}/api/v1/customers)
   ├── Update Customer (PUT {{baseURL}}/api/v1/customers/1)
   └── Delete Customer (DELETE {{baseURL}}/api/v1/customers/1)
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
- **Spring Boot 3.5.8** - Core framework
- **Spring Data JPA** - Database access layer
- **PostgreSQL Driver** - Database connectivity
- **Spring Boot Validation** - Request validation
- **Spring Boot Actuator** - Monitoring and health checks
- **Lombok** - Reduce boilerplate code
- **H2 Database** - In-memory database for testing

## Monitoring

### Health Check

The application includes Spring Boot Actuator for monitoring:

```bash
# Health check
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

### Available Actuator Endpoints

- `/actuator/health` - Application health status
- Additional endpoints can be enabled in [application.properties](src/main/resources/application.properties)

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

**Built with Spring Boot 3.5.8 and Java 21**
