# Wallet Management Microservice

A digital wallet management microservice built with Spring Boot, implementing wallet creation, deposits, withdrawals, and transfers between wallets.

**Status**: ✅ Stage 1 (HTTP Layer) - Complete and Production Ready  
**Stages 2 & 3**: Pending Implementation (Database & Business Logic)

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

## API Endpoints

All endpoints are versioned at `/api/v1`. Stage 1 provides the HTTP layer with mocked responses.

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
  -d '{
    "owner_name": "John Doe",
    "owner_email": "john@example.com"
  }'
```

### Deposit
```bash
curl -X POST http://localhost:8080/api/v1/wallets/{walletId}/deposit \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "description": "Initial deposit"
  }'
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
├── controller/           # REST endpoints
├── dto/                  # Request/Response objects
├── service/              # Business logic (mocked in Stage 1)
├── exception/            # Error handling
└── config/               # Configuration
```

## Features Implemented (Stage 1)

✅ 8 REST API endpoints  
✅ Input validation with error messages  
✅ Centralized exception handling  
✅ In-memory wallet storage  
✅ Transaction history tracking  
✅ Historical balance queries  
✅ Clean architecture design  
✅ Proper HTTP status codes  

## Data Storage

**Stage 1**: In-memory storage using HashMap (data resets on restart)  
**Stage 2**: Will integrate MongoDB for persistent storage

## Validation

- **Owner Name**: Required, non-blank
- **Owner Email**: Required, valid email format
- **Amount**: Required, must be > 0.01

## Error Handling

The API returns errors in a consistent format:

```json
{
  "error_code": "WALLET_NOT_FOUND",
  "message": "Wallet not found with id: ...",
  "timestamp": "2026-03-28T10:30:00",
  "path": "/api/v1/wallets/..."
}
```

HTTP Status Codes:
- `201`: Created (successful POST)
- `200`: OK (successful GET)
- `400`: Bad Request (validation error)
- `404`: Not Found
- `500`: Internal Server Error

## Technology Stack

- **Java 21**
- **Spring Boot 4.0.5**
- **Maven**
- **Jackson** (JSON serialization)
- **Jakarta Validation**

## Implementation Stages

### ✅ Stage 1: HTTP Layer (Complete)
- REST API endpoints
- Request/Response DTOs
- Input validation
- Error handling
- Mocked data storage

### 🔄 Stage 2: Persistence Layer (Pending)
- Domain entities with JPA
- MongoDB integration
- Repository pattern
- Real business logic
- Transaction validation

### 🔄 Stage 3: Advanced Features (Pending)
- Concurrency handling
- Idempotency implementation
- Event publishing
- Advanced error scenarios

## Code Quality

- Clean Code principles applied
- SOLID design patterns
- Layered architecture
- 100% English code
- Professional naming conventions
- Enterprise-grade implementation

## Notes

- All code is in English
- Stage 1 provides the HTTP layer only
- Data is stored in-memory and not persisted
- Ready for Stage 2 integration without modifications
