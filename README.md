# ShopSphere
A production-grade backend system for an e-commerce platform, built with reliability, scalability and correctness as first-class principles.

## Project overview
ShopSphere currently focuses on core commerce domains: users, products, carts, orders, and inventory reservations. It is designed to model a realistic backend workflow from catalog creation and cart management to checkout and order confirmation with inventory reservation handling.

## Current status
- User lifecycle: create, update, and fetch users by ID or email.
- Product catalog: create, update, and retrieve products (including lookup by SKU).
- Cart workflow: add/update/remove items, view active cart, and checkout.
- Orders: create pending orders and confirm them.
- Inventory reservations: reserve stock during checkout, consume on confirmation, and clean up expired reservations.

## Why this project exists

This project is built to:
- Learn how production backend systems are designed
- Practice writing scalable and reliable Java services
- Understand concurrency, transactions, and failure handling
- Build something that I can confidently explain in interviews

## What this project will NOT do

- No frontend or UI
- No microservices
- No external payment gateways
- No search engine
- No message queues

## Learning Approach
This project is built incrementally.
System design, database concepts (SQL, joins, transactions),
and scalability patterns will be learned and applied when
required by concrete features, mirroring real-world backend
development practices.

## Prerequisites
- Java 21
- Maven
- PostgreSQL

## Local setup
1. Configure `src/main/resources/application.properties` with your local PostgreSQL connection details:
    - `spring.datasource.url=jdbc:postgresql://localhost:5432/shopsphere`
    - `spring.datasource.username=YOUR_USERNAME`
    - `spring.datasource.password=YOUR_PASSWORD`
2. Flyway migrations run automatically on application startup when `spring.flyway.enabled=true`.
3. Start the Spring Boot app:
    - `./mvnw spring-boot:run`

## Configuration notes
- `spring.datasource.*` points to your PostgreSQL instance.
- `spring.jpa.database-platform` must align with the target database dialect (for local use, set it to the PostgreSQL dialect to match your instance).

## API endpoints
Base paths and examples:
- `/api/v1/users` (create user, update user, get by ID/email)
- `/api/v1/products` (create/update/get products, list all, lookup by SKU)
- `/api/v1/cart` (view cart, add/update/remove items, checkout)
- `/api/v1/orders` (create pending order, view pending/confirmed orders, confirm order)

Example:
```bash
curl -X POST "http://localhost:8080/api/v1/users" \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"supersecure"}'
```

## Database migrations
Flyway is used for schema migrations. Migration files live in `src/main/resources/db/migration`.

## Tech Stack

- Language: Java 21
- Framework: Spring Boot 3
- Database: PostgreSQL
- ORM: Spring Data JPA (Hibernate)
- Schema Management: Flyway
- Caching: Redis (planned)
- Containerization: Docker (planned)