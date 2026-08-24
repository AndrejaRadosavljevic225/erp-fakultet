@echo off
setlocal
cd /d "%~dp0"

rem Maven mora da radi na JDK 21. Na ovoj masini je sistemski `java` verzija 8,
rem pa skripta sama pronalazi JDK 21 umesto da se oslanja na JAVA_HOME.

set "JDK21="

rem 1. Ako je JAVA_HOME vec ispravan, koristi njega
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        "%JAVA_HOME%\bin\java.exe" -version 2>&1 | findstr /C:"version ""21" >nul && set "JDK21=%JAVA_HOME%"
    )
)

rem 2. JDK-ovi koje instalira IntelliJ
if not defined JDK21 (
    for /d %%D in ("%USERPROFILE%\.jdks\*21*") do set "JDK21=%%D"
)

rem 3. Uobicajene lokacije samostalne instalacije
if not defined JDK21 (
    for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-21*") do set "JDK21=%%D"
)
if not defined JDK21 (
    for /d %%D in ("%ProgramFiles%\Java\jdk-21*") do set "JDK21=%%D"
)

if not defined JDK21 (
    echo.
    echo GRESKA: nije pronadjen JDK 21.
    echo Instaliraj ga ili postavi JAVA_HOME na direktorijum JDK-a 21.
    echo.
    pause
    exit /b 1
)

set "JAVA_HOME=%JDK21%"
echo Koristim JDK: %JAVA_HOME%
echo.

call "%~dp0mvnw.cmd" test %*
set "REZULTAT=%ERRORLEVEL%"

echo.
if "%REZULTAT%"=="0" (
    echo Svi testovi su prosli.
) else (
    echo Testovi nisu prosli — detalji iznad.
)
pause
exit /b %REZULTAT%
