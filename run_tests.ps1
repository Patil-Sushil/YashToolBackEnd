# ==============================================================================
# YashTools Test Suite Runner (PowerShell)
# ==============================================================================

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "                      YASHTOOLS TEST SUITE RUNNER                     " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$MavenCmd = "mvn"
if (Test-Path "./mvnw.cmd") {
    $MavenCmd = "./mvnw.cmd"
    Write-Host "[INFO] Using local Maven Wrapper (mvnw.cmd)" -ForegroundColor Green
} else {
    Write-Host "[WARN] Maven Wrapper not found. Falling back to global 'mvn'" -ForegroundColor Yellow
}

Write-Host "[INFO] Running: $MavenCmd clean test" -ForegroundColor Green
& $MavenCmd clean test
$Result = $LASTEXITCODE

Write-Host "----------------------------------------------------------------------" -ForegroundColor Cyan
if ($Result -eq 0) {
    Write-Host "[SUCCESS] All tests compiled and passed successfully!" -ForegroundColor Green
} else {
    Write-Host "[FAILURE] Test run failed. Please check the logs above." -ForegroundColor Red
}
Write-Host "======================================================================" -ForegroundColor Cyan

exit $Result
