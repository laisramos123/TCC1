# ==========================================
# 🔧 SCRIPT DE CORREÇÃO DO TCC
# ==========================================
# Execute este script no PowerShell para aplicar todas as correções

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🔧 APLICANDO CORREÇÕES DO TCC" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Copiar script SQL para o container
Write-Host "📋 Copiando script de correção para o container..." -ForegroundColor Yellow
docker cp fix-database.sql tcc-postgres:/tmp/fix-database.sql

# 2. Executar script SQL
Write-Host "🗄️ Executando correções no banco de dados..." -ForegroundColor Yellow
docker exec tcc-postgres psql -U tcc_user -d postgres -f /tmp/fix-database.sql

# 3. Verificar se o cliente foi criado
Write-Host ""
Write-Host "🔍 Verificando cliente OAuth2..." -ForegroundColor Yellow
docker exec tcc-postgres psql -U tcc_user -d authdb -c "SELECT client_id, scopes FROM oauth2_registered_client;"

# 4. Reiniciar o auth-server
Write-Host ""
Write-Host "🔄 Reiniciando auth-server..." -ForegroundColor Yellow
docker-compose restart auth-server

# 5. Aguardar inicialização
Write-Host ""
Write-Host "⏳ Aguardando auth-server inicializar (60 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 60

# 6. Verificar saúde
Write-Host ""
Write-Host "💚 Verificando saúde dos serviços..." -ForegroundColor Yellow
docker-compose ps

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "✅ CORREÇÕES APLICADAS!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Próximos passos:" -ForegroundColor White
Write-Host "  1. Acesse http://localhost:8081" -ForegroundColor White
Write-Host "  2. Clique em 'Compartilhar Dados'" -ForegroundColor White
Write-Host "  3. Faça login com: joao.silva / password" -ForegroundColor White
Write-Host ""
Write-Host "Se ainda houver erros, veja os logs:" -ForegroundColor Yellow
Write-Host "  docker-compose logs -f auth-server" -ForegroundColor Yellow
Write-Host ""