@echo off
echo ========================================
echo    CRIANDO CERTIFICADOS MTLS - WINDOWS
echo ========================================

:: Configurações
set PASS=changeit
set CERT_DIR=src\main\resources\certificates

:: Criar diretórios
if not exist "%CERT_DIR%" mkdir "%CERT_DIR%"

:: Navegar para auth-server
cd auth-server 2>nul || (
    echo [ERRO] Execute este script na pasta raiz do projeto TCC1
    pause
    exit /b 1
)

:: Criar Keystore para auth-server
echo.
echo Criando auth-server-keystore.p12...
keytool -genkeypair ^
  -alias auth-server ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 365 ^
  -storetype PKCS12 ^
  -keystore "%CERT_DIR%\auth-server-keystore.p12" ^
  -storepass %PASS% ^
  -keypass %PASS% ^
  -dname "CN=localhost, OU=TCC, O=UnB, L=Brasilia, ST=DF, C=BR" ^
  -ext "SAN=dns:localhost,dns:auth-server,ip:127.0.0.1"

:: Criar Truststore
echo.
echo Criando auth-server-truststore.p12...
keytool -genkeypair ^
  -alias truststore ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 365 ^
  -storetype PKCS12 ^
  -keystore "%CERT_DIR%\auth-server-truststore.p12" ^
  -storepass %PASS% ^
  -keypass %PASS% ^
  -dname "CN=truststore, OU=TCC, O=UnB, L=Brasilia, ST=DF, C=BR"

:: Verificar criação
echo.
echo Verificando certificados criados:
dir "%CERT_DIR%\*.p12"

:: Voltar para pasta raiz
cd ..

echo.
echo ========================================
echo    CERTIFICADOS CRIADOS COM SUCESSO!
echo    Senha: %PASS%
echo ========================================
pause