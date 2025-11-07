@echo off
chcp 65001 >nul
echo   Desenvolvimento

 
echo   Build Otimizado  
 

REM Maven offline (após primeira execução)
echo   Compilação offline ...
cd auth-server && mvn package -DskipTests -o -q && cd ..
cd resource-server && mvn package -DskipTests -o -q && cd ..  
cd auth-client && mvn package -DskipTests -o -q && cd ..

echo  Docker restart rápido...
docker-compose up -d --no-recreate

echo  Aguardando 15 segundos...
timeout /t 15 /nobreak >nul

echo  Abrindo navegador...
start http://localhost:8081

echo  Ciclo DEV: ~2 minutos!