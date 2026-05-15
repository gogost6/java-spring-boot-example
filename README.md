# Catify

A RESTful backend built with Spring Boot 4 and Java 21. Catify is a community platform where users can create posts, leave comments, browse random cat content, and purchase a downloadable cat calendar via Stripe.

**Live:** [https://vps.moqtasvatba.online](https://vps.moqtasvatba.online) · **API:** [https://vpsapi.moqtasvatba.online](https://vpsapi.moqtasvatba.online) · **Swagger:** [https://vpsapi.moqtasvatba.online/swagger-ui/index.html](https://vpsapi.moqtasvatba.online/swagger-ui/index.html)

## Tech Stack

| Layer         | Technology                                                      |
|---------------|-----------------------------------------------------------------|
| Language      | Java 21                                                         |
| Framework     | Spring Boot 4                                                   |
| Security      | Spring Security · JWT (OAuth2 Resource Server) · Refresh Tokens |
| Database      | PostgreSQL · Spring Data JPA · Hibernate                        |
| Migrations    | Flyway                                                          |
| Caching       | Caffeine                                                        |
| Rate Limiting | Bucket4j                                                        |
| Payments      | Stripe                                                          |
| API Docs      | SpringDoc OpenAPI (Swagger UI)                                  |
| Templates     | Thymeleaf                                                       |
| Monitoring    | Spring Actuator                                                 |
| AOP           | Correlation ID logging                                          |
| CI/CD         | GitHub Actions · Docker                                         |
| Frontend      | https://vps.moqtasvatba.online                                  |
| Backend       | https://vpsapi.moqtasvatba.bg                                   |

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL running locally (or via Docker)

### Local setup

1. **Clone the repository**

   ```bash
   git clone https://github.com/georgistoilkov/catify.git
   cd catify
   ```

2. **Configure the database**

   Fill in your database credentials in `application-local.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/catify
   spring.datasource.username=your_user
   spring.datasource.password=your_password
   ```

3. **Set Stripe keys** in `application-local.properties`:

   ```properties
   stripe.secret-key=sk_test_...
   stripe.webhook-secret=whsec_...
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

### Deployment

The application is deployed to a VPS at **https://vpsapi.moqtasvatba.online**.

The `Dockerfile` uses a multi-stage build (Maven builder → slim JRE image with layered JAR extraction) and is used exclusively by the GitHub Actions CI/CD pipeline. On every push, the workflow builds the image and deploys it to the VPS running the `prod` Spring profile.

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

## API Reference

### Auth — `/api/auth`

| Method | Endpoint                      | Auth     | Description                           |
|--------|-------------------------------|----------|---------------------------------------|
| POST   | `/register`                   | Public   | Register a new account                |
| POST   | `/login`                      | Public   | Login and receive JWT + refresh token |
| POST   | `/refresh`                    | Public   | Refresh access token                  |
| POST   | `/logout`                     | Required | Revoke all refresh tokens             |
| PUT    | `/email`                      | Required | Update email address                  |
| PUT    | `/password`                   | Required | Update password                       |
| PUT    | `/users/{email}/roles/{role}` | Admin    | Assign a role to a user               |

### Posts — `/api/posts`

| Method | Endpoint              | Auth     | Description               |
|--------|-----------------------|----------|---------------------------|
| GET    | `/`                   | Public   | Get all posts (paginated) |
| GET    | `/{post_id}`          | Public   | Get a post by ID          |
| GET    | `/{post_id}/comments` | Public   | Get comments for a post   |
| POST   | `/`                   | Required | Create a post             |
| PUT    | `/{id}`               | Required | Update a post             |
| DELETE | `/{id}`               | Required | Delete a post             |

### Comments — `/api/comment`

| Method | Endpoint          | Auth     | Description             |
|--------|-------------------|----------|-------------------------|
| GET    | `/{post_id}/post` | Public   | Get comments by post    |
| GET    | `/{comment_id}`   | Public   | Get a comment by ID     |
| POST   | `/{post_id}`      | Required | Add a comment to a post |
| PUT    | `/{comment_id}`   | Required | Update a comment        |
| DELETE | `/{comment_id}`   | Required | Delete a comment        |

### Users — `/api/users`

| Method | Endpoint   | Auth     | Description              |
|--------|------------|----------|--------------------------|
| GET    | `/me`      | Required | Get current user profile |
| DELETE | `/me`      | Required | Delete current account   |
| GET    | `/{email}` | Required | Get user by email        |

### Payments — `/api/payments`

| Method | Endpoint             | Auth     | Description                                       |
|--------|----------------------|----------|---------------------------------------------------|
| POST   | `/checkout`          | Required | Create a Stripe checkout session for the calendar |
| GET    | `/calendar/status`   | Required | Check purchase status                             |
| GET    | `/calendar/download` | Required | Download the cat calendar PDF                     |
| POST   | `/webhook`           | Public   | Stripe webhook handler                            |

### Cats — `/api/cats`

| Method | Endpoint                   | Auth   | Description             |
|--------|----------------------------|--------|-------------------------|
| GET    | `/random-cat`              | Public | Get a random cat image  |
| GET    | `/random-cat-by-tag/{tag}` | Public | Get a random cat by tag |
| GET    | `/random-cat-gif`          | Public | Get a random cat GIF    |

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration`:

| Version | Description                                    |
|---------|------------------------------------------------|
| V1      | Initial schema (users, posts, comments, roles) |
| V2      | Add timestamps                                 |
| V3      | Refresh tokens table                           |
| V4      | Comment owner relationship                     |
| V5      | Calendar purchases table                       |

## Security

- Stateless JWT authentication using Spring OAuth2 Resource Server
- Separate short-lived access tokens and long-lived refresh tokens
- Role-based access control (`USER`, `ADMIN`)
- Rate limiting per IP via Bucket4j
- CORS configuration for cross-origin frontend clients
- Correlation ID propagated through all log entries for request tracing

## Running Tests

```bash
./mvnw test
```

Test coverage includes unit tests for services and repositories, and integration tests for controllers using `@WebMvcTest` with mocked security context.

## Health Check

```
GET http://localhost:8080/actuator/health
```
