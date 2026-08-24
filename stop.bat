@echo off
setlocal
cd /d "%~dp0"

echo Zaustavljam ERP Fakultet...
docker compose --profile app down
echo.
echo Zaustavljeno. Podaci u bazi su sacuvani (volumen postgres-data).
echo Za brisanje i podataka:  docker compose --profile app down -v
timeout /t 4 /nobreak >nul
endlocal
