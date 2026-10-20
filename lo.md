
## Day 3 - 21 October 2026

### Completed
- Started Docker infrastructure and all application services.
- Verified Eureka: Booking, Gateway, Payment, Inventory, Notification, User, and Movie services registered as UP.
- Verified Movie Service directly at http://localhost:8082/api/movies.
- Confirmed Eureka-advertised Movie Service IP and hostname are reachable.
- Verified Gateway health endpoint returns UP.
- Confirmed five Gateway routes are loaded: users, movies, inventory, bookings, and payments.
- Updated api-gateway/src/main/resources/application.yml to use the newer Spring Cloud Gateway WebFlux configuration property structure.
- Confirmed Gateway dependencies include Spring Cloud Gateway 4.3.0, Eureka Client, and Spring Cloud LoadBalancer.

### Remaining issue
- GET http://localhost:8080/api/movies still returns HTTP 500.
- Direct GET http://localhost:8082/api/movies succeeds and returns Interstellar and John Wick.
- Gateway routes are loaded, so the root cause is not yet identified.
- Maven reports duplicate spring-kafka dependency declarations in booking-service, payment-service, and notification-service.

### Next session: resume here
1. Check Git status and confirm today's changes.
2. Push the current project changes to GitHub.
3. Capture the actual Gateway Java exception in a log file.
4. Diagnose and fix the Gateway HTTP 500 without changing unrelated configurations.
5. Test the end-to-end booking and payment workflow through the Gateway.

### Gateway diagnostic commands
From D:\\distributed-movie-ticket-booking\\api-gateway:

    mvn spring-boot:run *> gateway-debug.log

After reproducing the failed request and stopping the Gateway with Ctrl+C:

    Select-String -Path .\\gateway-debug.log -Pattern "ERROR|Exception|Caused by|LoadBalancer|No servers available|Connection refused" -Context 3,8

### Important
- Keep the existing Docker volumes and data.
- Do not run docker compose down -v.
- Prefer fewer, combined tests.

## Day 3 — Gateway Debugging and Checkpoint

**Date:** 2026-10-21

### Completed
- Confirmed Eureka is running and all seven application services are registered as UP.
- Confirmed the Gateway health endpoint returns UP.
- Confirmed five Gateway routes load: users, movies, inventory, bookings, and payments.
- Updated Gateway configuration to use the Spring Cloud Gateway WebFlux route properties.
- Verified the Movie Service responds successfully on port 8082.
- Verified the Eureka-advertised Movie Service IP and hostname are reachable.
- Confirmed the Gateway still returns HTTP 500 for GET /api/movies.

### Current issue
The Gateway's root cause has not been identified. The route definitions load, Eureka registration is healthy, and direct Movie Service requests work.

### Next session
1. Push today's verified changes to GitHub.
2. Capture the Gateway exception in `gateway-debug.log`.
3. Diagnose and fix the HTTP 500.
4. Verify routing through the Gateway.
5. Continue end-to-end booking, payment, and Kafka event testing.

### Outstanding cleanup
- Maven reports duplicate `spring-kafka` declarations in booking-service, payment-service, and notification-service.
- Review generated logs and secrets before committing.
