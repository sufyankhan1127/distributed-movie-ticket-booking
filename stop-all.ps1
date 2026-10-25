$ErrorActionPreference = "Stop"

Write-Host "`n=== Movie Booking: Stop All Application Services ===" -ForegroundColor Cyan

$expectedApplications = @{
    8080 = "com.moviebooking.gateway.ApiGatewayApplication"
    8081 = "com.moviebooking.user.UserServiceApplication"
    8082 = "com.moviebooking.movie.MovieServiceApplication"
    8083 = "com.moviebooking.inventory.InventoryServiceApplication"
    8084 = "com.moviebooking.booking.BookingServiceApplication"
    8085 = "com.moviebooking.payment.PaymentServiceApplication"
    8086 = "com.moviebooking.notification.NotificationServiceApplication"
    8761 = "com.moviebooking.discovery.DiscoveryServerApplication"
}

foreach ($port in $expectedApplications.Keys) {
    $listeners = Get-NetTCPConnection -State Listen -LocalPort $port `
        -ErrorAction SilentlyContinue

    if (-not $listeners) {
        Write-Host "Port $port is already stopped." -ForegroundColor DarkGray
        continue
    }

    foreach ($listener in $listeners) {
        $processId = $listener.OwningProcess
        $process = Get-CimInstance Win32_Process `
            -Filter "ProcessId = $processId" `
            -ErrorAction SilentlyContinue

        if (-not $process -or
            $process.Name -ne "java.exe" -or
            $process.CommandLine -notlike "*$($expectedApplications[$port])*") {

            Write-Warning "Skipping port $port; application identity did not match."
            continue
        }

        Write-Host "Stopping $($expectedApplications[$port]) (PID $processId)..." `
            -ForegroundColor Yellow

        Stop-Process -Id $processId -Force
    }
}

Write-Host "`nApplication shutdown commands completed." -ForegroundColor Green
Write-Host "MySQL, Redis, and Kafka were left running." -ForegroundColor Cyan
