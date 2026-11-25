@echo off
set JAVA_HOME=C:\Users\Administrator.DESKTOP-N0QCVKS\.jdks\temurin-17
echo Step 1: Stopping Gradle daemons...
call "e:\GTL\GTLSUPBJAVA\gradlew.bat" -p "e:\GTL\GTLSUPBJAVA" --stop
echo.
echo Step 2: Running spotlessApply build...
call "e:\GTL\GTLSUPBJAVA\gradlew.bat" -p "e:\GTL\GTLSUPBJAVA" spotlessApply build
echo.
if %ERRORLEVEL% equ 0 (
    echo ============================================
    echo   BUILD SUCCESSFUL
    echo   Output: e:\GTL\GTLSUPBJAVA\build\libs\
    echo ============================================
) else (
    echo ============================================
    echo   BUILD FAILED Error code: %ERRORLEVEL%
    echo ============================================
)
pause
