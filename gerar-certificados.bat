@echo off
chcp 65001 >nul
echo.
echo ========================================
echo   Gerando Certificados SSL para TCC
echo ========================================
echo.

REM Criar pasta se não existir
if not exist "certificates" mkdir certificates

REM Limpar certificados antigos
del /q certificates\*.p12 2>nul
del /q certificates\*.jks 2>nul

echo [1/6] Gerando certificado Auth Server...
keytool -genkeypair ^
    -alias auth-server ^
    -keyalg RSA ^
    -keysize 2048 ^
    -storetype PKCS12 ^
    -keystore certificates\auth-server-keystore.p12 ^
    -validity 365 ^
    -storepass dilithium ^
    -keypass dilithium ^
    -dname "CN=auth-server, OU=TCC, O=UnB, L=Brasilia, ST=DF, C=BR"

echo [2/6] Gerando certificado Resource Server...
keytool -genkeypair ^
    -alias resource-server ^
    -keyalg RSA ^
    -keysize 2048 ^
    -storetype PKCS12 ^
    -keystore certificates\resource-server-keystore.p12 ^
    -validity 365 ^
    -storepass dilithium ^
    -keypass dilithium ^
    -dname "CN=resource-server, OU=TCC, O=UnB, L=Brasilia, ST=DF, C=BR"

echo [3/6] Gerando certificado Auth Client...
keytool -genkeypair ^
    -alias auth-client ^
    -keyalg RSA ^
    -keysize 2048 ^
    -storetype PKCS12 ^
    -keystore certificates\auth-client-keystore.p12 ^
    -validity 365 ^
    -storepass dilithium ^
    -keypass dilithium ^
    -dname "CN=auth-client, OU=TCC, O=UnB, L=Brasilia, ST=DF, C=BR"

echo [4/6] Gerando certificado TPP Client...
keytool -genkeypair ^
    -alias tpp-client ^
    -keyalg RSA ^
    -keysize 2048 ^
    -storetype PKCS12 ^
    -keystore certificates\tpp-client-keystore.p12 ^
    -validity 365 ^
    -storepass dilithium ^
    -keypass dilithium ^
    -dname "CN=tpp-client, OU=TCC, O=UnB, L=Brasilia, ST=DF, C=BR"

echo [5/6] Exportando certificados públicos...
keytool -exportcert ^
    -alias auth-server ^
    -keystore certificates\auth-server-keystore.p12 ^
    -storepass dilithium ^
    -file certificates\auth-server.cer

keytool -exportcert ^
    -alias resource-server ^
    -keystore certificates\resource-server-keystore.p12 ^
    -storepass dilithium ^
    -file certificates\resource-server.cer

keytool -exportcert ^
    -alias auth-client ^
    -keystore certificates\auth-client-keystore.p12 ^
    -storepass dilithium ^
    -file certificates\auth-client.cer

echo [6/6] Criando TrustStore (compartilhado)...
keytool -importcert ^
    -alias auth-server ^
    -file certificates\auth-server.cer ^
    -keystore certificates\truststore.p12 ^
    -storetype PKCS12 ^
    -storepass dilithium ^
    -noprompt

keytool -importcert ^
    -alias resource-server ^
    -file certificates\resource-server.cer ^
    -keystore certificates\truststore.p12 ^
    -storetype PKCS12 ^
    -storepass dilithium ^
    -noprompt

keytool -importcert ^
    -alias auth-client ^
    -file certificates\auth-client.cer ^
    -keystore certificates\truststore.p12 ^
    -storetype PKCS12 ^
    -storepass dilithium ^
    -noprompt

echo.
echo ========================================
echo   Certificados gerados com SUCESSO!
echo ========================================
echo.
echo  Arquivos criados em certificates\:
dir /b certificates\*.p12
echo.
echo   Senha de todos keystores: dilithium
echo   Validade: 365 dias
echo   Gerado em: %date% %time%
echo.
pause