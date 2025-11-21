# redthread-build-run.ps1 (simple)
# abre 4 ventanas (una por servicio): build (-DskipTests -q) -> run
# la principal solo muestra progreso hasta lanzar todo

param(
  [string[]] $Services = @('identity-service','catalog-service','order-service','delivery-service')
)

$ErrorActionPreference = 'Stop'

# root y carpeta de hijos
if ($PSScriptRoot) { $ROOT = $PSScriptRoot } else { $ROOT = (Get-Location).Path }
$CHILD_DIR = Join-Path $ROOT 'runner_children'
if (-not (Test-Path $CHILD_DIR)) { New-Item -Type Directory -Path $CHILD_DIR | Out-Null }

function Resolve-Maven([string]$svcPath) {
  $mvnw = Join-Path $svcPath 'mvnw.cmd'
  if (Test-Path $mvnw) { return $mvnw }
  return 'mvn'
}

# crea script hijo para cada servicio (sin tildes, ascii, sin here-strings)
function Write-Child([string]$svcName, [string]$svcPath) {
  $mvn = Resolve-Maven $svcPath
  $childPath = Join-Path $CHILD_DIR ($svcName + '-run.ps1')

  $lines = @(
    'param([string]$SvcPath,[string]$Mvn)'
    'Set-Location $SvcPath'
    'Write-Host "===== ' + $svcName + ' ====="'
    'Write-Host "[1/2] mvn clean install -DskipTests -q"'
    '& $Mvn clean install -DskipTests -q'
    'if ($LASTEXITCODE -ne 0) { Write-Host "BUILD FAILED code=$LASTEXITCODE"; Write-Host "Press a key..."; pause; exit 1 }'
    'Write-Host "[2/2] mvn spring-boot:run (Ctrl+C para parar)"'
    '& $Mvn spring-boot:run'
    'Write-Host ""'
    'Write-Host "Process finished for ' + $svcName + '"'
    'Write-Host "Press a key to close..."'
    'pause'
  )

  ($lines -join "`r`n") | Out-File -Encoding ASCII $childPath
  return $childPath
}

# ui simple
function Show-Header {
  Write-Host ""
  Write-Host "==============================="
  Write-Host "  RedThread - Build & Run"
  Write-Host "==============================="
  Write-Host ""
}

function Progress([int]$done, [int]$total, [string]$label) {
  $width = 40
  if ($total -le 0) { $total = 1 }
  $pct = [int](($done/$total)*100)
  $filled = [int](($done/$total)*$width)
  if ($filled -gt $width) { $filled = $width }
  $bar = ('#' * $filled) + ('-' * ($width - $filled))
  Write-Host ("[{0}] {1}%  ({2}/{3})  {4}" -f $bar,$pct,$done,$total,$label)
}

# main
Show-Header
$valid = @()
foreach ($svc in $Services) {
  $p = Join-Path $ROOT $svc
  if (Test-Path $p) { $valid += ,@($svc,$p) }
  else { Write-Warning ("skip: folder not found -> {0}" -f $p) }
}
if ($valid.Count -eq 0) {
  Write-Warning "no services found. check folder names."
  Read-Host "press ENTER to exit"
  exit
}

$total = $valid.Count
$done = 0

foreach ($pair in $valid) {
  $svc = $pair[0]; $svcPath = $pair[1]
  $child = Write-Child -svcName $svc -svcPath $svcPath

  # preferir Windows Terminal si existe, si no, CMD
  $wt = (Get-Command wt.exe -ErrorAction SilentlyContinue)
  if ($wt) {
    Start-Process wt.exe -ArgumentList @(
      '-w','0','nt','-d', $svcPath,
      'powershell','-NoLogo','-NoProfile','-ExecutionPolicy','Bypass','-NoExit','-File', $child, '-SvcPath', $svcPath, '-Mvn', (Resolve-Maven $svcPath)
    ) | Out-Null
  } else {
    Start-Process cmd.exe -ArgumentList @(
      '/k', 'powershell -NoLogo -NoProfile -ExecutionPolicy Bypass -NoExit -File "' + $child + '" -SvcPath "' + $svcPath + '" -Mvn "' + (Resolve-Maven $svcPath) + '"'
    ) | Out-Null
  }

  $done++
  Progress -done $done -total $total -label ("launched " + $svc)
  Start-Sleep -Milliseconds 200
}

Write-Host ""
Write-Host "all windows launched. each one: build (skip tests, quiet) -> run."
Write-Host "you can watch output in each window. close with Ctrl+C or when finishes."
Write-Host ""
Read-Host "press ENTER to close this main window"
