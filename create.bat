@echo off
:: Navegue até auth-server no Windows
cd C:\Users\laisr\IdeaProjects\TCC1\auth-server

:: Criar pasta se não existir
if not exist "src\main\resources\certificates" mkdir src\main\resources\certificates

:: Criar keystore
keytool -genkeypair -alias auth-server -keyalg RSA -keysize 2048 -validity 365 -storetype PKCS12 -keystore src\main\resources\certificates\auth-server-keystore.p12 -storepass changeit -keypass changeit -dname "CN=localhost, OU=OpenFinance, O=TCC-UnB, L=Brasilia, ST=DF, C=BR"

:: Criar truststore  
keytool -genkeypair -alias truststore -keyalg RSA -keysize 2048 -validity 365 -storetype PKCS12 -keystore src\main\resources\certificates\auth-server-truststore.p12 -storepass changeit -keypass changeit -dname "CN=truststore, OU=OpenFinance, O=TCC-UnB, L=Brasilia, ST=DF, C=BR"

:: Verificar
dir src\main\resources\certificates\*.p12