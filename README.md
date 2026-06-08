# Core-Banking-Loan

Loan microservice for the Core Banking system. Handles loan applications, approvals, and lifecycle management using reactive programming.

## Features

- Apply for a loan (personal, mortgage, auto, business, education)
- Automatic EMI calculation using the standard amortisation formula
- Approve and close loans
- Account validation via Account service
- Reactive/non-blocking with Spring WebFlux + R2DBC
- Kafka integration for event-driven processing
- JWT authentication with role-based access control
- Distributed tracing with MDC + X-Trace-Id header

## Tech Stack

- Java 21 with Spring Boot WebFlux
- R2DBC for reactive database access (PostgreSQL)
- Apache Kafka for messaging
- Redis for caching
- Eureka for service discovery
- Micrometer + Prometheus for metrics

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/loans/apply` | Apply for a new loan | ADMIN, MANAGER, USER |
| POST | `/loans/approve?loanId={uuid}` | Approve a loan | ADMIN, MANAGER |
| POST | `/loans/close?loanId={uuid}` | Close a loan | ADMIN, MANAGER |
| GET | `/loans/getById?loanId={uuid}` | Get loan by ID | ADMIN, MANAGER, USER |
| GET | `/loans/getByCustomer?customerId={uuid}` | Get loans by customer | ADMIN, MANAGER |

## Loan Types

`PERSONAL` · `MORTGAGE` · `AUTO` · `BUSINESS` · `EDUCATION`

## Loan Statuses

`PENDING` → `ACTIVE` → `CLOSED` / `DEFAULTED`

## Configuration

Key properties in `application.properties`:

```properties
server.port=8083
spring.application.name=loan-service
eureka.instance.appName=LOAN-SERVICE
```

## Running Locally

```bash
mvn clean package -DskipTests
java -jar target/loan-*.jar
```
