@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ============================================
echo  ERP Fakultet - pokretanje celog sistema
echo ============================================
echo.

rem --- 1. Docker mora da radi ---
docker info >nul 2>&1
if not errorlevel 1 goto dockerok

echo Docker nije pokrenut, pokrecem Docker Desktop...
start "" "%ProgramFiles%\Docker\Docker\Docker Desktop.exe"
echo Cekam da Docker bude spreman...
set /a _tries=0

:waitdocker
ping -n 5 127.0.0.1 >nul
docker info >nul 2>&1
if not errorlevel 1 goto dockerok
set /a _tries+=1
if !_tries! GEQ 60 goto dockerfail
goto waitdocker

:dockerfail
echo.
echo GRESKA: Docker se nije podigao u razumnom roku.
pause
exit /b 1

:dockerok
echo Docker je spreman.
echo.

rem --- 2. Podizanje servisa; --wait ceka da svi budu ZDRAVI ---
echo Podizem bazu, Kafku i servise (prvi put traje nekoliko minuta zbog build-a)...
docker compose --profile app up -d --build --wait
if errorlevel 1 goto composefail

echo.
echo Sistem je spreman. Otvaram http://localhost:3000
echo Prijava: admin / admin123   (takodje: hr / hr1234, profesor / prof1234)
start "" http://localhost:3000
echo.
echo Zaustavljanje: stop.bat
ping -n 5 127.0.0.1 >nul
exit /b 0

:composefail
echo.
echo GRESKA: podizanje nije uspelo. Detalji: docker compose --profile app logs
pause
exit /b 1
