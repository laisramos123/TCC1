@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================
REM BUILD ROBUSTO - TCC OPEN FINANCE + DILITHIUM
REM Autor: Lais Rocha
REM Versão: 2.0
REM ============================================

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║         BUILD ROBUSTO - PROJETO TCC                        ║
echo ║  Implementação de Criptografia Pós-Quântica               ║
echo ║           Open Finance Brasil + Dilithium3                 ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM Verificar se está no diretório correto
if not exist "auth-server" (
    echo [ERRO] Diretório auth-server não encontrado!
    echo Execute este script no diretório raiz do projeto TCC1
    pause
    exit /b 1
)

if not exist "resource-server" (
    echo [ERRO] Diretório resource-server não encontrado!
    pause
    exit /b 1
)

if not exist "auth-client" (
    echo [ERRO] Diretório auth-client não encontrado!
    pause
    exit /b 1
)

echo [INFO] Diretório do projeto verificado com sucesso!
echo.

REM ============================================
REM ETAPA 1: PARAR CONTAINERS EXISTENTES
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 🛑 ETAPA 1: PARANDO CONTAINERS EXISTENTES
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

docker-compose down
if errorlevel 1 (
    echo [AVISO] Não foi possível parar os containers ou nenhum container em execução
) else (
    echo [OK] Containers parados com sucesso!
)
echo.

REM ============================================
REM ETAPA 2: LIMPEZA DE ARTEFATOS ANTIGOS
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 🧹 ETAPA 2: LIMPEZA DE ARTEFATOS ANTIGOS
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

echo [INFO] Limpando Auth Server...
cd auth-server
if exist "target" (
    rmdir /s /q target
    echo [OK] Target do Auth Server removido
)
cd ..

echo [INFO] Limpando Resource Server...
cd resource-server
if exist "target" (
    rmdir /s /q target
    echo [OK] Target do Resource Server removido
)
cd ..

echo [INFO] Limpando Auth Client...
cd auth-client
if exist "target" (
    rmdir /s /q target
    echo [OK] Target do Auth Client removido
)
cd ..

echo.

REM ============================================
REM ETAPA 3: BUILD MAVEN - AUTH SERVER
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 📦 ETAPA 3: BUILD MAVEN - AUTH SERVER
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

cd auth-server
echo [INFO] Compilando Auth Server (OAuth2 + Dilithium3)...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERRO] Falha no build do Auth Server!
    cd ..
    pause
    exit /b 1
)
echo [OK] Auth Server compilado com sucesso!
cd ..
echo.

REM ============================================
REM ETAPA 4: BUILD MAVEN - RESOURCE SERVER
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 📦 ETAPA 4: BUILD MAVEN - RESOURCE SERVER
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

cd resource-server
echo [INFO] Compilando Resource Server (Open Banking API)...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERRO] Falha no build do Resource Server!
    cd ..
    pause
    exit /b 1
)
echo [OK] Resource Server compilado com sucesso!
cd ..
echo.

REM ============================================
REM ETAPA 5: BUILD MAVEN - AUTH CLIENT
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 📦 ETAPA 5: BUILD MAVEN - AUTH CLIENT
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

cd auth-client
echo [INFO] Compilando Auth Client (TPP Frontend)...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERRO] Falha no build do Auth Client!
    cd ..
    pause
    exit /b 1
)
echo [OK] Auth Client compilado com sucesso!
cd ..
echo.

REM ============================================
REM ETAPA 6: BUILD DOCKER IMAGES
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 🐳 ETAPA 6: BUILD DOCKER IMAGES
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

echo [INFO] Construindo imagens Docker...
docker-compose build --no-cache
if errorlevel 1 (
    echo [ERRO] Falha no build das imagens Docker!
    pause
    exit /b 1
)
echo [OK] Imagens Docker construídas com sucesso!
echo.

REM ============================================
REM ETAPA 7: SUBIR CONTAINERS
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 🚀 ETAPA 7: SUBINDO CONTAINERS
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

echo [INFO] Iniciando containers em modo detached...
docker-compose up -d
if errorlevel 1 (
    echo [ERRO] Falha ao iniciar containers!
    pause
    exit /b 1
)
echo [OK] Containers iniciados com sucesso!
echo.

REM ============================================
REM ETAPA 8: AGUARDAR INICIALIZAÇÃO
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo ⏳ ETAPA 8: AGUARDANDO INICIALIZAÇÃO DOS SERVIÇOS
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

echo [INFO] Aguardando PostgreSQL (30s)...
timeout /t 30 /nobreak >nul

echo [INFO] Aguardando Redis (10s)...
timeout /t 10 /nobreak >nul

echo [INFO] Aguardando Auth Server (60s)...
timeout /t 60 /nobreak >nul

echo [INFO] Aguardando Resource Server (30s)...
timeout /t 30 /nobreak >nul

echo [INFO] Aguardando Auth Client (20s)...
timeout /t 20 /nobreak >nul

echo.

REM ============================================
REM ETAPA 9: VERIFICAR STATUS DOS CONTAINERS
REM ============================================

echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo 🔍 ETAPA 9: VERIFICANDO STATUS DOS CONTAINERS
echo ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
echo.

docker-compose ps
echo.

REM ============================================
REM RELATÓRIO FINAL
REM ============================================

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║                   BUILD CONCLUÍDO!                         ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
echo ✅ Todos os microsserviços foram compilados e iniciados!
echo.
echo 🌐 URLs DISPONÍVEIS:
echo.
echo    📘 Auth Server (OAuth2 + Dilithium3):
echo       http://localhost:8080
echo       http://localhost:8080/actuator/health
echo.
echo    📗 Resource Server (Open Banking API):
echo       http://localhost:8082
echo       http://localhost:8082/actuator/health
echo.
echo    📙 Auth Client (TPP Frontend):
echo       http://localhost:8081
echo       http://localhost:8081/actuator/health
echo.
echo    📊 Monitoring:
echo       Prometheus: http://localhost:9090
echo       Grafana:    http://localhost:3000 (admin/admin123)
echo.
echo    🗄️ Database:
echo       PostgreSQL: localhost:5432
echo       PgAdmin:    http://localhost:5050
echo       Redis:      localhost:6379
echo.
echo 📋 PRÓXIMOS PASSOS:
echo.
echo    1. Verificar logs: docker-compose logs -f [serviço]
echo    2. Testar OAuth2 Flow: http://localhost:8081
echo    3. Executar testes JMeter: .\run-jmeter-tests.ps1
echo    4. Monitorar métricas: http://localhost:3000
echo.
echo 🔥 Para parar os containers: docker-compose down
echo.

REM Perguntar se quer ver os logs
set /p SHOW_LOGS="Deseja ver os logs dos serviços? (S/N): "
if /i "!SHOW_LOGS!"=="S" (
    echo.
    echo [INFO] Mostrando logs dos últimos 50 eventos...
    docker-compose logs --tail=50
)

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║              BUILD ROBUSTO FINALIZADO!                     ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

pause