@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Gerando Certificados mTLS - Open Finance
echo ========================================
echo.

set PASSWORD=tcc-openFinance
set CERT_DIR=certificates
set VALIDITY=365

:: Limpar certificados antigos
echo [1/8] Limpando certificados antigos...
if exist %CERT_DIR% rmdir /s /q %CERT_DIR%
mkdir %CERT_DIR%
cd %CERT_DIR%

:: 1. Gerar CA
echo [2/8] Gerando CA (Certificate Authority)...
keytool -genkeypair -alias ca -keyalg RSA -keysize 2048 -validity %VALIDITY% ^
  -dname "CN=Open Finance CA, OU=TCC, O=Universidade, L=Brasil, ST=SP, C=BR" ^
  -keystore ca-keystore.p12 -storetype PKCS12 -storepass %PASSWORD% -keypass %PASSWORD%

keytool -exportcert -alias ca -keystore ca-keystore.p12 -storepass %PASSWORD% -file ca.cer -rfc

:: 2. Auth Server
echo [3/8] Gerando certificado do Auth Server...
keytool -genkeypair -alias auth-server -keyalg RSA -keysize 2048 -validity %VALIDITY% ^
  -dname "CN=auth-server, OU=TCC, O=Universidade, L=Brasil, ST=SP, C=BR" ^
  -ext "SAN=DNS:auth-server,DNS:localhost,IP:127.0.0.1" ^
  -keystore auth-server-keystore.p12 -storetype PKCS12 -storepass %PASSWORD% -keypass %PASSWORD%

keytool -exportcert -alias auth-server -keystore auth-server-keystore.p12 -storepass %PASSWORD% -file auth-server.cer -rfc

:: 3. Resource Server
echo [4/8] Gerando certificado do Resource Server...
keytool -genkeypair -alias resource-server -keyalg RSA -keysize 2048 -validity %VALIDITY% ^
  -dname "CN=resource-server, OU=TCC, O=Universidade, L=Brasil, ST=SP, C=BR" ^
  -ext "SAN=DNS:resource-server,DNS:localhost,IP:127.0.0.1" ^
  -keystore resource-server-keystore.p12 -storetype PKCS12 -storepass %PASSWORD% -keypass %PASSWORD%

keytool -exportcert -alias resource-server -keystore resource-server-keystore.p12 -storepass %PASSWORD% -file resource-server.cer -rfc

:: 4. TPP Client
echo [5/8] Gerando certificado do TPP Client...
keytool -genkeypair -alias tpp-client -keyalg RSA -keysize 2048 -validity %VALIDITY% ^
  -dname "CN=tpp-client, OU=TPP, O=Open Finance TPP, L=Brasil, ST=SP, C=BR" ^
  -ext "SAN=DNS:auth-client,DNS:localhost,IP:127.0.0.1" ^
  -keystore tpp-client-keystore.p12 -storetype PKCS12 -storepass %PASSWORD% -keypass %PASSWORD%

copy tpp-client-keystore.p12 auth-client-keystore.p12

:: 5. Truststore
echo [6/8] Criando Truststore...
keytool -importcert -alias ca -keystore truststore.p12 -storetype PKCS12 -storepass %PASSWORD% -file ca.cer -noprompt
keytool -importcert -alias auth-server -keystore truststore.p12 -storetype PKCS12 -storepass %PASSWORD% -file auth-server.cer -noprompt
keytool -importcert -alias resource-server -keystore truststore.p12 -storetype PKCS12 -storepass %PASSWORD% -file resource-server.cer -noprompt

:: 6. Copiar para servicos
echo [7/8] Copiando certificados...

if not exist "..\auth-server\src\main\resources\certificates" mkdir "..\auth-server\src\main\resources\certificates"
copy auth-server-keystore.p12 "..\auth-server\src\main\resources\certificates\"
copy truststore.p12 "..\auth-server\src\main\resources\certificates\"

if not exist "..\resource-server\src\main\resources\certificates" mkdir "..\resource-server\src\main\resources\certificates"
copy resource-server-keystore.p12 "..\resource-server\src\main\resources\certificates\"
copy truststore.p12 "..\resource-server\src\main\resources\certificates\"

if not exist "..\auth-client\src\main\resources\certificates" mkdir "..\auth-client\src\main\resources\certificates"
copy tpp-client-keystore.p12 "..\auth-client\src\main\resources\certificates\"
copy auth-client-keystore.p12 "..\auth-client\src\main\resources\certificates\"
copy truststore.p12 "..\auth-client\src\main\resources\certificates\"

cd ..

echo.
echo [8/8] Verificando...
dir /b certificates\*.p12

echo.
echo ========================================
echo ✅ Certificados gerados com sucesso!
echo    Senha: tcc-openFinance
echo ========================================
pause