# Wallet Management Microservice

Microservice for digital wallet management built with **Java 21**, **Spring Boot** and **MongoDB**.

## What it does

- Create wallets
- Deposit and withdraw funds
- Transfer between wallets
- List transactions
- Query current and historical balance

Base API path: `http://localhost:8080/api/v1`

## Stack

- Java 21
- Spring Boot 3.4.5
- Spring Data MongoDB
- Spring Boot Actuator
- Maven
- Docker
- JUnit 5, Mockito and JaCoCo

## Requirements

- Java 21
- Maven
- Docker Desktop or Docker Engine with Compose

## Running locally

1. Start MongoDB:

```bash
cd docker
docker compose up -d
cd ..
```

MongoDB URI used by the application:

```text
mongodb://walletadmin:walletpass@localhost:27017/wallet_management?authSource=admin
```

If needed, override it:

```bash
export MONGODB_URI='mongodb://walletadmin:walletpass@localhost:27017/wallet_management?authSource=admin'
```

2. Run the application:

```bash
mvn spring-boot:run
```

3. Check health:

```bash
curl http://localhost:8080/actuator/health
```

4. Stop MongoDB when finished:

```bash
cd docker
docker compose down
```

## Running with Docker

1. Start MongoDB:

```bash
cd docker
docker compose up -d
cd ..
```

2. Build the image:

```bash
docker build -t wallet-management-ms:latest .
```

3. Run the container:

```bash
docker run --rm -p 8080:8080 \
  -e MONGODB_URI='mongodb://walletadmin:walletpass@host.docker.internal:27017/wallet_management?authSource=admin' \
  --name wallet-management-ms \
  wallet-management-ms:latest
```

> On Linux, replace `host.docker.internal` with your Docker host gateway/IP if needed.

## API endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| POST | `/wallets` | Create wallet |
| GET | `/wallets/{walletId}` | Get wallet |
| GET | `/wallets/{walletId}/balance` | Get current balance |
| GET | `/wallets/{walletId}/balance?at=datetime` | Get historical balance |
| POST | `/wallets/{walletId}/deposit` | Deposit funds |
| POST | `/wallets/{walletId}/withdraw` | Withdraw funds |
| GET | `/wallets/{walletId}/transactions` | List transactions |
| POST | `/transfers` | Transfer between wallets |

## Example requests

Create wallet:

```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"owner_name":"John Doe","owner_email":"john@example.com"}'
```

Transfer:

```bash
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "source_wallet_id":"uuid-1",
    "destination_wallet_id":"uuid-2",
    "amount":50.00,
    "description":"Payment"
  }'
```

## Error format

```json
{
  "error_code": "WALLET_NOT_FOUND",
  "message": "Wallet not found with id: ...",
  "timestamp": "2026-03-29T10:30:00",
  "path": "/api/v1/wallets/..."
}
```

Common status codes: `200`, `201`, `400`, `404`, `422`, `500`.

## Postman

Files available in `collection/`:

- `wallet-management-ms.postman_collection.json`
- `wallet-management-ms.local.postman_environment.json`
- `wallet-management-ms.docker.postman_environment.json`

## Tests

Run all tests:

```bash
mvn test
```

Generate coverage report:

```bash
mvn test jacoco:report
```

Coverage output: `target/site/jacoco/index.html`

## Project structure

```text
src/main/java/io/github/juli0mendes/walletmanagement/
├── application/       # Use cases
├── controller/        # REST controllers
├── domain/            # Domain model and repository ports
├── dto/               # Request/response DTOs
├── exception/         # API error handling
├── infrastructure/    # MongoDB adapters, mappers and configuration
└── config/            # Application configuration
```
