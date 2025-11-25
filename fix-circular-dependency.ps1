#!/usr/bin/env pwsh
# fix-resource-server.ps1

Write-Host "🔧 Corrigindo Resource Server..." -ForegroundColor Cyan
Write-Host ""

# 1. Parar resource-server
Write-Host "1️⃣ Parando resource-server..." -ForegroundColor Yellow
docker-compose stop resource-server

# 2. Limpar tabela transactions
Write-Host "2️⃣ Limpando tabela transactions..." -ForegroundColor Yellow
docker-compose exec postgres psql -U tcc_user -d resourcedb -c "TRUNCATE TABLE transactions CASCADE;"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Tabela transactions limpa com sucesso!" -ForegroundColor Green
} else {
    Write-Host "❌ Erro ao limpar tabela" -ForegroundColor Red
    exit 1
}

# 3. Verificar
Write-Host "3️⃣ Verificando..." -ForegroundColor Yellow
docker-compose exec postgres psql -U tcc_user -d resourcedb -c "SELECT COUNT(*) as total_transactions FROM transactions;"

# 4. Reiniciar resource-server
Write-Host "4️⃣ Reiniciando resource-server..." -ForegroundColor Yellow
docker-compose start resource-server

# 5. Aguardar
Write-Host "5️⃣ Aguardando 20 segundos para inicialização..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

# 6. Verificar health
Write-Host "6️⃣ Verificando health..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -ErrorAction Stop
    if ($health.status -eq "UP") {
        Write-Host "✅ Resource Server está UP!" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Resource Server não respondeu" -ForegroundColor Red
}

Write-Host ""
Write-Host "🎉 CORREÇÃO CONCLUÍDA!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Verifique os logs:" -ForegroundColor Cyan
Write-Host "   docker-compose logs resource-server --tail=50" -ForegroundColor White
Write-Host ""