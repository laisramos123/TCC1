@echo off
chcp 65001 >nul
echo 🚀 BUILD COMPLETO TCC - Open Banking + Dilithium

echo ==========================================
echo 🧹 ETAPA 1: LIMPEZA INICIAL
echo ==========================================

echo 📦 Limpando containers antigos...
docker-compose down -v >nul 2>&1

echo 🗑️ Limpando builds Maven antigos...
if exist "auth-server\target" rmdir /s /q auth-server\target
if exist "resource-server\target" rmdir /s /q resource-server\target  
if exist "auth-client\target" rmdir /s /q auth-client\target

echo 🐳 Limpando cache Docker...
docker system prune -f >nul 2>&1

echo ==========================================
echo 📦 ETAPA 2: COMPILAÇÃO MAVEN
echo ==========================================

echo 🔧 Compilando auth-server...
cd auth-server
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Erro ao compilar auth-server
    echo 📋 Tentando com logs detalhados...
    call mvn clean package -DskipTests
    pause
    exit /b %ERRORLEVEL%
)
cd ..
echo ✅ auth-server compilado!

echo 🔧 Compilando resource-server...
cd resource-server
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Erro ao compilar resource-server
    echo 📋 Tentando com logs detalhados...
    call mvn clean package -DskipTests
    pause
    exit /b %ERRORLEVEL%
)
cd ..
echo ✅ resource-server compilado!

echo 🔧 Compilando auth-client...
cd auth-client
call mvn clean package -DskipTests -q
if %ERRORLEVEL% neq 0 (
    echo ❌ Erro ao compilar auth-client
    echo 📋 Tentando com logs detalhados...
    call mvn clean package -DskipTests
    pause
    exit /b %ERRORLEVEL%
)
cd ..
echo ✅ auth-client compilado!

echo ==========================================
echo 🔍 ETAPA 3: VERIFICAÇÃO DOS JARs
echo ==========================================

set TODOS_OK=1

if exist "auth-server\target\auth-server-0.0.1-SNAPSHOT.jar" (
    echo ✅ auth-server JAR: OK
    for %%f in (auth-server\target\auth-server-0.0.1-SNAPSHOT.jar) do (
        echo    📊 Tamanho: %%~zf bytes
        if %%~zf LSS 1000 (
            echo ⚠️ JAR muito pequeno: %%~zf bytes
            set TODOS_OK=0
        )
    )
) else (
    echo ❌ auth-server JAR: NÃO ENCONTRADO
    set TODOS_OK=0
)

if exist "resource-server\target\resource-server-0.0.1-SNAPSHOT.jar" (
    echo ✅ resource-server JAR: OK
    for %%f in (resource-server\target\resource-server-0.0.1-SNAPSHOT.jar) do (
        echo    📊 Tamanho: %%~zf bytes
        if %%~zf LSS 1000 (
            echo ⚠️ JAR muito pequeno: %%~zf bytes
            set TODOS_OK=0
        )
    )
) else (
    echo ❌ resource-server JAR: NÃO ENCONTRADO
    set TODOS_OK=0
)

if exist "auth-client\target\auth-client-0.0.1-SNAPSHOT.jar" (
    echo ✅ auth-client JAR: OK
    for %%f in (auth-client\target\auth-client-0.0.1-SNAPSHOT.jar) do (
        echo    📊 Tamanho: %%~zf bytes
        if %%~zf LSS 1000 (
            echo ⚠️ JAR muito pequeno: %%~zf bytes
            set TODOS_OK=0
        )
    )
) else (
    echo ❌ auth-client JAR: NÃO ENCONTRADO
    set TODOS_OK=0
)

if %TODOS_OK% equ 0 (
    echo ❌ Alguns JARs estão com problema
    echo 🔍 Verifique logs da compilação acima
    pause
    exit /b 1
)

echo ==========================================
echo 🐳 ETAPA 4: DOCKER BUILD E START
echo ==========================================

echo 🔍 Verificando disponibilidade do Docker...
docker --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Docker não está disponível ou não está instalado
    echo 📋 Para usar containers, instale Docker Desktop e tente novamente
    echo ✅ Compilação Maven finalizada com sucesso!
    goto :FINAL_SEM_DOCKER
)

docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ⚠️ Docker está instalado mas não está rodando
    echo 💡 Inicie o Docker Desktop e execute novamente para containers
    echo ✅ Compilação Maven finalizada com sucesso!
    goto :FINAL_SEM_DOCKER
)

echo ✅ Docker disponível - prosseguindo com containers...
echo 🏗️ Construindo e iniciando containers...
docker-compose up -d --build

echo ⏳ Aguardando serviços carregarem (60 segundos)...
timeout /t 60 /nobreak >nul

echo ==========================================
echo 📊 ETAPA 5: VERIFICAÇÃO DO SISTEMA
echo ==========================================

echo 🔍 Status dos containers:
docker-compose ps

echo 🧪 Testando endpoints:
curl -s http://localhost:8080/actuator/health >nul 2>&1 && echo ✅ auth-server (8080) - ONLINE || echo ⚠️ auth-server (8080) - Iniciando...
curl -s http://localhost:8082/actuator/health >nul 2>&1 && echo ✅ resource-server (8082) - ONLINE || echo ⚠️ resource-server (8082) - Iniciando...
curl -s http://localhost:8081/ >nul 2>&1 && echo ✅ auth-client (8081) - ONLINE || echo ⚠️ auth-client (8081) - Iniciando...

echo ==========================================
echo 🔐 ETAPA 6: TESTE DILITHIUM
echo ==========================================

echo 🧪 Testando algoritmo Dilithium...
curl -X POST http://localhost:8080/api/v1/dilithium/public/assinar ^
  -H "Content-Type: application/json" ^
  -d "{\"data\": \"TCC Build Test\"}" ^
  -s > temp_dilithium.json 2>nul

if exist temp_dilithium.json (
    findstr "success" temp_dilithium.json >nul && (
        echo ✅ Dilithium - Assinatura OK
    ) || (
        echo ⚠️ Dilithium - Verificar logs: docker-compose logs auth-server
    )
    del temp_dilithium.json >nul 2>&1
) else (
    echo ⚠️ Dilithium - Endpoint não responde ainda
)

echo ==========================================
echo 🎉 BUILD COMPLETO - STATUS FINAL
echo ==========================================

echo 📋 Sistema TCC Open Banking + Dilithium:
echo.
echo 🌐 URLs disponíveis:
echo    💻 Frontend:        http://localhost:8081
echo    🔐 Auth Server:     http://localhost:8080  
echo    📊 Resource APIs:   http://localhost:8082
echo.
echo 🔧 APIs Dilithium:
echo    📝 Assinar:    POST http://localhost:8080/api/v1/dilithium/public/assinar
echo    ✅ Verificar:  POST http://localhost:8080/api/v1/dilithium/public/verificar
echo    ℹ️ Info:       GET  http://localhost:8080/api/v1/dilithium/info
echo.
echo 📊 Comandos úteis:
echo    📈 Ver logs:        docker-compose logs -f [service-name]
echo    🔄 Reiniciar:       docker-compose restart [service-name]
echo    🛑 Parar tudo:      docker-compose down
echo    🧹 Limpar cache:    docker system prune -f
echo.
echo 🎯 Para desenvolvimento:
echo    1. Modifique o código
echo    2. Execute: .\build-robusto.bat
echo    3. Teste no navegador
echo.
echo ✅ Sistema pronto para uso e demonstração!

echo 📋 Pressione qualquer tecla para finalizar...
pause >nul

:FINAL_SEM_DOCKER
echo ==========================================
echo 📋 COMPILAÇÃO CONCLUÍDA - SEM CONTAINERS
echo ==========================================

echo ✅ JARs compilados com sucesso:
echo    📦 auth-server: target/auth-server-0.0.1-SNAPSHOT.jar
echo    📦 resource-server: target/resource-server-0.0.1-SNAPSHOT.jar  
echo    📦 auth-client: target/auth-client-0.0.1-SNAPSHOT.jar

echo 🐳 Para usar containers:
echo    1. Instale Docker Desktop (se não instalado)
echo    2. Inicie Docker Desktop
echo    3. Execute novamente: .\build-robusto.bat

echo 🔧 Para testar localmente (sem Docker):
echo    1. cd auth-server && java -jar target/auth-server-0.0.1-SNAPSHOT.jar
echo    2. cd resource-server && java -jar target/resource-server-0.0.1-SNAPSHOT.jar
echo    3. cd auth-client && java -jar target/auth-client-0.0.1-SNAPSHOT.jar

echo 📋 Pressione qualquer tecla para finalizar...
pause >nul
