$ErrorActionPreference = "Continue"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  MOVIE BOOKING - PROJECT HEALTH CHECK" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$services = @(
    @{ Name = "Eureka";              Port = 8761; Url = "http://localhost:8761/actuator/health" },
    @{ Name = "API Gateway";         Port = 8080; Url = "http://localhost:8080/actuator/health" },
    @{ Name = "User Service";        Port = 8081; Url = "http://localhost:8081/actuator/health" },
    @{ Name = "Movie Service";       Port = 8082; Url = "http://localhost:8082/actuator/health" },
    @{ Name = "Inventory Service";   Port = 8083; Url = "http://localhost:8083/actuator/health" },
    @{ Name = "Booking Service";     Port = 8084; Url = "http://localhost:8084/actuator/health" },
    @{ Name = "Payment Service";     Port = 8085; Url = "http://localhost:8085/actuator/health" },
    @{ Name = "Notification Service";Port = 8086; Url = "http://localhost:8086/actuator/health" }
)

$results = foreach ($service in $services) {
    $portListening = [bool](Get-NetTCPConnection -State Listen -LocalPort $service.Port -ErrorAction SilentlyContinue |
        Select-Object -First 1)

    $health = "UNAVAILABLE"

    if ($portListening) {
        try {
            $response = Invoke-RestMethod -Uri $service.Url -TimeoutSec 3
            if ($response.status -eq "UP") {
                $health = "UP"
            } else {
                $health = "RESPONDED: $($response.status)"
            }
        } catch {
            $health = "PORT OPEN; HEALTH ENDPOINT UNAVAILABLE"
        }
    } else {
        $health = "NOT RUNNING"
    }

    [PSCustomObject]@{
        Service = $service.Name
        Port    = $service.Port
        Health  = $health
    }
}

$results | Format-Table -AutoSize

Write-Host "`n--- Docker Infrastructure ---" -ForegroundColor Cyan
docker compose ps

Write-Host "`n--- Gateway Movie Route ---" -ForegroundColor Cyan
try {
    $movies = Invoke-RestMethod -Uri "http://localhost:8080/api/movies" -TimeoutSec 5
    Write-Host "Gateway /api/movies responded successfully." -ForegroundColor Green
    $movies | ConvertTo-Json -Depth 5
} catch {
    Write-Host "Gateway /api/movies failed: $($_.Exception.Message)" -ForegroundColor Yellow
}
