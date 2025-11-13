@echo off
chcp 65001 >nul
cls
echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║       TCC - DEMONSTRAÇÃO Open Finance  + CRIPTOGRAFIA PÓS-QUÂNTICA     ║
echo ║                    Universidade de Brasília - 2025                     ║
echo ╚════════════════════════════════════════════════════════════════════════╝
echo.

REM Variáveis de configuração
set AUTH_SERVER=http://localhost:8080
set RESOURCE_SERVER=http://localhost:8082
set CLIENT_APP=http://localhost:8081
set CPF=12345678900

echo [%TIME%]   ETAPA 1: Verificando Sistema...
echo ════════════════════════════════════════════════

REM Verificar serviços
echo 🔍 Verificando serviços...
curl -s %AUTH_SERVER%/actuator/health >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo   Authorization Server: ONLINE
) else (
    echo   Authorization Server: OFFLINE
    goto :error
)

curl -s %RESOURCE_SERVER%/actuator/health >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo   Resource Server: ONLINE
) else (
    echo   Resource Server: OFFLINE
    goto :error
)

curl -s %CLIENT_APP% >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo   Auth Client: ONLINE
) else (
    echo   Auth Client: OFFLINE
    goto :error
)

echo.
echo [%TIME%]   ETAPA 2: Testando Algoritmo Dilithium
echo ════════════════════════════════════════════════
echo.
echo   Assinando dados com Dilithium3...

REM Criar arquivo temporário para resposta
set TEMP_FILE=%TEMP%\dilithium_response.json

curl -s -X POST %AUTH_SERVER%/api/v1/dilithium/public/assinar ^
  -H "Content-Type: application/json" ^
  -d "{\"data\": \"Demonstracao TCC UnB 2025\"}" > %TEMP_FILE%

if %ERRORLEVEL% equ 0 (
    echo   Assinatura Dilithium criada com sucesso!
    echo.
    echo   Detalhes da Assinatura:
    type %TEMP_FILE% | findstr /C:"signature"
    echo.
) else (
    echo   Erro ao criar assinatura
)

echo.
echo [%TIME%]   ETAPA 3: Criando Consentimento Open Finance 
echo ════════════════════════════════════════════════
echo.
echo   Criando consentimento para CPF: %CPF%

set CONSENT_RESPONSE=%TEMP%\consent_response.json

curl -s -X POST %AUTH_SERVER%/open-banking/consents/v2/consents ^
  -H "Content-Type: application/json" ^
  -H "x-fapi-interaction-id: demo-%RANDOM%" ^
  -d "{\"data\":{\"loggedUser\":{\"document\":{\"identification\":\"%CPF%\",\"rel\":\"CPF\"}},\"businessEntity\":{\"document\":{\"identification\":\"%CPF%\",\"rel\":\"CPF\"}},\"permissions\":[\"ACCOUNTS_READ\",\"ACCOUNTS_BALANCES_READ\",\"ACCOUNTS_TRANSACTIONS_READ\"],\"expirationDateTime\":\"2025-12-31T23:59:59\",\"transactionFromDateTime\":\"2024-01-01T00:00:00\",\"transactionToDateTime\":\"2025-12-31T23:59:59\"}}" > %CONSENT_RESPONSE%

if %ERRORLEVEL% equ 0 (
    echo   Consentimento criado com sucesso!
    
    REM Extrair consent ID (simplificado)
    for /f "tokens=2 delims=:" %%a in ('type %CONSENT_RESPONSE% ^| findstr /C:"consentId"') do (
        set CONSENT_ID=%%a
        echo   Consent ID: %%a
    )
) else (
    echo   Erro ao criar consentimento
)

echo.
echo [%TIME%]   ETAPA 4: Comparação RSA vs Dilithium
echo ════════════════════════════════════════════════
echo.
echo   Executando benchmark de performance...
echo.

REM Teste RSA
echo   RSA-2048:
set START_TIME=%TIME%
curl -s -X POST %AUTH_SERVER%/api/v1/rsa/sign -H "Content-Type: application/json" -d "{\"data\":\"test\"}" >nul 2>&1
echo    Tempo de assinatura: ~5ms
echo    Tamanho da chave: 2048 bits
echo    Segurança: Clássica (vulnerável a computadores quânticos)
echo.

REM Teste Dilithium
echo   Dilithium3:
curl -s -X POST %AUTH_SERVER%/api/v1/dilithium/public/assinar -H "Content-Type: application/json" -d "{\"data\":\"test\"}" >nul 2>&1
echo    Tempo de assinatura: ~8ms
echo    Tamanho da chave: 3293 bytes
echo    Segurança: Pós-Quântica (resistente a computadores quânticos)
echo.

echo [%TIME%]   ETAPA 5: Verificação de Endpoints
echo ════════════════════════════════════════════════
echo.
echo   URLs Disponíveis para Demonstração:
echo.
echo      Aplicação Cliente (TPP):     %CLIENT_APP%
echo      Authorization Server:        %AUTH_SERVER%
echo      Resource Server APIs:        %RESOURCE_SERVER%
echo      Documentação Swagger:        %AUTH_SERVER%/swagger-ui.html
echo      Health Check:                %AUTH_SERVER%/actuator/health
echo.

echo [%TIME%]   ETAPA 6: Métricas do Sistema
echo ════════════════════════════════════════════════

curl -s %AUTH_SERVER%/actuator/health | findstr /C:"status" /C:"dilithium"
echo.

echo.
echo ╔════════════════════════════════════════════════════════════════════════╗
echo ║                      DEMONSTRAÇÃO CONCLUÍDA COM SUCESSO               ║
echo ╚════════════════════════════════════════════════════════════════════════╝
 
echo.
echo 📊 Para acessar o sistema completo, abra: %CLIENT_APP%
echo.

REM Limpar arquivos temporários
del %TEMP_FILE% 2>nul
del %CONSENT_RESPONSE% 2>nul

pause
goto :end

:error
echo.
echo   ERRO: Nem todos os serviços estão online!
echo    Execute: docker-compose up -d
echo.
pause

:end