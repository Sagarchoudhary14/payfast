# Payment Transaction Processing System

A full-stack payment authorization demo built with **Java 17, Spring Boot 3, Spring Security, JWT, MySQL, React, and Vite**.

## Features

- REST API for payment authorization and transaction history
- JWT login and role-based access (`CUSTOMER`, `ADMIN`)
- Idempotency via the `Idempotency-Key` request header
- Rule-based fraud checks (amount threshold and repeated payments to the same recipient)
- MySQL persistence using Spring Data JPA
- OpenAPI/Swagger UI
- JUnit 5 and Mockito tests
- React dashboard for login, submitting payments, and viewing transactions

> This is a learning/portfolio project, not a production payment processor. It does not connect to a bank, card network, or payment gateway. It stores demo transaction records only. Before production use, add a proper secret manager, refresh-token/session strategy, rate limiting, audit logging, migrations, PCI/security review, and robust idempotency concurrency handling.

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8+

## 1. Create the database

```sql
CREATE DATABASE payment_system;
```

## 2. Configure the backend

Copy `backend/src/main/resources/application-example.yml` to `backend/src/main/resources/application.yml` and set your MySQL username/password and a long random JWT secret (at least 32 bytes).

Or set environment variables:

```bash
export DB_URL='jdbc:mysql://localhost:3306/payment_system?useSSL=false&serverTimezone=UTC'
export DB_USERNAME='root'
export DB_PASSWORD='your-password'
export JWT_SECRET='replace-with-a-long-random-secret-at-least-32-bytes'
```

## 3. Run the backend

```bash
cd backend
mvn spring-boot:run
```

Backend: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Demo accounts are seeded at startup:
- Customer: `customer@example.com` / `Customer123!`
- Admin: `admin@example.com` / `Admin123!`

Change these credentials before sharing/deploying the app. Demo passwords are intentionally simple to make local setup easy.

## 4. Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend: `http://localhost:5173`

## API overview

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Exchange credentials for a JWT |
| POST | `/api/payments` | CUSTOMER, ADMIN | Authorize a payment; send `Idempotency-Key` |
| GET | `/api/payments` | CUSTOMER, ADMIN | List own transactions; admins can list all |
| GET | `/api/payments/{id}` | Owner or ADMIN | Get a transaction |
| GET | `/api/admin/health` | ADMIN | Example admin-only route |

### Example payment request

```bash
curl -X POST http://localhost:8080/api/payments \
  -H 'Authorization: Bearer YOUR_JWT' \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: order-2026-0001' \
  -d '{"recipient":"merchant-123","amount":1250.00,"currency":"INR","description":"Order payment"}'
```

The response contains `status` (`AUTHORIZED` or `DECLINED`) and a `fraudReason` when a rule triggers. Repeating the same request with the same key and user returns the original transaction rather than creating a second one.

## Fraud rules

1. Amount above INR 100,000 is declined.
2. Three or more payments to the same recipient by the same user within five minutes trigger a decline.
3. The rules are intentionally simple and illustrative. They are not a real fraud-detection model.

## Tests

```bash
cd backend
mvn test
```

The included test suite focuses on the payment service's idempotency and fraud-rule behavior. Coverage is not guaranteed to be 90% until you run a coverage tool and expand tests for your environment. To measure coverage, add JaCoCo or run your IDE's coverage runner.

## Project structure

```text
payment-transaction-processing-system/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/payments/
│       │   ├── PaymentApplication.java
│       │   ├── config/
│       │   ├── auth/
│       │   ├── payment/
│       │   └── common/
│       ├── main/resources/
│       └── test/java/com/example/payments/
└── frontend/
    ├── package.json
    ├── index.html
    └── src/
```
