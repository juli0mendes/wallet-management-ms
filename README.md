# Wallet Management Microservice

A production-grade digital wallet management microservice built with Spring Boot, following Clean Architecture and Domain-Driven Design principles. Supports wallet creation, deposits, withdrawals, and transfers with persistent storage in MongoDB.

**Status**: ✅ Production-ready API (MongoDB persistence, Actuator health checks, Docker support)

## Quick Start

### Requirements
- Docker Desktop (or Docker Engine + Compose plugin)
- Java 21
- Maven

### 1. Start MongoDB (Docker)
```bash
cd docker
docker compose up -d
```

MongoDB will be available at `localhost:27017`.

Connection URI used by the application:
```text
mongodb://walletadmin:walletpass@localhost:27017/wallet_management?authSource=admin
```

To override the URI at runtime:
```bash
export MONGODB_URI='mongodb://walletadmin:walletpass@localhost:27017/wallet_management?authSource=admin'
```

### Stop MongoDB
```bash
cd docker
docker compose down
```

### 2. Build
```bash
mvn clean package -DskipTests
```

### 3. Run
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api/v1`

### 4. Health Check (Actuator)

Only the health endpoint is exposed for operational checks:

```bash
curl http://localhost:8080/actuator/health
```

Expected output:

```json
{"status":"UP"}
```

## Postman: Import Collection + Environment and switch target in one click

Ready-to-import files:

- Collection: `wallet-management-ms.postman_collection.json`
- Local Environment: `wallet-management-ms.local.postman_environment.json`
- Docker Environment: `wallet-management-ms.docker.postman_environment.json`

Quick steps:

1) In Postman, click **Import** and import the collection plus both environments.
2) In the top-right corner, select the active environment:
   - **Wallet Management MS - Local** for local execution.
   - **Wallet Management MS - Docker** for container execution.
3) Run the collection requests as usual; target switching is handled automatically through `baseUrl`.

Note: if your Docker port differs from `8080`, update only `baseUrl` in the Docker environment.

## Run with Docker

Use this approach when you want to run the app image built from the project's `Dockerfile` and keep MongoDB running from the existing `docker-compose` setup.

### Full flow (Mongo via docker-compose + app via Dockerfile)

1) Start MongoDB with compose:

```bash
cd docker
docker compose up -d
cd ..
```

2) Build the application image from `Dockerfile`:

```bash
docker build -t wallet-management-ms:latest .
```

3) Run the application container:

```bash
docker run --rm -p 8080:8080 \
  -e MONGODB_URI='mongodb://walletadmin:walletpass@host.docker.internal:27017/wallet_management?authSource=admin' \
  --name wallet-management-ms \
  wallet-management-ms:latest
```

4) Validate health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

5) Stop everything when done:

```bash
docker stop wallet-management-ms
cd docker
docker compose down
```

> Linux note: if `host.docker.internal` is not available, use your Docker host gateway/IP in `MONGODB_URI`.

### Build image

```bash
docker build -t wallet-management-ms:latest .
```

### Run container

```bash
docker run --rm -p 8080:8080 \
  -e MONGODB_URI='mongodb://walletadmin:walletpass@host.docker.internal:27017/wallet_management?authSource=admin' \
  --name wallet-management-ms \
  wallet-management-ms:latest
```

After startup, validate health:

```bash
curl http://localhost:8080/actuator/health
```

## API Endpoints

All endpoints are versioned at `/api/v1`.

### Wallet Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/wallets` | Create a new wallet |
| GET | `/wallets/{walletId}` | Get wallet details |
| GET | `/wallets/{walletId}/balance` | Get current balance |
| GET | `/wallets/{walletId}/balance?at=datetime` | Get balance at specific time |
| POST | `/wallets/{walletId}/deposit` | Deposit funds |
| POST | `/wallets/{walletId}/withdraw` | Withdraw funds |
| GET | `/wallets/{walletId}/transactions` | Get transaction history |

### Transfer Operations
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/transfers` | Transfer between wallets |

## API Examples

### Create Wallet
```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"owner_name": "John Doe", "owner_email": "john@example.com"}'
```

### Deposit
```bash
curl -X POST http://localhost:8080/api/v1/wallets/{walletId}/deposit \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "description": "Initial deposit"}'
```

### Get Balance
```bash
curl -X GET http://localhost:8080/api/v1/wallets/{walletId}/balance
```

### Transfer
```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "source_wallet_id": "uuid-1",
    "destination_wallet_id": "uuid-2",
    "amount": 50.00,
    "description": "Payment"
  }'
```

## Project Structure

```
src/main/java/io/github/juli0mendes/walletmanagement/
├── domain/
│   ├── model/              # Domain entities (Wallet, Transaction)
│   ├── exception/          # Domain exceptions (InvalidWalletException, InsufficientBalanceException)
│   └── repository/         # Repository interfaces (ports)
├── application/
│   └── usecase/            # Business use cases
├── infrastructure/
│   ├── configuration/      # Spring configuration
│   └── persistence/
│       ├── document/       # MongoDB documents
│       ├── mapper/         # Domain ↔ Document mappers
│       └── repository/     # MongoDB repository implementations
├── controller/             # REST endpoints
├── dto/                    # Request/Response DTOs (Java Records)
├── exception/              # Global exception handling
└── config/                 # Jackson / app configuration
```

## Features Implemented

✅ 8 REST API endpoints  
✅ Input validation with error messages  
✅ Centralized exception handling  
✅ **MongoDB persistent storage** with embedded MongoDB for tests  
✅ Transaction history tracking  
✅ Historical balance queries  
✅ **Clean Architecture** (domain isolated from frameworks)  
✅ **Domain-driven design** with enforced business rules  
✅ **Domain-specific exceptions** (InvalidWalletException, InsufficientBalanceException)  
✅ **Java Records for DTOs** (immutable, concise)  
✅ **Embedded MongoDB for unit tests** (in-memory persistence)  
✅ Proper HTTP status codes  
✅ High test coverage (>95% on domain + persistence layers)  
✅ Spring Actuator health endpoint exposure (`/actuator/health`)  
✅ Structured operational logs on controllers and exception handling  
✅ Multi-stage Docker image for portable execution  

## Observability

- **Health endpoint**: `/actuator/health`
- **Exposed Actuator endpoints**: health only
- **Default log level**: `INFO` (application), `WARN` (Spring Web)
- **Where logs are generated**:
  - Request lifecycle in wallet and transfer controllers
  - Validation/business/runtime exceptions in global exception handler

To inspect container logs:

```bash
docker logs -f wallet-management-ms
```

## Data Storage

All data is persisted in MongoDB in two collections:

| Collection | Description |
|---|---|
| `wallets` | Wallet documents (id, ownerName, ownerEmail, balance, createdAt, updatedAt) |
| `transactions` | Transaction documents per wallet (type, amount, balanceAfter, createdAt) |

## Validation

- **Owner Name**: Required, minimum 3 characters
- **Owner Email**: Required, valid email format
- **Amount**: Required, must be > 0

## Error Handling

The API returns errors in a consistent format:

```json
{
  "error_code": "WALLET_NOT_FOUND",
  "message": "Wallet not found with id: ...",
  "timestamp": "2026-03-29T10:30:00",
  "path": "/api/v1/wallets/..."
}
```

HTTP Status Codes:
- `201`: Created
- `200`: OK
- `400`: Bad Request (validation / invalid input)
- `404`: Not Found
- `422`: Unprocessable Entity (insufficient balance)
- `500`: Internal Server Error

## Technology Stack

- **Java 21**
- **Spring Boot 3.4.5**
- **Spring Data MongoDB**
- **Spring Boot Actuator**
- **MongoDB 7.0** (Docker)
- **de.flapdoodle.embed.mongo** (Embedded MongoDB for tests)
- **Maven**
- **Docker**
- **Jackson**
- **Jakarta Validation**
- **JUnit 5 + Mockito**
- **JaCoCo** (test coverage)

## Testing Strategy

### Unit Tests
- **Domain model tests** (`WalletTest`): Full coverage of business rules with domain exceptions
- **DTO tests**: Verify Record structure and immutability
- **Use case tests**: Mock repositories, test orchestration logic

### Integration Tests
- **MongoDB integration** (`MongoWalletRepositoryTest`): Uses `@DataMongoTest` with embedded MongoDB
- **Mapper tests** (`WalletMapperTest`, `TransactionMapperTest`): Domain ↔ Document conversions
- **Repository adapter tests**: Spring Data integration

### Local Development
- Embedded MongoDB automatically spins up for `@DataMongoTest` tests
- Application context tests use `@MockBean` for Spring Data repositories
- No real MongoDB connection required for running test suite

## Implementation Stages

### ✅ Stage 1: HTTP Layer
- REST API endpoints
- Request/Response DTOs
- Input validation
- Error handling

### ✅ Stage 2: Application Layer
- Use cases (CreateWallet, Deposit, Withdraw, Transfer, GetBalance, GetTransactions, GetHistoricalBalance)
- Domain model and business rules
- Repository interface (port)

### ✅ Stage 3: Domain + MongoDB Persistence
- **Domain entity** (`domain.model.Wallet`) with enforced business rules
- **Domain exceptions** (InvalidWalletException, InsufficientBalanceException)
- **Java Records for DTOs** (immutable data transfer objects)
- **MongoDB documents** (WalletDocument, TransactionDocument)
- **Domain ↔ Document mappers** (WalletMapper, TransactionMapper)
- **MongoWalletRepository** implementing domain repository port
- **Real MongoDB persistence** with Docker Compose setup
- **Embedded MongoDB for tests** (de.flapdoodle.embed.mongo)
- **Consolidated package structure** under `io.github.juli0mendes.walletmanagement`
- **High test coverage** with integration tests using embedded MongoDB

## Code Quality

- **Clean Code**: Follows Clean Code principles and naming conventions
- **SOLID Design**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **DDD**: Domain-driven design with aggregates, value objects, and bounded contexts
- **Layered Architecture**: Clear separation of concerns (domain, application, infrastructure, presentation)
- **Immutability**: Java Records for DTOs ensure thread safety and reduce bugs
- **Test Coverage**: >95% on domain and infrastructure persistence layers
- **Production-Ready**: Enterprise-grade implementation with proper error handling, logging, and validation

## Design Decisions and Requirement Mapping

This section explains the main design choices and how they address functional and non-functional requirements.

### Functional requirements

- **Wallet lifecycle and money movement (`/wallets`, `/transfers`)**
  - Decision: use explicit use cases (`CreateWalletUseCase`, `DepositUseCase`, `WithdrawUseCase`, `TransferUseCase`) with a domain-centric model.
  - Why: keeps business rules (amount validation, insufficient balance handling, transfer consistency) centralized and testable.

- **Historical balance and transaction traceability**
  - Decision: persist wallet state and transaction ledger separately in MongoDB (`wallets` + `transactions`).
  - Why: enables reconstruction of account history and audit-friendly transaction listing per wallet.

- **Input and business validation**
  - Decision: combine Jakarta Validation at DTO boundary with domain exceptions in core model.
  - Why: invalid input is rejected early (`400`), and domain invariants are enforced consistently (`422` for insufficient balance).

### Non-functional requirements

- **Maintainability and extensibility**
  - Decision: apply Clean Architecture + DDD layering (domain, application, infrastructure, controller).
  - Why: business logic is isolated from frameworks and storage details, reducing coupling and easing future changes.

- **Operational reliability and diagnosability**
  - Decision: expose Spring Actuator health endpoint only (`/actuator/health`) and add structured logs in controllers/exception handler.
  - Why: enables safe runtime monitoring and faster incident triage with minimal operational surface.

- **Portability and environment consistency**
  - Decision: provide multi-stage `Dockerfile` and documented Mongo startup via `docker-compose`.
  - Why: standardizes execution across machines with reduced local setup variability.

## Trade-offs and Time Constraints

Given time constraints, some capabilities were prioritized while others were deferred.

- **BDD/E2E automation (Cucumber/Testcontainers)**
  - Trade-off: current coverage emphasizes unit + integration tests and functional API validation scripts.
  - Deferred: a full BDD suite with Cucumber scenarios and Testcontainers-managed dependencies.

- **Load and performance testing**
  - Trade-off: correctness and API contract validation were prioritized.
  - Deferred: dedicated performance baselines (throughput, latency percentiles, stress and soak tests).

- **Resilience and chaos-style validation**
  - Trade-off: health checks and error handling are in place, but advanced resilience experiments were not included.
  - Deferred: fault injection, dependency degradation tests, and formal recovery SLO validation.

- **Operational hardening depth**
  - Trade-off: health and logging are implemented with conservative defaults.
  - Deferred: broader observability stack (metrics dashboards, tracing, alert rules) and stricter production profiles.

These trade-offs do not block functional correctness, but they are recommended next steps before high-scale production rollout.

## Technical Debt (To Be Addressed)

- Pagination on queries/endpoints that return lists
- HATEOAS support

## Running Tests

```bash
# Run all tests (including MongoDB embedded integration tests)
mvn test

# Run with coverage report
mvn test jacoco:report

# View coverage at: target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=WalletTest

# Run specific test method
mvn test -Dtest=WalletTest#shouldRejectNegativeBalance
```

## Notes

- All code is in English following professional conventions
- Clean Architecture ensures domain logic is isolated from infrastructure and frameworks
- MongoDB is the production persistence layer; embedded MongoDB handles tests
- No in-memory repositories; all persistence is through MongoDB
- Ready for horizontal scaling and microservice architecture
- Production-ready error handling and validation on all operations

