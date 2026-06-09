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

## Loan Process

### 1. Apply for a Loan (`POST /loans/apply`)

```
Client → LoanController → LoanProcess → LoanService
```

1. The request body (`LoanDTO`) carries `customerId`, `accountId`, `loanType`, `principalAmount`, `interestRate`, `termMonths`, and optional `startDate`.
2. **Account validation** — `LoanProcess` calls `LoanService.isAccountValid()`, which hits `ACCOUNT-SERVICE /accounts/validateAccount` via `WebClient`. A circuit breaker (Resilience4j) protects the call; if the account service is unavailable the fallback returns `false` and the application is rejected.
3. **Account detail fetch** — on a valid account, `LoanService.getAccountDetail()` retrieves the full account payload from `ACCOUNT-SERVICE /accounts/getAccount`.
4. **EMI calculation** — `LoanProcess.calculateMonthlyPayment()` dispatches to the correct formula based on the loan type (see [EMI Calculation](#emi-calculation) below).
5. **Loan creation** — a `Loan` entity is built with status `PENDING`, `remainingBalance = principalAmount`, computed `startDate` / `endDate`, and persisted via R2DBC (`LoanRepository`).
6. The saved loan is returned with HTTP **201 Created**.

```
PENDING  ──(approve)──►  ACTIVE  ──(close)──►  CLOSED
                                          └──────────►  DEFAULTED
```

---

### 2. Approve a Loan (`POST /loans/approve?loanId={uuid}`)

- Restricted to **ADMIN** and **MANAGER** roles.
- Loads the loan by ID, sets `loanStatus = ACTIVE`, and saves it.
- Returns the updated loan with HTTP **200 OK**.

---

### 3. Close a Loan (`POST /loans/close?loanId={uuid}`)

- Restricted to **ADMIN** and **MANAGER** roles.
- Loads the loan by ID, sets `loanStatus = CLOSED`, and saves it.
- Returns the updated loan with HTTP **200 OK**.

---

### EMI Calculation

The monthly payment formula depends on the loan type:

| Loan Type | Calculation Method | Formula |
|-----------|-------------------|---------|
| `PERSONAL` | Amortising EMI | $M = \dfrac{P \cdot r(1+r)^n}{(1+r)^n - 1}$ |
| `BUSINESS` | Amortising EMI | Same as PERSONAL |
| `EDUCATION` | Amortising EMI | Same as PERSONAL |
| `MORTGAGE` | Monthly Compound | $A = P\left(1 + \dfrac{r}{12}\right)^{12t}$, then $M = A / n$ |
| `AUTO` | Annual Simple Interest | $A = P(1 + r \cdot t)$, then $M = A / n$ |

**Add-on Interest (Flat Rate):**

When `ADD_ON` calculation type is used:
- Total Interest = $P \cdot r \cdot t$
- Total Amount = $P +$ Total Interest
- Monthly Payment = Total Amount ÷ $n$

Where:
- **P** = principal amount
- **r** = annual interest rate / 100 (monthly rate = r / 12 for amortising)
- **n** = term in months
- **t** = term in years (= n / 12)

If `annualRate = 0`, all methods fall back to `M = P / n` (zero-interest equal instalments).

---

### Kafka Integration

| Direction | Topic | Consumer Group | Purpose |
|-----------|-------|----------------|---------|
| Consume | `account-events` | `loan-service-group` | React to account changes affecting loans |
| Consume | `transaction-events` | `loan-service-group` | Process repayments / balance updates |
| Produce | configurable | — | Publish loan lifecycle events (encrypted AES) |

Outbound messages are **AES-encrypted** before being sent to Kafka. The secret key is injected via `encryption.secret-key` in `application.properties`.

---

### Resilience & Observability

| Concern | Implementation |
|---------|---------------|
| Circuit breaker | Resilience4j on `accountService` calls |
| Reactive pipeline | Spring WebFlux + R2DBC (non-blocking end-to-end) |
| Distributed tracing | MDC + `X-Trace-Id` header propagated by `TraceIdFilter` |
| Metrics | Micrometer → Prometheus (`/actuator/prometheus`) |
| Service discovery | Eureka (`LOAN-SERVICE`) |

---

## Running Locally

```bash
mvn clean package -DskipTests
java -jar target/loan-*.jar
```
