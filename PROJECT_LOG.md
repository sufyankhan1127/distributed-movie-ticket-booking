
# Distributed Movie Ticket Booking System
### Daily Development Log & Engineering Journal

> **Project Type:** Java Backend | Spring Boot | Microservices  
> **Repository:** [GitHub - Distributed Movie Ticket Booking System](https://github.com/sufyankhan1127/distributed-movie-ticket-booking)  
> **Project Root:** `D:\distributed-movie-ticket-booking`  
> **Current Phase:** Core microservices development  
> **Latest Recorded Session:** Day 1 — Foundation, Services & Event Integration

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Project Objectives](#2-project-objectives)
3. [Technology Stack](#3-technology-stack)
4. [Microservices Architecture](#4-microservices-architecture)
5. [Project Structure](#5-project-structure)
6. [Day 1 — Work Completed](#6-day-1--work-completed)
7. [Important Implementation Details](#7-important-implementation-details)
8. [Infrastructure & Local Environment](#8-infrastructure--local-environment)
9. [Build & Run Commands](#9-build--run-commands)
10. [Current Progress](#10-current-progress)
11. [Known Limitations & Future Improvements](#11-known-limitations--future-improvements)
12. [Day 2 — Planned Work](#12-day-2--planned-work)
13. [Development Rules](#13-development-rules)

---

## 1. Project Overview

The **Distributed Movie Ticket Booking System** is a Java-based backend project designed to demonstrate how a real-world ticket booking platform can be developed using a microservices architecture.

Instead of building one large application, the system separates business responsibilities into independent services. These services communicate using REST APIs, while Apache Kafka is used to publish and consume asynchronous events.

The system is being developed incrementally, with an emphasis on understanding the purpose of each component, how services interact, and how the application behaves when failures occur.

### Core business workflow

1. A user registers or logs in.
2. The user browses available movies.
3. The user selects a show and the required number of seats.
4. The Booking Service requests seat reservation from the Inventory Service.
5. The booking is saved with a pending status.
6. A booking event is published to Kafka.
7. The Payment Service validates the booking and creates a pending payment.
8. The payment status is updated to success or failure.
9. The Booking Service is notified of the payment outcome through its status endpoint.
10. Kafka payment events are consumed by the Notification Service.

**Important:** This describes the intended workflow and implemented components. The complete end-to-end workflow has not yet been verified through integration testing.

---

## 2. Project Objectives

The project aims to develop practical knowledge of:

- Java and object-oriented programming in a backend application.
- Spring Boot application development.
- Building REST APIs and connecting independent services.
- Microservices architecture and service discovery.
- API Gateway routing.
- Database persistence using Spring Data JPA.
- MySQL database integration.
- Redis fundamentals and infrastructure setup.
- Apache Kafka producers, consumers, topics, and asynchronous messaging.
- Seat reservation and prevention of overselling.
- Payment and booking status management.
- Exception handling and service-to-service communication.
- Maven multi-module builds.
- Docker and Docker Compose.
- Testing, debugging, and documenting a distributed application.

---

## 3. Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 target | Main programming language and project compilation target |
| Spring Boot 3.5.6 | Application framework |
| Spring Cloud | Microservices infrastructure and service discovery integration |
| Maven | Dependency management and multi-module builds |
| Spring Web | REST controllers and HTTP communication |
| Spring Data JPA | Database access and persistence |
| MySQL | Relational database |
| Spring Kafka | Kafka event publishing and consumption |
| Apache Kafka | Asynchronous event communication |
| Redis | In-memory data store; infrastructure configured |
| Eureka | Service registration and discovery |
| Spring Cloud Gateway | Central API entry point |
| Docker Compose | Local infrastructure orchestration |
| IntelliJ IDEA | Development environment |
| PowerShell | Windows terminal and build commands |
| Git and GitHub | Version control and remote repository |

**Note:** A technology being configured or included in the project does not necessarily mean its application-level integration has been fully implemented or tested.

---

## 4. Microservices Architecture

### 4.1 Service responsibilities

| Module | Port | Responsibility |
|---|---:|---|
| `contracts` | — | Shared DTOs, event models, and API response structures |
| `discovery-server` | 8761 | Eureka service registry |
| `api-gateway` | 8080 | Central entry point for API requests |
| `user-service` | 8081 | User registration, login, and JWT authentication |
| `movie-service` | 8082 | Movie-related operations |
| `inventory-service` | 8083 | Show inventory and seat reservation |
| `booking-service` | 8084 | Booking creation and booking status management |
| `payment-service` | 8085 | Payment record creation and payment status management |
| `notification-service` | TBD | Kafka event consumption and notification processing |

The ports above are the intended local service ports. Verify the application configuration before assuming every service is running.

### 4.2 Communication patterns

**Synchronous communication — REST**

- Booking Service → Inventory Service
- Payment Service → Booking Service
- Payment Service → Booking Service status endpoint

**Asynchronous communication — Kafka**

- Booking Service → `booking-events`
- Payment Service → `payment-events`
- Notification Service consumes booking and payment events

### 4.3 High-level architecture

```text
                         Client / API Consumer
                                  |
                                  v
                         API Gateway :8080
                                  |
                 +----------------+----------------+
                 |                |                |
                 v                v                v
           User Service     Movie Service    Booking Service
              :8081            :8082             :8084
                                                    |
                                                    v
                                            Inventory Service
                                                  :8083

                                           Payment Service
                                                :8085
                                                    |
                                                    v
                                             Booking Service

                 +------------------------------------------+
                 |              Apache Kafka                |
                 |                                          |
                 |  booking-events   payment-events         |
                 +----------------------+-------------------+
                                        |
                                        v
                               Notification Service

                 Eureka Discovery Server :8761

                 MySQL + Redis + Kafka via Docker Compose
```

This is a conceptual overview. The final runtime topology and gateway routing will be verified during integration testing.

---

## 5. Project Structure

The project uses a Maven multi-module structure.

```text
distributed-movie-ticket-booking/
|
|-- pom.xml
|-- compose.yaml
|-- docker-compose.yml
|-- PROJECT_LOG.md
|
|-- contracts/
|   |-- pom.xml
|   |-- src/main/java/com/moviebooking/contracts/
|       |-- dto/
|       |   |-- ApiResponse.java
|       |-- event/
|           |-- BookingEvent.java
|           |-- PaymentEvent.java
|
|-- discovery-server/
|   |-- pom.xml
|   |-- src/main/java/
|   |-- src/main/resources/application.yml
|
|-- api-gateway/
|   |-- pom.xml
|   |-- src/main/java/
|   |-- src/main/resources/application.yml
|
|-- user-service/
|   |-- pom.xml
|   |-- src/main/java/
|   |-- src/main/resources/application.yml
|
|-- movie-service/
|   |-- pom.xml
|   |-- src/main/java/
|   |-- src/main/resources/application.yml
|
|-- inventory-service/
|   |-- pom.xml
|   |-- src/main/java/
|   |-- src/main/resources/application.yml
|
|-- booking-service/
|   |-- pom.xml
|   |-- src/main/java/
|   |   |-- com/moviebooking/booking/
|   |       |-- controller/
|   |       |-- entity/
|   |       |-- repository/
|   |       |-- service/
|   |       |-- client/
|   |       |-- event/
|   |-- src/main/resources/application.yml
|
|-- payment-service/
|   |-- pom.xml
|   |-- src/main/java/
|   |   |-- com/moviebooking/payment/
|   |       |-- controller/
|   |       |-- dto/
|   |       |-- entity/
|   |       |-- repository/
|   |       |-- service/
|   |       |-- event/
|   |-- src/main/resources/application.yml
|
|-- notification-service/
    |-- pom.xml
    |-- src/main/java/
    |-- src/main/resources/application.yml
```

The tree shows the main module layout and known files. Some package directories and files may differ from the final repository state; use the actual IntelliJ project tree as the source of truth.

---

## 6. Day 1 — Work Completed

### 6.1 Project foundation

- [x] Created the project repository and local project structure.
- [x] Established a Maven multi-module project.
- [x] Selected Spring Boot 3.5.6.
- [x] Set Java compilation target to Java 21.
- [x] Added the core microservice modules.
- [x] Established a shared `contracts` module.
- [x] Prepared the project for development in IntelliJ IDEA on Windows.

### 6.2 Shared Contracts module

Created shared classes to avoid duplicating event definitions across services.

**`ApiResponse.java`**

Provides a common response model for API operations.

**`BookingEvent.java`**

Represents a booking-related event. Its fields include:

- `bookingId`
- `userId`
- `showId`
- `eventType`
- `amount`
- `occurredAt`

**`PaymentEvent.java`**

Represents a payment-related event. Its fields include:

- `paymentId`
- `bookingId`
- `eventType`
- `amount`
- `occurredAt`

Both event classes use Java fields with getters and setters and have no-argument constructors.

### 6.3 Discovery Server

- Created the Eureka Discovery Server application.
- Configured the discovery server on port `8761`.
- Verified earlier that the discovery server started and returned HTTP 200 from its web interface.

**Purpose:** Eureka allows registered services to discover one another without relying exclusively on hard-coded host and port values.

### 6.4 API Gateway

- Created the API Gateway module.
- Prepared the gateway as the central entry point for incoming requests.

**Remaining verification:** Gateway routes, service registration, and actual request forwarding must be tested.

### 6.5 User Service

Implemented the foundation for user authentication.

- Created the User Service application.
- Added user-related API components.
- Worked on registration and login.
- Added JWT-based authentication.
- Configured the service to use a generated JWT secret from an environment variable.
- Prepared the service to use the shared response model.

**Security note:** JWT signing secrets must be generated securely and must not be committed to GitHub.

### 6.6 Movie Service

- Created the Movie Service application.
- Implemented the foundation for movie-related CRUD operations.
- Prepared the service for database-backed movie management.

**Remaining verification:** Validate movie creation, retrieval, updating, and deletion against the running database.

### 6.7 Inventory Service

Implemented the foundation for show inventory and seat reservation.

The `ShowInventory` entity contains:

- `id`
- `movieId`
- `showTime`
- `totalSeats`
- `availableSeats`
- `ticketPrice`

Added repository logic for conditional seat reservation.

The reservation operation decreases the available seat count only when enough seats remain.

Conceptually:

```sql
UPDATE show_inventory
SET available_seats = available_seats - :seats
WHERE id = :showId
  AND available_seats >= :seats;
```

The actual repository uses a Spring Data JPA update query.

**Why this matters:** A conditional database update is safer than reading the available count and updating it without an atomic condition. It helps prevent two simultaneous requests from reserving more seats than are available.

The service also validates the requested seat count and handles unsuccessful reservations.

### 6.8 Booking Service

Implemented the core booking foundation.

- Created the Booking entity.
- Added booking repository and service components.
- Added the Inventory Service client.
- Added booking creation and retrieval endpoints.
- Added booking status management.
- Integrated the booking event producer.

The Booking entity includes:

- `id`
- `userId`
- `showId`
- `numberOfSeats`
- `totalAmount`
- `status`
- `createdAt`

Booking statuses currently include:

- `PENDING`
- `CONFIRMED`
- `CANCELLED`
- `FAILED`

The booking creation workflow reserves seats, calculates the amount, saves the booking, and publishes a `BookingEvent`.

**Important limitation:** Seat reservation and booking persistence are not yet coordinated through a complete distributed transaction or compensating workflow. Failure recovery needs further work.

### 6.9 Payment Service

Implemented the payment record and status-management foundation.

- Created the Payment entity and status enum.
- Added the payment repository.
- Added payment creation and retrieval logic.
- Added communication with Booking Service through REST.
- Validated the requested payment amount against the booking total.
- Added payment status management.
- Added a Kafka payment event producer.
- Connected payment status changes with booking status updates.

The Payment entity includes:

- `id`
- `bookingId`
- `amount`
- `status`
- `createdAt`

Payment statuses currently include:

- `PENDING`
- `SUCCESS`
- `FAILED`
- `REFUNDED`

Payment creation validates that the booking exists and the submitted amount matches the booking total.

Payment status updates currently allow `SUCCESS` or `FAILED` from the pending state.

**Important:** This is a simulated payment workflow. It does not process real money through an external payment provider.

### 6.10 Kafka event publishing

Configured the event-publishing foundation using Spring Kafka.

**Booking Service**

Publishes events to:

```text
booking-events
```

**Payment Service**

Publishes events to:

```text
payment-events
```

Events use JSON serialization and a key based on the relevant booking or payment ID.

### 6.11 Notification Service

- Added the Notification Service module.
- Added Spring Kafka support.
- Configured the Kafka consumer foundation.
- Created a booking event consumer.
- Added a payment event consumer.

The consumers currently log event details to the application console.

**Remaining verification:** Confirm that both consumers deserialize their respective event types and receive messages from the running Kafka broker.

### 6.12 Docker infrastructure

Prepared the local infrastructure using Docker Compose.

The infrastructure includes:

- MySQL
- Redis
- Apache Kafka

MySQL was configured for host port `3307` to avoid a conflict with the default MySQL port on the host.

The configured database is:

```text
movie_booking
```

The configured application database user is:

```text
movieapp
```

The application database password is configured in the local environment or Compose configuration and must not be committed as a production secret.

**Previously verified:** Docker was running, MySQL responded to its health check, and Redis returned `PONG`.

**Remaining verification:** Confirm current container status and Kafka connectivity before integration testing.

### 6.13 Build verification

The latest build command was:

```powershell
mvn -pl payment-service,notification-service -am clean install -DskipTests
```

**Result: SUCCESS — confirmed by the developer.**

The build covered the selected modules and their Maven dependencies, including `contracts`.

The command skipped tests, so this result confirms successful compilation and packaging for the selected reactor modules, not complete end-to-end functionality.

---

## 7. Important Implementation Details

### 7.1 Seat reservation

The Inventory Service uses a conditional update to avoid reserving unavailable seats.

A future improvement is to add integration tests that simulate simultaneous reservation requests.

### 7.2 Booking and payment statuses

The intended flow is:

```text
Booking Created
      |
      v
Booking: PENDING
      |
      v
Payment Created
      |
      +------------------+
      |                  |
      v                  v
Payment SUCCESS     Payment FAILED
      |                  |
      v                  v
Booking CONFIRMED   Booking FAILED
```

This diagram represents the intended workflow. Error recovery and distributed consistency are not yet complete.

### 7.3 Kafka event flow

```text
Booking Service
      |
      | BookingEvent
      v
booking-events
      |
      v
Notification Service


Payment Service
      |
      | PaymentEvent
      v
payment-events
      |
      v
Notification Service
```

Kafka is used for asynchronous event delivery. It is not a replacement for REST calls that require an immediate response.

### 7.4 Maven reactor builds

The `-am` option means **also make required modules**.

For example:

```powershell
mvn -pl payment-service -am clean install -DskipTests
```

This allows Maven to build the selected service along with its required project modules.

The `-DskipTests` flag skips test execution. It should not be treated as proof that the application is correct at runtime.

---

## 8. Infrastructure & Local Environment

### 8.1 Start Docker infrastructure

Run from the project root:

```powershell
cd D:\distributed-movie-ticket-booking
docker compose up -d
docker compose ps
```

Use the Compose file selected by Docker Compose and inspect its services if any expected container is missing.

### 8.2 Verify Redis

```powershell
docker exec movie-booking-redis redis-cli ping
```

Expected response when Redis is healthy:

```text
PONG
```

### 8.3 Verify MySQL

Check the container status:

```powershell
docker compose ps
```

The MySQL container should be healthy or otherwise report a successful startup.

### 8.4 Verify Kafka

Kafka must be running and reachable at the broker address configured in the application.

The local application configuration currently uses:

```text
localhost:9092
```

Verify the actual host-port mapping in the Compose file before troubleshooting connection errors.

---

## 9. Build & Run Commands

### 9.1 Build Payment and Notification modules

```powershell
cd D:\distributed-movie-ticket-booking
mvn -pl payment-service,notification-service -am clean install -DskipTests
```

### 9.2 Build the complete project

Use this after the remaining modules are ready to compile:

```powershell
cd D:\distributed-movie-ticket-booking
mvn clean install -DskipTests
```

### 9.3 Start Eureka

Open a dedicated PowerShell terminal:

```powershell
cd D:\distributed-movie-ticket-booking
mvn -pl discovery-server spring-boot:run
```

### 9.4 Start API Gateway

Open another terminal:

```powershell
cd D:\distributed-movie-ticket-booking
mvn -pl api-gateway spring-boot:run
```

### 9.5 Start an individual service

For example, to start Booking Service:

```powershell
cd D:\distributed-movie-ticket-booking\booking-service
mvn spring-boot:run
```

Start each service in its own terminal so that its logs remain visible.

Repeat the module-directory approach for the other services after confirming their configuration and dependencies.

### 9.6 User Service JWT secret

Generate a temporary secret in the same PowerShell terminal used to start User Service:

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
mvn spring-boot:run
```

Generating a new secret invalidates tokens signed with the previous secret. Use a consistent, securely managed secret when persistence across restarts is required.

---

## 10. Current Progress

### Completed or implemented

- [x] Maven multi-module foundation
- [x] Shared API response and event contracts
- [x] Eureka application foundation
- [x] API Gateway application foundation
- [x] User authentication foundation
- [x] Movie Service foundation
- [x] Inventory entity and conditional seat reservation
- [x] Booking entity and service foundation
- [x] Booking Kafka event producer
- [x] Payment entity and service foundation
- [x] Payment Kafka event producer
- [x] Booking event consumer
- [x] Payment event consumer
- [x] Docker infrastructure setup
- [x] Payment and Notification Maven reactor build

### Not yet verified end to end

- [ ] Full-project Maven build
- [ ] All services running together
- [ ] Eureka registration of every service
- [ ] Gateway routing to each service
- [ ] Registration and login with JWT
- [ ] Movie CRUD API testing
- [ ] Seat reservation API testing
- [ ] Booking creation and amount calculation
- [ ] Booking event received by Notification Service
- [ ] Payment creation and amount validation
- [ ] Payment status update and booking status synchronization
- [ ] Payment event received by Notification Service
- [ ] Concurrent seat reservation testing
- [ ] Failure recovery and rollback behavior

---

## 11. Known Limitations & Future Improvements

### Reliability

- Implement compensation when booking creation fails after seat reservation.
- Handle Kafka publication failures after a database save.
- Consider the transactional outbox pattern for reliable event publishing.
- Prevent duplicate payment creation using database-level uniqueness and appropriate concurrency controls.
- Design consistent recovery when the payment status update succeeds but the booking status update fails.

### Security

- Secure booking and payment endpoints.
- Prevent arbitrary clients from changing booking statuses.
- Validate that the authenticated user owns the booking being accessed.
- Keep secrets outside source control.
- Add authorization rules for administrative operations.

### Distributed systems

- Configure service-to-service discovery instead of relying permanently on hard-coded URLs.
- Add sensible connection timeouts and failure handling.
- Evaluate retries and circuit breakers where appropriate.
- Introduce idempotent event processing.
- Define a clear strategy for duplicate and out-of-order events.

### Testing and observability

- Add unit tests for service logic.
- Add integration tests for persistence and REST communication.
- Test Kafka producer-consumer communication.
- Add structured logging and request correlation IDs.
- Test failure scenarios instead of only the successful path.

### Performance

- Evaluate Redis caching for appropriate read-heavy operations.
- Add database indexes for frequently queried fields.
- Measure performance before optimizing.

---

## 12. Day 2 — Planned Work

The next development session will focus on running and verifying the system rather than adding features blindly.

### Phase 1: Environment verification

- [ ] Start Docker infrastructure.
- [ ] Check MySQL, Redis, and Kafka health.
- [ ] Start Eureka and verify its dashboard.
- [ ] Build the complete Maven project.

### Phase 2: Service startup

- [ ] Start User Service.
- [ ] Start Movie Service.
- [ ] Start Inventory Service.
- [ ] Start Booking Service.
- [ ] Start Payment Service.
- [ ] Start Notification Service.
- [ ] Start API Gateway.
- [ ] Verify registration and application logs.

### Phase 3: API testing

- [ ] Register a user and test login.
- [ ] Create a movie.
- [ ] Create a show inventory record.
- [ ] Reserve seats and verify available-seat counts.
- [ ] Create a booking.
- [ ] Verify the booking event in Notification Service logs.
- [ ] Create a payment using the exact booking total.
- [ ] Update the payment to `SUCCESS` and verify the booking status.
- [ ] Confirm the payment event reaches Notification Service.
- [ ] Repeat with `FAILED` and invalid inputs.

### Phase 4: Reliability review

- [ ] Test insufficient seat availability.
- [ ] Test invalid booking IDs and mismatched payment amounts.
- [ ] Review status update authorization.
- [ ] Document any failures and fixes.
- [ ] Update this log with the actual results.

**Day 2 completion criterion:** The core booking and payment flow is exercised through the APIs, the expected events are observed, and failures are documented.

---

## 13. Development Rules

To keep the project maintainable and useful as a learning project:

1. Implement related components in dependency order.
2. Understand each class before moving on.
3. Keep shared DTOs and event contracts in the `contracts` module.
4. Build after a coherent group of changes.
5. Test behavior instead of relying only on successful compilation.
6. Do not store real credentials in Git.
7. Record important design decisions and errors in this file.
8. Update the checklist only after verifying the corresponding work.
9. Keep this log aligned with the actual repository state.
10. Commit completed, working milestones to GitHub.

---

## Daily Update Template

Copy this section at the end of the file for each new development day.

### Day N — [Session Title]

**Date:** YYYY-MM-DD

**Objective:** What the session aims to achieve.

**Work completed:**
- [ ] Item 1
- [ ] Item 2

**Files created or modified:**
- `path/to/file`

**Commands executed:**
```powershell
# Record important commands here
```

**Build and test results:**
- Build:
- Tests:
- Runtime checks:

**Issues encountered:**
- Describe the problem and its root cause.

**Solutions implemented:**
- Describe the fix.

**Key concepts learned:**
- Record important technical concepts.

**Pending tasks:**
- [ ] Next task

**Next session objective:** Describe the next milestone.

---

*End of Day 1 engineering log.*
