@echo off
setlocal
cd /d "%~dp0"
REM Mantener la ventana abierta con -NoExit y permitir script con ExecutionPolicy Bypass
powershell -NoLogo -NoProfile -ExecutionPolicy Bypass -NoExit -File ".\redthread-build-run.ps1"
