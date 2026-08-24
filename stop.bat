@echo off
setlocal
cd /d "%~dp0"

echo Zaustavljam ERP Fakultet...
docker compose --profile app down
echo.
echo Zaustavljeno. Podaci u bazi su sacuvani (volumen postgres-data).
echo Za brisanje i podataka:  docker compose --profile app down -v
ping -n 4 127.0.0.1 >nul
endlocal
