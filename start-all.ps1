$ErrorActionPreference = "Stop"

$Root = "D:\distributed-movie-ticket-booking"
Set-Location $Root

Write-Host "`n=== Movie Booking: Start All ===" -ForegroundColor Cyan

# 1. Start infrastructure and wait for health checks
Write-Host "`n[1/3] Starting MySQL, Redis, and Kafka..." -ForegroundColor Yellow
docker compose up -d --wait --wait-timeout 180

if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose failed. Check Docker Desktop and docker compose ps."
}

# Generate a development JWT secret inherited by child processes
if (-not $env:JWT_SECRET) {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($bytes)
    $rng.Dispose()
    $env:JWT_SECRET = [Convert]::ToBase64String($bytes)
}

function Test-ListeningPort([int]$Port) {
    return [bool](Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue |
        Select-Object -First 1)
}

function Start-ServiceWindow([string]$Name, [string]$Directory, [int]$Port) {
    if (Test-ListeningPort $Port) {
        Write-Host "$Name already has a listener on port $Port; skipping." -ForegroundColor DarkYellow
        return
    }

    $command = "Set-Location '$Directory'; Write-Host 'Starting $Name on port $Port...' -ForegroundColor Cyan; mvn spring-boot:run"

    $encoded = [Convert]::ToBase64String(
        [System.Text.Encoding]::Unicode.GetBytes($command)
    )

    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-EncodedCommand", $encoded
    ) | Out-Null

    Write-Host "Opened terminal for $Name." -ForegroundColor Green
}

# 2. Start Eureka and wait for its dashboard/API
Write-Host "`n[2/3] Starting Eureka..." -ForegroundColor Yellow
if (-not (Test-ListeningPort 8761)) {
    $eurekaCommand = "Set-Location '$Root'; Write-Host 'Starting Eureka...' -ForegroundColor Cyan; mvn -pl discovery-server spring-boot:run"
    $encoded = [Convert]::ToBase64String(
        [System.Text.Encoding]::Unicode.GetBytes($eurekaCommand)
    )

    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-ExecutionPolicy", "Bypass",
        "-EncodedCommand", $encoded
    ) | Out-Null
}

$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $response = Invoke-WebRequest "http://localhost:8761" -TimeoutSec 2
        if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
            $ready = $true
            break
        }
    } catch {}
    Start-Sleep -Seconds 2
}

if (-not $ready) {
    throw "Eureka did not become reachable. Check its terminal before starting application services."
}

# 3. Start application services
Write-Host "`n[3/3] Starting application services..." -ForegroundColor Yellow

$services = @(
    @{ Name = "User Service";         Folder = "user-service";         Port = 8081 },
    @{ Name = "Movie Service";        Folder = "movie-service";        Port = 8082 },
    @{ Name = "Inventory Service";    Folder = "inventory-service";    Port = 8083 },
    @{ Name = "Booking Service";      Folder = "booking-service";      Port = 8084 },
    @{ Name = "Payment Service";      Folder = "payment-service";      Port = 8085 },
    @{ Name = "Notification Service"; Folder = "notification-service"; Port = 8086 },
    @{ Name = "API Gateway";          Folder = "api-gateway";           Port = 8080 }
)

foreach ($service in $services) {
    Start-ServiceWindow $service.Name (Join-Path $Root $service.Folder) $service.Port
    Start-Sleep -Seconds 2
}

Write-Host "`nStartup commands sent." -ForegroundColor Green
Write-Host "Eureka:  http://localhost:8761"
Write-Host "Gateway: http://localhost:8080"
Write-Host "`nAllow the services time to finish starting. Check each terminal for Maven errors."
