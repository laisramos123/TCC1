@echo off
setlocal enabledelayedexpansion

echo ========================================
echo    GERANDO CERTIFICADOS mTLS - WINDOWS
echo ========================================

:: Verificar OpenSSL
where openssl >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERRO] OpenSSL nao encontrado!
    echo Por favor, instale o OpenSSL:
    echo 1. Baixe de: https://slproweb.com/products/Win32OpenSSL.html
    echo 2. Adicione ao PATH do sistema
    echo.
    pause
    exit /b 1
)

echo [OK] OpenSSL encontrado

:: Verificar keytool
where keytool >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo [ERRO] Keytool nao encontrado!
    echo Certifique-se de que o Java esta instalado e no PATH
    echo.
    pause
    exit /b 1
)

echo [OK] Keytool encontrado

:: Criar diretorios
echo.
echo Criando diretorios...

if not exist "auth-server\src\main\resources\certificates" (
    mkdir "auth-server\src\main\resources\certificates"
)
if not exist "resource-server\src\main\resources\certificates" (
    mkdir "resource-server\src\main\resources\certificates"
)
if not exist "auth-client\src\main\resources\certificates" (
    mkdir "auth-client\src\main\resources\certificates"
)
if not exist "temp-certs" (
    mkdir "temp-certs"
)

cd temp-certs

:: Gerar CA
echo.
echo Gerando Certificate Authority...
openssl req -x509 -newkey rsa:4096 -keyout ca-key.pem -out ca-cert.pem -days 365 -nodes -subj "/C=BR/ST=DF/L=Brasilia/O=TCC-UnB/CN=OpenFinance-CA"

:: Gerar certificado auth-server
echo.
echo Criando certificado auth-server...
openssl genrsa -out auth-server-key.pem 2048
openssl req -new -key auth-server-key.pem -out auth-server.csr -subj "/C=BR/ST=DF/L=Brasilia/O=TCC-UnB/CN=auth-server"
openssl x509 -req -in auth-server.csr -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out auth-server-cert.pem -days 365
openssl pkcs12 -export -in auth-server-cert.pem -inkey auth-server-key.pem -out auth-server-keystore.p12 -name auth-server -password pass:openFinance

:: Gerar certificado resource-server
echo.
echo Criando certificado resource-server...
openssl genrsa -out resource-server-key.pem 2048
openssl req -new -key resource-server-key.pem -out resource-server.csr -subj "/C=BR/ST=DF/L=Brasilia/O=TCC-UnB/CN=resource-server"
openssl x509 -req -in resource-server.csr -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out resource-server-cert.pem -days 365
openssl pkcs12 -export -in resource-server-cert.pem -inkey resource-server-key.pem -out resource-server-keystore.p12 -name resource-server -password pass:openFinance

:: Gerar certificado auth-client
echo.
echo Criando certificado auth-client...
openssl genrsa -out auth-client-key.pem 2048
openssl req -new -key auth-client-key.pem -out auth-client.csr -subj "/C=BR/ST=DF/L=Brasilia/O=TCC-UnB/CN=auth-client"
openssl x509 -req -in auth-client.csr -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out auth-client-cert.pem -days 365
openssl pkcs12 -export -in auth-client-cert.pem -inkey auth-client-key.pem -out auth-client-keystore.p12 -name auth-client -password pass:openFinance

:: Gerar certificado tpp-client
echo.
echo Criando certificado tpp-client...
openssl genrsa -out tpp-client-key.pem 2048
openssl req -new -key tpp-client-key.pem -out tpp-client.csr -subj "/C=BR/ST=DF/L=Brasilia/O=TCC-UnB/CN=tpp-client"
openssl x509 -req -in tpp-client.csr -CA ca-cert.pem -CAkey ca-key.pem -CAcreateserial -out tpp-client-cert.pem -days 365
openssl pkcs12 -export -in tpp-client-cert.pem -inkey tpp-client-key.pem -out tpp-client-keystore.p12 -name tpp-client -password pass:openFinance

:: Criar truststore
echo.
echo Criando truststore...
keytool -importcert -file ca-cert.pem -keystore truststore.p12 -alias ca -storetype PKCS12 -storepass openFinance -noprompt

:: Copiar certificados
echo.
echo Distribuindo certificados...

copy auth-server-keystore.p12 "..\auth-server\src\main\resources\certificates\" >nul
copy resource-server-keystore.p12 "..\resource-server\src\main\resources\certificates\" >nul
copy auth-client-keystore.p12 "..\auth-client\src\main\resources\certificates\" >nul
copy tpp-client-keystore.p12 "..\auth-client\src\main\resources\certificates\" >nul

copy truststore.p12 "..\auth-server\src\main\resources\certificates\" >nul
copy truststore.p12 "..\resource-server\src\main\resources\certificates\" >nul
copy truststore.p12 "..\auth-client\src\main\resources\certificates\" >nul

:: Limpar
cd ..
rmdir /s /q temp-certs

echo.
echo ========================================
echo    CERTIFICADOS CRIADOS COM SUCESSO!
echo ========================================
echo.
echo Senha para todos os certificados: openFinance
echo.
pause