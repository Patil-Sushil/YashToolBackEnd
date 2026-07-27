# ==============================================================================
# YashTools API Integration Test Runner (PowerShell)
# Runs Newman Postman Collections and validates all Report API endpoints
# ==============================================================================

Param(
    [string]$Url = "http://localhost:8080",
    [string]$Collection = "documentation/YashTools.json",
    [switch]$AutoStart,
    [switch]$Help
)

if ($Help) {
    Write-Host "Usage: .\run_api_tests.ps1 [-Url <url>] [-Collection <path>] [-AutoStart] [-Help]" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Url         Specify the backend Base URL (default: http://localhost:8080)"
    Write-Host "  -Collection  Specify a custom Postman collection file path (default: documentation/YashTools.json)"
    Write-Host "  -AutoStart   Automatically boot the backend server if it's not already running"
    Write-Host "  -Help        Show this help message"
    Exit 0
}

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "    YashTools API Integration Test Runner        " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Target Base URL : $Url" -ForegroundColor Gray
Write-Host "Collection File : $Collection" -ForegroundColor Gray
Write-Host "Auto-Start Server: $($AutoStart.ToBool())" -ForegroundColor Gray
Write-Host "--------------------------------------------------" -ForegroundColor Gray

# Ensure collection file exists
if (-not (Test-Path $Collection)) {
    Write-Host "[ERROR] Collection file not found at: $Collection" -ForegroundColor Red
    Exit 1
}

# Check if Node.js is installed
$nodeCheck = Get-Command node -ErrorAction SilentlyContinue
$npmCheck = Get-Command npm -ErrorAction SilentlyContinue

if (-not $nodeCheck -or -not $npmCheck) {
    Write-Host "[ERROR] Node.js or NPM is not installed." -ForegroundColor Red
    Write-Host "Node.js (with npm) is required to run Newman." -ForegroundColor Yellow
    Write-Host "Please download and install it from https://nodejs.org/" -ForegroundColor Yellow
    Exit 1
}

# Check if Backend Server is Running
Write-Host "[INFO] Checking if backend server is already running..." -ForegroundColor Blue
$serverRunning = $false
try {
    $response = Invoke-RestMethod -Uri "$Url/actuator/health" -Method Get -TimeoutSec 3 -ErrorAction Stop
    if ($response.status -eq "UP" -or $response -match "UP") {
        $serverRunning = $true
    }
}
catch {
    $serverRunning = $false
}

$startedServer = $false
$serverProcess = $null

if (-not $serverRunning) {
    if ($AutoStart) {
        Write-Host "[INFO] Backend server is not running. Booting it automatically..." -ForegroundColor Blue
        $env:SPRING_PROFILES_ACTIVE = "dev"
        
        # Start backend in a new minimized cmd window to keep console output clean
        $serverProcess = Start-Process -FilePath "cmd.exe" -ArgumentList "/c .\mvnw.cmd spring-boot:run" -PassThru -WindowStyle Minimized
        $startedServer = $true
        
        Write-Host "[INFO] Server process started (PID: $($serverProcess.Id)). Waiting for health check..." -ForegroundColor Blue
        
        # Poll health endpoint for up to 60 seconds
        $maxAttempts = 45
        $attempt = 1
        $healthy = $false
        
        while ($attempt -le $maxAttempts -and -not $healthy) {
            Start-Sleep -Seconds 2
            try {
                $response = Invoke-RestMethod -Uri "$Url/actuator/health" -Method Get -TimeoutSec 2 -ErrorAction Stop
                if ($response.status -eq "UP") {
                    $healthy = $true
                }
            } catch {
                # Ignore connection errors while starting
            }
            Write-Host "[INFO] Waiting for server... (Attempt $attempt / $maxAttempts)" -ForegroundColor Gray
            $attempt++
        }
        
        if (-not $healthy) {
            Write-Host "[ERROR] Timeout waiting for backend server to become healthy." -ForegroundColor Red
            # Stop the started process tree
            taskkill /pid $serverProcess.Id /t /f | Out-Null
            Exit 1
        }
        Write-Host "[SUCCESS] Backend server is booted and active!" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] Backend server is not running." -ForegroundColor Yellow
        Write-Host "Please start the backend first, or run this script with -AutoStart switch:" -ForegroundColor Yellow
        Write-Host "  .\run_api_tests.ps1 -AutoStart" -ForegroundColor Cyan
        Write-Host ""
        $confirmation = Read-Host "Would you like to try running the tests anyway? (y/n)"
        if ($confirmation -ne "y") {
            Write-Host "[INFO] Aborted." -ForegroundColor Gray
            Exit 0
        }
    }
} else {
    Write-Host "[SUCCESS] Backend server is already running and reachable!" -ForegroundColor Green
}

# --- STAGE 1: Run Newman tests ---
Write-Host ""
Write-Host "--------------------------------------------------" -ForegroundColor Gray
Write-Host "[STAGE 1] Running Core ERP End-to-End Newman Collections..." -ForegroundColor Blue
Write-Host "--------------------------------------------------" -ForegroundColor Gray

Write-Host "[INFO] Running Core Collection: $Collection ..." -ForegroundColor Gray
$newmanCommand1 = "npx --yes newman run `"$Collection`" --env-var `"baseUrl=$Url`" --reporters cli"
Invoke-Expression $newmanCommand1
$newmanExitCode1 = $LASTEXITCODE

Write-Host ""
$reportsCollection = "documentation/YashTools_Reports_Postman_Collection.json"
Write-Host "[INFO] Running Reports Collection: $reportsCollection ..." -ForegroundColor Gray
$newmanCommand2 = "npx --yes newman run `"$reportsCollection`" --env-var `"baseUrl=$Url`" --reporters cli"
Invoke-Expression $newmanCommand2
$newmanExitCode2 = $LASTEXITCODE

$newmanExitCode = $newmanExitCode1 + $newmanExitCode2

# --- STAGE 2: Validate Report APIs ---
Write-Host ""
Write-Host "--------------------------------------------------" -ForegroundColor Gray
Write-Host "[STAGE 2] Validating Unified Report API Endpoints..." -ForegroundColor Blue
Write-Host "--------------------------------------------------" -ForegroundColor Gray

$reportFailures = 0

try {
    # 1. Login to get Auth Token
    Write-Host "[INFO] Authenticating as Admin (admin@yashtools.com)..." -ForegroundColor Gray
    $loginBody = @{
        email = "admin@yashtools.com"
        password = "Admin@123"
    } | ConvertTo-Json
    
    $loginRes = Invoke-RestMethod -Uri "$Url/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginRes.data.token
    
    if (-not $token) {
        throw "Failed to extract access token from login response"
    }
    
    $headers = @{
        Authorization = "Bearer $token"
    }
    
    # Define Report Endpoints to Test
    $reportEndpoints = @(
        # Labor Reports
        @{ path = "/api/labor-reports/summary?preset=THIS_MONTH"; type = "JSON" },
        @{ path = "/api/labor-reports/weekly?date=2026-07-27"; type = "JSON" },
        @{ path = "/api/labor-reports/monthly?date=2026-07-27"; type = "JSON" },
        @{ path = "/api/labor-reports/yearly/2026"; type = "JSON" },
        @{ path = "/api/labor-reports/export?preset=THIS_MONTH"; type = "EXCEL" },
        
        # Production Reports
        @{ path = "/api/production-reports/execution?preset=THIS_MONTH"; type = "JSON" },
        @{ path = "/api/production-reports/execution/export-excel?preset=THIS_MONTH"; type = "EXCEL" },
        @{ path = "/api/production-reports/execution/export-pdf?preset=THIS_MONTH"; type = "PDF" },
        
        @{ path = "/api/production-reports/quality?preset=THIS_MONTH"; type = "JSON" },
        @{ path = "/api/production-reports/quality/export-excel?preset=THIS_MONTH"; type = "EXCEL" },
        @{ path = "/api/production-reports/quality/export-pdf?preset=THIS_MONTH"; type = "PDF" },
        
        @{ path = "/api/production-reports/coating?preset=THIS_MONTH"; type = "JSON" },
        @{ path = "/api/production-reports/coating/export-excel?preset=THIS_MONTH"; type = "EXCEL" },
        @{ path = "/api/production-reports/coating/export-pdf?preset=THIS_MONTH"; type = "PDF" },
        
        @{ path = "/api/production-reports/efficiency?preset=THIS_MONTH"; type = "JSON" },
        @{ path = "/api/production-reports/efficiency/export-excel?preset=THIS_MONTH"; type = "EXCEL" },
        @{ path = "/api/production-reports/efficiency/export-pdf?preset=THIS_MONTH"; type = "PDF" },
        
        # P&L Reports
        @{ path = "/api/production-reports/profit-loss?preset=THIS_MONTH"; type = "JSON" },
        @{ path = "/api/production-reports/profit-loss/export-excel?preset=THIS_MONTH"; type = "EXCEL" },
        @{ path = "/api/production-reports/profit-loss/export-pdf?preset=THIS_MONTH"; type = "PDF" }
    )
    
    foreach ($endpoint in $reportEndpoints) {
        $testPath = $endpoint.path
        $expectedType = $endpoint.type
        Write-Host "Testing GET $testPath ($expectedType) ... " -NoNewline -ForegroundColor Gray
        
        try {
            $response = Invoke-WebRequest -Uri "$Url$testPath" -Method Get -Headers $headers -TimeoutSec 10
            
            if ($response.StatusCode -eq 200) {
                # Check content type / size
                $length = $response.Content.Length
                if ($length -gt 0) {
                    Write-Host "OK (Bytes: $length)" -ForegroundColor Green
                } else {
                    Write-Host "FAILED (Empty Response)" -ForegroundColor Red
                    $reportFailures++
                }
            } else {
                Write-Host "FAILED (HTTP $($response.StatusCode))" -ForegroundColor Red
                $reportFailures++
            }
        }
        catch {
            Write-Host "ERROR ($($_.Exception.Message))" -ForegroundColor Red
            $reportFailures++
        }
    }
}
catch {
    Write-Host "[ERROR] Report APIs verification aborted: $($_.Message)" -ForegroundColor Red
    $reportFailures = 99
}

# Cleanup server if we started it
if ($startedServer -and $serverProcess) {
    Write-Host ""
    Write-Host "[INFO] Shutting down automatically started backend server (PID: $($serverProcess.Id))...." -ForegroundColor Blue
    taskkill /pid $serverProcess.Id /t /f | Out-Null
}

# Overall Exit Status
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "               TEST SUMMARY                       " -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "Stage 1 (Newman Collections): " -NoNewline -ForegroundColor Gray
if ($newmanExitCode -eq 0) { Write-Host "PASSED" -ForegroundColor Green } else { Write-Host "FAILED" -ForegroundColor Red }

Write-Host "Stage 2 (Report APIs Audit): " -NoNewline -ForegroundColor Gray
if ($reportFailures -eq 0) { Write-Host "PASSED" -ForegroundColor Green } else { Write-Host "FAILED ($reportFailures errors)" -ForegroundColor Red }
Write-Host "==================================================" -ForegroundColor Cyan

if ($newmanExitCode1 -eq 0 -and $newmanExitCode2 -eq 0 -and $reportFailures -eq 0) {
    Write-Host "[SUCCESS] All project endpoints tested successfully!" -ForegroundColor Green
    Exit 0
} else {
    Write-Host "[ERROR] E2E Verification failed. Review logs above." -ForegroundColor Red
    Exit 1
}
