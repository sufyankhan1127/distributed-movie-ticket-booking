# API Gateway Troubleshooting & Error-Fix Log

## Distributed Movie Ticket Booking System

**Repository:**
`https://github.com/sufyankhan1127/distributed-movie-ticket-booking`\
**Local project path:** `D:\distributed-movie-ticket-booking`\
**Stack:** Java, Spring Boot, Spring Cloud Gateway, Eureka Discovery,
Maven, PowerShell, Docker\
**Last verified:** 28 October 2026

> This document records the API Gateway issues encountered in this
> project, the diagnosis, the commands/configuration changes used, and a
> repeatable checklist for another developer. Commands are written for
> **Windows PowerShell**. The log records what was observed in this
> project; it is not a claim that every possible endpoint or workflow
> has been fully tested.

------------------------------------------------------------------------

## 1. Architecture and expected request path

The Gateway listens on `8080` and forwards requests to services
discovered through Eureka on `8761`.

  Component                   Port
  ------------------------- ------
  Eureka Discovery Server     8761
  API Gateway                 8080
  User Service                8081
  Movie Service               8082
  Inventory Service           8083
  Booking Service             8084
  Payment Service             8085
  Notification Service        8086

Configured Gateway routes:

  Gateway path          Eureka service ID
  --------------------- ---------------------
  `/api/users/**`       `user-service`
  `/api/movies/**`      `movie-service`
  `/api/inventory/**`   `inventory-service`
  `/api/bookings/**`    `booking-service`
  `/api/payments/**`    `payment-service`

Expected flow:

`Client → API Gateway (8080) → Eureka service lookup → target service`

The Gateway route definition can be correct while a request still fails:
the target service must be registered in Eureka with a reachable host
and port.

------------------------------------------------------------------------

## 2. Error: Gateway returned HTTP 500 for `/api/movies`

### What happened

A request to:

``` text
http://localhost:8080/api/movies
```

returned HTTP 500. The Movie Service itself responded when called
directly at:

``` text
http://localhost:8082/api/movies
```

The Gateway logs showed a hostname-resolution problem, including an
`UnknownHostException` involving:

``` text
LAPTOP-OQRSQQT3.mshome.net:8082
```

### Root cause

Eureka had registered the Movie Service with the machine hostname. The
Gateway selected the service instance from Eureka, then tried to connect
to that hostname. On this machine/network, the hostname was not
resolvable from the Gateway process, even though `localhost:8082`
worked.

This was **not initially a route-path problem**. The important clue was
that direct access to the service worked but access through the Gateway
failed.

### Diagnosis commands

Run from PowerShell:

``` powershell
# Check whether the Movie Service is reachable directly.
Invoke-WebRequest "http://localhost:8082/api/movies" -UseBasicParsing

# Check the same endpoint through the Gateway.
Invoke-WebRequest "http://localhost:8080/api/movies" -UseBasicParsing

# Inspect registered Eureka instances.
Invoke-RestMethod "http://localhost:8761/eureka/apps" |
    ConvertTo-Json -Depth 20
```

If the Eureka response is large, inspect the Movie Service registration
specifically:

``` powershell
$registry = Invoke-RestMethod "http://localhost:8761/eureka/apps"
$registry.applications.application |
    Where-Object { $_.name -eq "MOVIE-SERVICE" } |
    ConvertTo-Json -Depth 20
```

Eureka's JSON shape can vary by response/content negotiation; if this
extraction does not match your Eureka version, inspect the full response
or open the Eureka dashboard at `http://localhost:8761`.

### Fix applied

In `movie-service\src\main\resources\application.yml`, the Eureka
instance was configured to prefer its IP address:

``` yaml
eureka:
  instance:
    prefer-ip-address: true
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

**Important:** Merge these keys into the existing `eureka:` section; do
not create duplicate top-level `eureka:` keys. Keep the existing service
configuration and database settings intact.

Restart the Movie Service after changing this file. Then check Eureka
again and retry the Gateway URL.

### Verification

After the fix and restart, this request returned HTTP 200 with the
seeded movies (including *Interstellar* and *John Wick*):

``` powershell
Invoke-WebRequest "http://localhost:8080/api/movies" -UseBasicParsing
```

------------------------------------------------------------------------

## 3. Related error: Inventory, Booking, and Payment routes failed too

### What happened

At different points, Gateway requests to these endpoints returned HTTP
500:

-   `/api/inventory`
-   `/api/bookings`
-   `/api/payments`

The issue had the same general pattern: some service registrations in
Eureka still advertised the computer hostname instead of an IP address.
Movie, User, Notification, and Gateway registrations had changed to an
IP, but Inventory, Booking, and Payment initially still showed the
hostname.

### Root cause

The Eureka instance preference had not been applied consistently to all
services. Fixing only Movie Service did not correct the registrations of
the other services.

### Configuration to apply

For each of these files:

-   `inventory-service\src\main\resources\application.yml`
-   `booking-service\src\main\resources\application.yml`
-   `payment-service\src\main\resources\application.yml`

ensure the existing Eureka configuration includes:

``` yaml
eureka:
  instance:
    prefer-ip-address: true
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

As above, preserve any existing keys under `eureka.client` and merge
rather than duplicate YAML sections.

### PowerShell inspection command used

The following prints the three service configuration files so a
developer can check their existing YAML before editing:

``` powershell
cd D:\distributed-movie-ticket-booking

Get-Content ".\inventory-service\src\main\resources\application.yml"
Get-Content ".\booking-service\src\main\resources\application.yml"
Get-Content ".\payment-service\src\main\resources\application.yml"
```

### Safer automated update

The earlier edit was done with PowerShell text replacement. Text
replacement can be risky when YAML formatting differs, or when a service
already has an `instance:` section. Prefer editing each file
deliberately in IntelliJ and checking that there is only one `eureka:`
key.

If you choose to automate, **back up the files first**, inspect the
backups/diff, and do not assume a script correctly merged YAML just
because it ran:

``` powershell
cd D:\distributed-movie-ticket-booking

Copy-Item ".\inventory-service\src\main\resources\application.yml" ".\inventory-service\src\main\resources\application.yml.bak"
Copy-Item ".\booking-service\src\main\resources\application.yml" ".\booking-service\src\main\resources\application.yml.bak"
Copy-Item ".\payment-service\src\main\resources\application.yml" ".\payment-service\src\main\resources\application.yml.bak"
```

After editing, inspect the relevant section in each file. Then restart
the services so Eureka receives fresh registrations.

### Verification

After the config updates and service restart, these Gateway endpoints
all returned HTTP 200:

``` powershell
Invoke-WebRequest "http://localhost:8080/api/inventory" -UseBasicParsing
Invoke-WebRequest "http://localhost:8080/api/bookings" -UseBasicParsing
Invoke-WebRequest "http://localhost:8080/api/payments" -UseBasicParsing
```

Observed sample responses included inventory with 100 total seats / 95
available seats, booking records in `PENDING` status, and a payment
record in `PENDING` status. These were GET/list checks, not proof of a
complete booking-and-payment transaction.

------------------------------------------------------------------------

## 4. Error: Gateway request failed to connect immediately after restart

### What happened

Immediately after running the stop/start scripts, a request to the
Gateway failed because `localhost:8080` was not yet accepting
connections.

### Root cause

The services were still stopping or starting. A process restart is
asynchronous from the perspective of the next command; the port can be
temporarily unavailable while Spring Boot starts, registers with Eureka,
and initializes its dependencies.

### Correct procedure

From the project root:

``` powershell
cd D:\distributed-movie-ticket-booking

.\stop-all.ps1
.\start-all.ps1
```

Do not treat a failed request immediately after startup as proof that
the configuration is still broken. Wait for the Gateway and services to
finish starting, then check ports and health endpoints.

### Check listening ports

``` powershell
$ports = 8761,8080,8081,8082,8083,8084,8085,8086

foreach ($port in $ports) {
    $listener = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1

    if ($listener) {
        Write-Host "Port $port : LISTENING (PID $($listener.OwningProcess))"
    }
    else {
        Write-Host "Port $port : NOT LISTENING"
    }
}
```

A listening port only proves that a process has bound the port. It does
not by itself prove the service is healthy or registered in Eureka.

### Check Gateway health

``` powershell
Invoke-RestMethod "http://localhost:8080/actuator/health" |
    ConvertTo-Json -Depth 5
```

If Actuator is not exposed in your configuration, use the service
startup logs and the Eureka dashboard instead.

------------------------------------------------------------------------

## 5. Gateway routes: how to verify them instead of guessing

The project uses explicit Gateway routes with service IDs such as
`lb://movie-service`. Discovery locator was disabled, so the explicit
route configuration matters.

### Inspect the route list

In this project, the route Actuator endpoint was available after the
Gateway configuration/dependencies were corrected:

``` powershell
Invoke-RestMethod "http://localhost:8080/actuator/gateway/routes" |
    ConvertTo-Json -Depth 10
```

Check that each route has the expected path predicate and `lb://...`
URI.

If this returns **404**, do not immediately rewrite all routes. A 404
can mean the Gateway Actuator endpoint is not exposed, the Actuator
Gateway endpoint is disabled for that version/configuration, or the
running process is not the updated Gateway build. Check:

1.  `api-gateway` is the process listening on port `8080`.
2.  Actuator dependencies/configuration and exposed endpoints.
3.  Gateway startup logs.
4.  The actual configuration loaded by the running process.

A 404 from `/actuator/gateway/routes` is different from a 500 from
`/api/movies`.

------------------------------------------------------------------------

## 6. HTTP 403 from `/api/users` is a different problem

During testing, `/api/users` returned **403 Forbidden** while other
Gateway endpoints were being debugged.

A 403 usually indicates that a security rule denied the request. It is
not the same as `UnknownHostException`, service discovery failure, or a
missing Gateway route.

**Do not disable Spring Security just to make the test return 200.**
First determine whether the endpoint is intentionally protected, whether
authentication is required, and which role/authority is expected. Test
with a valid token/credentials if the application requires them, and
inspect the User Service security configuration and logs.

A successful HTTP response from a public GET route does not prove that a
protected user route should be public.

------------------------------------------------------------------------

## 7. Repeatable diagnostic checklist

Run these checks in order. The order helps isolate which layer is
failing.

### Step 1 --- Confirm the project root

``` powershell
cd D:\distributed-movie-ticket-booking
Get-Location
```

### Step 2 --- Check ports

Use the port-checking script in section 4. Confirm the relevant service
is listening.

### Step 3 --- Test the target service directly

For Movie Service:

``` powershell
Invoke-WebRequest "http://localhost:8082/api/movies" -UseBasicParsing
```

Repeat with the appropriate direct port/path for other services. If
direct access fails, investigate that service before blaming Gateway
routing.

### Step 4 --- Check Eureka

Open `http://localhost:8761` in a browser, or inspect the registry
using:

``` powershell
Invoke-RestMethod "http://localhost:8761/eureka/apps" |
    ConvertTo-Json -Depth 20
```

Verify the service is `UP`, the service ID matches the route URI
(case-insensitive in Eureka), and its advertised host/port is reachable
from the Gateway process.

### Step 5 --- Check the Gateway route table

``` powershell
Invoke-RestMethod "http://localhost:8080/actuator/gateway/routes" |
    ConvertTo-Json -Depth 10
```

Confirm path predicate and `lb://service-id`.

### Step 6 --- Test through the Gateway

``` powershell
$paths = @(
    "/api/movies",
    "/api/inventory",
    "/api/bookings",
    "/api/payments"
)

foreach ($path in $paths) {
    $url = "http://localhost:8080$path"
    try {
        $response = Invoke-WebRequest $url -UseBasicParsing -TimeoutSec 15
        Write-Host "$path -> HTTP $([int]$response.StatusCode)"
    }
    catch {
        if ($_.Exception.Response) {
            Write-Host "$path -> HTTP $([int]$_.Exception.Response.StatusCode)"
        }
        else {
            Write-Host "$path -> CONNECTION/REQUEST ERROR: $($_.Exception.Message)"
        }
    }
}
```

This is a quick smoke test for the four list endpoints that were
verified in this project. It does not replace tests for
create/update/delete, authentication, Kafka, or the full booking
workflow.

### Step 7 --- Interpret the result

  -----------------------------------------------------------------------
  Symptom                             First thing to inspect
  ----------------------------------- -----------------------------------
  Connection refused / cannot connect Is the relevant process listening?
                                      Has startup completed?

  Gateway 500 with                    Eureka's advertised hostname/IP and
  `UnknownHostException`              Gateway logs

  Gateway 503                         Service registration/availability,
                                      reachable instance, and
                                      load-balancer logs

  Gateway 404                         Route path, context path, route
                                      predicates, and whether the
                                      requested route exists

  403 Forbidden                       Authentication/authorization rules

  401 Unauthorized                    Missing/invalid credentials or
                                      token

  Direct service succeeds but Gateway Eureka registration, route URI,
  fails                               Gateway networking, and Gateway
                                      logs

  Direct service and Gateway both     Target service, its dependencies,
  fail                                configuration, and logs
  -----------------------------------------------------------------------

These are diagnostic starting points, not absolute rules; use the
exception and logs to confirm the cause.

------------------------------------------------------------------------

## 8. Startup and shutdown notes

Use the repository scripts rather than manually starting multiple Java
processes, if those scripts are present and configured correctly:

``` powershell
cd D:\distributed-movie-ticket-booking
.\start-all.ps1
```

Stop the services with:

``` powershell
cd D:\distributed-movie-ticket-booking
.\stop-all.ps1
```

If PowerShell reports that a script cannot run because of execution
policy, inspect the exact error and follow your machine/team policy
rather than changing global execution policy blindly.

After changing `application.yml`, restart the affected service(s).
Existing processes do not automatically pick up ordinary YAML changes
unless the application is configured for refresh.

------------------------------------------------------------------------

## 9. Before committing configuration fixes

Review exactly what changed:

``` powershell
cd D:\distributed-movie-ticket-booking

git status --short
git diff --check
git diff
```

Check that: - The intended service YAML files contain
`eureka.instance.prefer-ip-address: true`. - Each YAML file has no
duplicate top-level `eureka:` key. - Existing
`eureka.client.service-url.defaultZone` values remain correct for the
environment. - No secrets, local-only files, logs, or backup `.bak`
files are accidentally committed.

If the diff is correct, stage only the intended files, commit with a
clear message, and push:

``` powershell
git add movie-service/src/main/resources/application.yml inventory-service/src/main/resources/application.yml booking-service/src/main/resources/application.yml payment-service/src/main/resources/application.yml
git diff --cached
git commit -m "Fix Eureka service instance registration"
git push origin main
```

Only run the commit/push commands after reviewing the staged diff and
confirming these are the intended changes. If other files changed, do
not stage everything blindly.

------------------------------------------------------------------------

## 10. Prevention checklist for future developers

-   [ ] Keep Gateway route service IDs aligned with Eureka
    application/service IDs.
-   [ ] Use `lb://service-id` for load-balanced routes.
-   [ ] Ensure every service registers a hostname/IP that the Gateway
    can actually reach.
-   [ ] If the machine hostname is not resolvable in the local
    environment, consider `eureka.instance.prefer-ip-address: true`
    consistently across services.
-   [ ] Keep exactly one top-level `eureka:` block per YAML file.
-   [ ] Restart services after configuration changes.
-   [ ] Check direct service access before testing the same path through
    Gateway.
-   [ ] Check Eureka registration before changing Gateway route
    definitions.
-   [ ] Distinguish 401/403 security responses from routing/discovery
    errors.
-   [ ] Do not diagnose a startup-time connection refusal until startup
    has had time to complete.
-   [ ] Use Gateway logs and the actual exception as the source of
    truth; do not infer the cause from the HTTP status alone.
-   [ ] Run smoke tests after restart and record which endpoints were
    actually tested.

------------------------------------------------------------------------

## 11. Known-good verification recorded on 28 October 2026

After setting the Eureka instance preference and restarting the
services, these Gateway GET endpoints returned HTTP 200:

-   `GET http://localhost:8080/api/movies`
-   `GET http://localhost:8080/api/inventory`
-   `GET http://localhost:8080/api/bookings`
-   `GET http://localhost:8080/api/payments`

The application ports `8761` and `8080`--`8086` were also reported as
listening at one point.

**Not yet established by these checks:** successful user authentication,
full booking/payment integration, Kafka notification delivery after
restart, or complete automated test coverage. Those require separate
verification.

------------------------------------------------------------------------

## 12. Quick commands reference

``` powershell
# Move to repository
cd D:\distributed-movie-ticket-booking

# Eureka registry
Invoke-RestMethod "http://localhost:8761/eureka/apps" | ConvertTo-Json -Depth 20

# Gateway health
Invoke-RestMethod "http://localhost:8080/actuator/health" | ConvertTo-Json -Depth 5

# Gateway routes
Invoke-RestMethod "http://localhost:8080/actuator/gateway/routes" | ConvertTo-Json -Depth 10

# Direct Movie Service
Invoke-WebRequest "http://localhost:8082/api/movies" -UseBasicParsing

# Through Gateway
Invoke-WebRequest "http://localhost:8080/api/movies" -UseBasicParsing

# Review repository changes
git status --short
git diff --check
git diff
```
