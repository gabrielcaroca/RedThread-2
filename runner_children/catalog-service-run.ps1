param([string]$SvcPath,[string]$Mvn)
Set-Location $SvcPath
Write-Host "===== catalog-service ====="
Write-Host "[1/2] mvn -U clean install"
& $Mvn -U clean install
if ($LASTEXITCODE -ne 0) { Write-Host "BUILD FAILED code=$LASTEXITCODE"; Write-Host "Press a key..."; pause; exit 1 }
Write-Host "[2/2] mvn spring-boot:run (Ctrl+C para parar)"
& $Mvn spring-boot:run
Write-Host ""
Write-Host "Process finished for catalog-service"
Write-Host "Press a key to close..."
pause
