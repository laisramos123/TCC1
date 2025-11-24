@echo off
chcp 65001 > nul
echo ========================================
echo 🔥 LIMPEZA COMPLETA E REBUILD FORÇADO
echo ========================================
echo.

cd /d C:\Users\laisr\IdeaProjects\TCC1

REM ============================================
REM ETAPA 1: PARAR E LIMPAR DOCKER
REM ============================================
echo [1/8] Parando containers...
docker-compose down

echo [1/8] Removendo imagens antigas...
docker rmi tcc-auth-server:latest 2>nul
docker rmi tcc-auth-client:latest 2>nul
docker rmi tcc-resource-server:latest 2>nul

echo [1/8] Limpando build cache do Docker...
docker builder prune -f

echo [1/8] Limpando volumes...
docker volume prune -f

echo ✅ Docker limpo
echo.

REM ============================================
REM ETAPA 2: LIMPAR MAVEN LOCAL REPOSITORY
REM ============================================
echo [2/8] Limpando dependências Apache HttpComponents do cache Maven...

set MAVEN_REPO=%USERPROFILE%\.m2\repository

if exist "%MAVEN_REPO%\org\apache\httpcomponents\client5" (
    echo Removendo client5...
    rmdir /s /q "%MAVEN_REPO%\org\apache\httpcomponents\client5"
)

if exist "%MAVEN_REPO%\org\apache\httpcomponents\core5" (
    echo Removendo core5...
    rmdir /s /q "%MAVEN_REPO%\org\apache\httpcomponents\core5"
)

if exist "%MAVEN_REPO%\org\apache\httpcomponents" (
    echo Removendo httpcomponents antigo (v4)...
    rmdir /s /q "%MAVEN_REPO%\org\apache\httpcomponents\httpclient"
)

echo ✅ Cache Maven limpo
echo.

REM ============================================
REM ETAPA 3: LIMPAR TARGET DO PROJETO
REM ============================================
echo [3/8] Limpando target do auth-server...
cd auth-server

if exist "target" (
    rmdir /s /q target
)

echo ✅ Target limpo
echo.

REM ============================================
REM ETAPA 4: MAVEN CLEAN
REM ============================================
echo [4/8] Maven clean...
call mvn clean

if errorlevel 1 (
    echo ❌ Erro no Maven clean
    pause
    exit /b 1
)

echo ✅ Maven clean concluído
echo.

REM ============================================
REM ETAPA 5: BAIXAR DEPENDÊNCIAS NOVAS
REM ============================================
echo [5/8] Baixando dependências (force update)...
call mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false

call mvn dependency:resolve -U

if errorlevel 1 (
    echo ❌ Erro ao baixar dependências
    pause
    exit /b 1
)

echo ✅ Dependências baixadas
echo.

REM ============================================
REM ETAPA 6: VERIFICAR DEPENDÊNCIAS
REM ============================================
echo [6/8] Verificando dependências httpclient...
call mvn dependency:tree -Dincludes=org.apache.httpcomponents.client5:httpclient5 > dependencies.txt

echo.
echo 📋 Dependências httpclient5 encontradas:
type dependencies.txt | findstr "httpclient5"
echo.

echo Pressione qualquer tecla para continuar com o build...
pause

REM ============================================
REM ETAPA 7: BUILD MAVEN
REM ============================================
echo [7/8] Executando Maven build...
call mvn clean package -DskipTests -U

if errorlevel 1 (
    echo ❌ Erro no Maven build
    pause
    exit /b 1
)

echo ✅ Maven build concluído
echo.

REM ============================================
REM ETAPA 8: VERIFICAR JAR
REM ============================================
echo [8/8] Verificando classes no JAR...

cd target

echo Extraindo lista de classes do JAR...
jar -tf auth-server-0.0.1-SNAPSHOT.jar | findstr "apache/hc" > jar-contents.txt

echo.
echo 📦 Classes Apache HttpComponents no JAR:
type jar-contents.txt | findstr "TlsSocketStrategy"

if errorlevel 1 (
    echo.
    echo ❌ ALERTA: TlsSocketStrategy NÃO encontrado no JAR!
    echo.
    echo Verificando todas as classes httpclient5:
    type jar-contents.txt
    echo.
    echo Isso indica que as dependências não foram empacotadas corretamente.
    pause
) else (
    echo ✅ TlsSocketStrategy encontrado no JAR!
)

cd ..\..

echo.
echo ========================================
echo ✅ LIMPEZA E BUILD CONCLUÍDOS
echo ========================================
echo.
echo 🚀 Agora execute o Docker build:
echo    docker-compose build --no-cache --pull auth-server
echo    docker-compose up
echo.
pause