@echo off
chcp 65001 >nul
echo ========================================================
echo          LEARNOVA - ONLAYN TƏHSİL PLATFORMASI
echo ========================================================
echo.
echo PostgreSQL və Learnova tətbiqi Docker ilə başladılır...
docker compose up -d --build
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ========================================================
    echo [XƏTA] Docker xidməti işə düşmədi!
    echo Səbəb: Docker Desktop proqramı açıq deyil və ya başladılmayıb.
    echo.
    echo Zəhmət olmasa:
    echo 1. Kompüterdə "Docker Desktop" proqramını işə salın.
    echo 2. Docker tam açıldıqdan sonra bu faylı (start-app.bat) yenidən başladın.
    echo ========================================================
    pause
    exit /b 1
)
echo.
echo ========================================================
echo Platforma uğurla başladıldı!
echo Saytı açmaq üçün brauzerdə daxil olun:
echo    http://localhost:8080/
echo.
echo PostgreSQL Baza əlaqəsi:
echo    Host: localhost:5433  (və ya Docker daxilində postgres:5432)
echo    Baza: learnova_db
echo    İstifadəçi: postgres
echo    Şifrə: password
echo ========================================================
pause
