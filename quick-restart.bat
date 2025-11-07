@echo off
chcp 65001 >nul
echo     Restart  

echo   Reiniciando containers...
docker-compose restart

echo   Aguardando 20 segundos...
timeout /t 20 /nobreak >nul

echo   Testando...
curl -s http://localhost:8081/ >nul 2>&1 && echo ✅ ONLINE || echo ⚠️ Carregando

echo   http://localhost:8081
echo   Restart em ~30 segundos!