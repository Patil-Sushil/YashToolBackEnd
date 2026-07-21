# ==============================================================================
# YashTools API Integration Test Runner (PowerShell)
# Runs the comprehensive Postman collection using Newman
# ==============================================================================

param(
    [string]$Url = "http://localhost:8080",
    [string]$Collection = "documentation/YashTools.json",
    [switch]$StartServer,
    [switch]$Help
)

if ($Help) {
    Write-Host "Usage: .\run_api_tests.ps1 [options]"
    Write-Host ""
    Write-Host "Options:"
    Write-Host "  -Url <url>           Specify the backend Base URL (default: http://localhost:8080)"
    Write-Host "  -Collection <path>   Specify a custom Postman collection file path"
    Write-Host "  -StartServer         Auto-start Spring Boot backend if server is not reachable"
    Write-Host "  -Help                Show this help message"
    exit 0
}

# Handle command line arguments passed without param syntax
for ($i = 0; $i -lt $args.Count; $i++) {
    if ($args[$i] -eq "-u" -or $args[$i] -eq "--url") {
        $Url = $args[++$i]
    } elseif ($args[$i] -eq "-c" -or $args[$i] -eq "--collection") {
        $Collection = $args[++$i]
    } elseif ($args[$i] -eq "-s" -or $args[$i] -eq "--start-server") {
        $StartServer = $true
    } elseif ($args[$i] -eq "-h" -or $args[$i] -eq "--help") {
        Write-Host "Usage: .\run_api_tests.ps1 [-Url <url>] [-Collection <path>] [-StartServer]"
        exit 0
    }
}

$baseUrl = $Url
$collectionPath = $Collection

# Ensure collection file exists
if (-not (Test-Path $collectionPath)) {
    Write-Error "Collection file not found at: $collectionPath"
    exit 1
}

Write-Host "YashTools Integration Test Runner" -ForegroundColor Blue
Write-Host "---------------------------------" -ForegroundColor Blue
Write-Host "Target Base URL: $baseUrl" -ForegroundColor Gray
Write-Host "Collection File: $collectionPath" -ForegroundColor Gray
Write-Host "---------------------------------" -ForegroundColor Blue

# Check if Node.js & npm are installed
$nodeCheck = Get-Command node -ErrorAction SilentlyContinue
if (-not $nodeCheck) {
    Write-Error "Node.js is not installed. Node.js (with npm) is required to run Newman."
    Write-Warning "Please install Node.js from https://nodejs.org/"
    exit 1
}

$npmCheck = Get-Command npm -ErrorAction SilentlyContinue
if (-not $npmCheck) {
    Write-Error "npm is not installed."
    exit 1
}

# Pre-flight check: Is backend running?
Function Test-ServerConnection([string]$testUrl) {
    try {
        $uri = [System.Uri]$testUrl
        $tcp = New-Object System.Net.Sockets.TcpClient
        $connection = $tcp.BeginConnect($uri.Host, $uri.Port, $null, $null)
        $wait = $connection.AsyncWaitHandle.WaitOne(2000, $false)
        if ($wait) {
            $tcp.EndConnect($connection)
            $tcp.Close()
            return $true
        }
        $tcp.Close()
        return $false
    } catch {
        return $false
    }
}

$serverStartedByScript = $false
$serverProcess = $null

if (-not (Test-ServerConnection $baseUrl)) {
    if ($StartServer) {
        Write-Host "Backend server is not running on $baseUrl. Attempting to start Spring Boot application..." -ForegroundColor Yellow
        $serverProcess = Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -PassThru -NoNewWindow
        $serverStartedByScript = $true

        Write-Host "Waiting for backend server to start up on $baseUrl..." -ForegroundColor Yellow
        $attempts = 0
        $maxAttempts = 60
        while (-not (Test-ServerConnection $baseUrl) -and ($attempts -lt $maxAttempts)) {
            Start-Sleep -Seconds 2
            $attempts++
            Write-Host "." -NoNewline -ForegroundColor Gray
        }
        Write-Host ""

        if (-not (Test-ServerConnection $baseUrl)) {
            Write-Error "Failed to start backend server within timeout period."
            if ($serverProcess -and -not $serverProcess.HasExited) {
                Stop-Process -Id $serverProcess.Id -Force
            }
            exit 1
        }
        Write-Host "Backend server started successfully!" -ForegroundColor Green
    } else {
        Write-Error "Backend server is NOT reachable at $baseUrl."
        Write-Host "Please start your Spring Boot application first (e.g., using '.\mvnw.cmd spring-boot:run')," -ForegroundColor Yellow
        Write-Host "or re-run this script with the -StartServer switch:" -ForegroundColor Yellow
        Write-Host "    .\run_api_tests.ps1 -StartServer" -ForegroundColor Cyan
        exit 1
    }
}

# Run Newman via npx
Write-Host "Running tests using Newman via npx..." -ForegroundColor Blue
$exitCode = 0
try {
    npx --yes newman run $collectionPath --env-var "baseUrl=$baseUrl" --reporters cli
    $exitCode = $LASTEXITCODE
} finally {
    if ($serverStartedByScript -and $serverProcess -and -not $serverProcess.HasExited) {
        Write-Host "Stopping background Spring Boot server..." -ForegroundColor Yellow
        Stop-Process -Id $serverProcess.Id -Force -ErrorAction SilentlyContinue
    }
}

if ($exitCode -eq 0) {
    Write-Host "All API endpoints tested successfully!" -ForegroundColor Green
    exit 0
} else {
    Write-Error "Some API endpoint tests failed. Check the Newman output above."
    exit $exitCode
}

