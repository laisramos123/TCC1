@echo off
chcp 65001 >nul
echo   Iniciando AMBOS os Authorization Servers

echo.
echo ====================================
echo   Compilando projetos...
echo ====================================

cd auth-server
call mvn clean package -DskipTests -q
cd ..

cd resource-server
call mvn clean package -DskipTests -q
cd ..

cd auth-client
call mvn clean package -DskipTests -q
cd ..

echo.
echo ====================================
echo   Iniciando Auth Server RSA (8080)
echo ====================================
start "Auth-Server-RSA" java -jar auth-server/target/auth-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=rsa

timeout /t 10 /nobreak >nul

echo.
echo ====================================
echo  Iniciando Auth Server DILITHIUM (9080)
echo ====================================
start "Auth-Server-Dilithium" java -jar auth-server/target/auth-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=dilithium

timeout /t 10 /nobreak >nul

echo.
echo ====================================
echo   Iniciando Resource Server RSA (8082)
echo ====================================
start "Resource-Server-RSA" java -jar resource-server/target/resource-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=rsa

timeout /t 10 /nobreak >nul

echo.
echo ====================================
echo   Iniciando Resource Server DILITHIUM (9082)
echo ====================================
start "Resource-Server-Dilithium" java -jar resource-server/target/resource-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=dilithium

timeout /t 10 /nobreak >nul

echo.
echo ====================================
echo  Iniciando Auth Client (8081)
echo ====================================
start "Auth-Client" java -jar auth-client/target/auth-client-0.0.1-SNAPSHOT.jar

echo.
echo   TODOS OS SERVIDORES INICIADOS!
echo.
echo   URLs Disponíveis:
echo    RSA Auth:       http://localhost:8080
echo    Dilithium Auth: http://localhost:9080
echo    RSA Resources:  http://localhost:8082
echo    Dilithium Res:  http://localhost:9082
echo    Auth Client:    http://localhost:8081
echo.
pause