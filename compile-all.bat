@echo off
chcp 65001 >nul
echo 🚀 TCC Open Banking + Dilithium - Compilação e Execução

echo ====================================
echo 🧹 Limpeza inicial
echo ====================================
echo 🗑️ Limpando builds antigos...
if exist "auth-server\target" rmdir /s /q auth-server\target
if exist "resource-server\target" rmdir /s /q resource-server\target
if exist "auth-client\target" rmdir /s /q auth-client\target

echo 🐳 Limpando Docker completamente...
docker-compose down -v >nul 2>&1
docker builder prune -a -f >nul 2>&1
docker system prune -a -f >nul 2>&1
echo ✅ Cache Docker limpo!

echo ====================================
echo 📦 Compilando Authorization Server
echo ====================================
cd auth-server
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ❌ ERRO ao compilar auth-server
    pause
    exit /b 1
)
echo ✅ auth-server compilado!
cd ..

echo ====================================
echo 📦 Compilando Resource Server
echo ====================================
cd resource-server
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ❌ ERRO ao compilar resource-server
    pause
    exit /b 1
)
echo ✅ resource-server compilado!
cd ..

echo ====================================
echo 📦 Compilando Auth Client
echo ====================================
cd auth-client
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ❌ ERRO ao compilar auth-client
    pause
    exit /b 1
)
echo ✅ auth-client compilado!
cd ..

echo ====================================
echo 🔍 Verificando JARs criados
echo ====================================

set TODOS_OK=1

if exist "auth-server\target\auth-server-0.0.1-SNAPSHOT.jar" (
    echo ✅ auth-server JAR: OK
    for %%f in (auth-server\target\auth-server-0.0.1-SNAPSHOT.jar) do (
        echo    📊 Tamanho: %%~zf bytes
    )
) else (
    echo ❌ auth-server JAR: NÃO ENCONTRADO
    set TODOS_OK=0
)

if exist "resource-server\target\resource-server-0.0.1-SNAPSHOT.jar" (
    echo ✅ resource-server JAR: OK
    for %%f in (resource-server\target\resource-server-0.0.1-SNAPSHOT.jar) do (
        echo    📊 Tamanho: %%~zf bytes
    )
) else (
    echo ❌ resource-server JAR: NÃO ENCONTRADO
    set TODOS_OK=0
)

if exist "auth-client\target\auth-client-0.0.1-SNAPSHOT.jar" (
    echo ✅ auth-client JAR: OK
    for %%f in (auth-client\target\auth-client-0.0.1-SNAPSHOT.jar) do (
        echo    📊 Tamanho: %%~zf bytes
    )
) else (
    echo ❌ auth-client JAR: NÃO ENCONTRADO
    set TODOS_OK=0
)

if %TODOS_OK% equ 0 (
    echo ❌ Alguns JARs falharam na compilação
    pause
    exit /b 1
)

echo ====================================
echo 🐳 Iniciando com Docker (Build Limpo)
echo ====================================

echo 🏗️ Verificando Docker...
docker --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ⚠️ Docker não disponível - execute manualmente:
    echo    java -jar auth-server\target\auth-server-0.0.1-SNAPSHOT.jar
    echo    java -jar resource-server\target\resource-server-0.0.1-SNAPSHOT.jar
    echo    java -jar auth-client\target\auth-client-0.0.1-SNAPSHOT.jar
    pause
    exit /b 0
)

echo 🏗️ Build completo Docker (sem cache)...
docker-compose build --no-cache --pull

echo 🚀 Iniciando containers...
docker-compose up -d

echo ⏳ Aguardando serviços carregarem (60 segundos)...
timeout /t 60 /nobreak >nul

echo ====================================
echo 🧪 Testando Sistema
echo ====================================

echo 📊 Status dos containers:
docker-compose ps

echo 🔍 Testando endpoints:
curl -s http://localhost:8080/actuator/health >nul 2>&1 && echo ✅ auth-server (8080) - ONLINE || echo ⚠️ auth-server (8080) - Verificar logs
curl -s http://localhost:8082/actuator/health >nul 2>&1 && echo ✅ resource-server (8082) - ONLINE || echo ⚠️ resource-server (8082) - Verificar logs  
curl -s http://localhost:8081/ >nul 2>&1 && echo ✅ auth-client (8081) - ONLINE || echo ⚠️ auth-client (8081) - Verificar logs

echo ====================================
echo ✅ SISTEMA TCC EXECUTANDO!
echo ====================================

echo 🌐 URLs disponíveis:
echo    💻 Frontend:      http://localhost:8081
echo    🔐 Auth Server:   http://localhost:8080
echo    📊 Resource API:  http://localhost:8082

echo 🔧 Comandos úteis:
echo    📈 Ver logs:      docker-compose logs -f [service-name]
echo    🔄 Reiniciar:     docker-compose restart [service-name]
echo    🛑 Parar tudo:    docker-compose down

echo 💡 Se algum serviço não responder, execute:
echo    docker-compose logs [service-name]

echo ✅ Sistema pronto para uso e demonstração!
pause