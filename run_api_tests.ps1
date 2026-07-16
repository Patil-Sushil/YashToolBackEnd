# ==============================================================================
# YashTools API Integration Test Runner (PowerShell)
# Runs the comprehensive Postman collection using Newman
# ==============================================================================

$baseUrl = "http://localhost:8080"
$collectionPath = "documentation/YashTools_Comprehensive_Postman_Collection.json"

# Parse arguments
for ($i = 0; $i -lt $args.Count; $i++) {
    if ($args[$i] -eq "-u" -or $args[$i] -eq "--url") {
        $baseUrl = $args[++$i]
    } elseif ($args[$i] -eq "-c" -or $args[$i] -eq "--collection") {
        $collectionPath = $args[++$i]
    } elseif ($args[$i] -eq "-h" -or $args[$i] -eq "--help") {
        Write-Host "Usage: .\run_api_tests.ps1 [options]"
        Write-Host ""
        Write-Host "Options:"
        Write-Host "  -u, --url <url>       Specify the backend Base URL (default: http://localhost:8080)"
        Write-Host "  -c, --collection <p>  Specify a custom Postman collection file path"
        Write-Host "  -h, --help            Show this help message"
        exit 0
    } else {
        Write-Error "Unknown parameter: $($args[$i])"
        exit 1
    }
}

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

# Run Newman via npx
Write-Host "Checking/Running tests using Newman via npx..." -ForegroundColor Blue
npx --yes newman run $collectionPath --env-var "baseUrl=$baseUrl" --reporters cli

if ($LASTEXITCODE -eq 0) {
    Write-Host "All API endpoints tested successfully!" -ForegroundColor Green
    exit 0
} else {
    Write-Error "Some API endpoint tests failed. Check the Newman output above."
    exit $LASTEXITCODE
}
