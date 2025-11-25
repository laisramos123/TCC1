#!/usr/bin/env pwsh
# fix-auth-client-complete.ps1
# 🔧 Correção Completa de URLs do Auth-Client

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  🔧 CORREÇÃO COMPLETA - AUTH-CLIENT URLS                  ║" -ForegroundColor Cyan
Write-Host "║  Corrigindo problema de redirect localhost vs auth-server ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"

# ========================================
# 1. BACKUP DOS ARQUIVOS ORIGINAIS
# ========================================
Write-Host "📦 PASSO 1/7: Criando backups..." -ForegroundColor Yellow

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupDir = "backups/$timestamp"

if (!(Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null
}

# Backup do application-docker
if (Test-Path "auth-client/src/main/resources/application-docker") {
    Copy-Item "auth-client/src/main/resources/application-docker" `
              "$backupDir/application-docker.backup" -Force
    Write-Host "✅ Backup: application-docker" -ForegroundColor Green
}

# Backup do AuthorizationService.java
if (Test-Path "auth-client/src/main/java/com/example/auth_client/service/AuthorizationService.java") {
    Copy-Item "auth-client/src/main/java/com/example/auth_client/service/AuthorizationService.java" `
              "$backupDir/AuthorizationService.java.backup" -Force
    Write-Host "✅ Backup: AuthorizationService.java" -ForegroundColor Green
}

Write-Host ""

# ========================================
# 2. COPIAR ARQUIVOS CORRIGIDOS
# ========================================
Write-Host "📝 PASSO 2/7: Copiando arquivos corrigidos..." -ForegroundColor Yellow

# Copiar application-docker.yml
if (Test-Path "/mnt/user-data/outputs/application-docker-CORRIGIDO.yml") {
    Copy-Item "/mnt/user-data/outputs/application-docker-CORRIGIDO.yml" `
              "auth-client/src/main/resources/application-docker" -Force
    Write-Host "✅ Copiado: application-docker" -ForegroundColor Green
} else {
    Write-Host "❌ Arquivo não encontrado: application-docker-CORRIGIDO.yml" -ForegroundColor Red
    exit 1
}

# Copiar AuthorizationService.java
if (Test-Path "/mnt/user-data/outputs/AuthorizationService-CORRIGIDO.java") {
    Copy-Item "/mnt/user-data/outputs/AuthorizationService-CORRIGIDO.java" `
              "auth-client/src/main/java/com/example/auth_client/service/AuthorizationService.java" -Force
    Write-Host "✅ Copiado: AuthorizationService.java" -ForegroundColor Green
} else {
    Write-Host "❌ Arquivo não encontrado: AuthorizationService-CORRIGIDO.java" -ForegroundColor Red
    exit 1
}

Write-Host ""

# ========================================
# 3. PARAR AUTH-CLIENT
# ========================================
Write-Host "🛑 PASSO 3/7: Parando auth-client..." -ForegroundColor Yellow
docker-compose stop auth-client

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Auth-client parado" -ForegroundColor Green
} else {
    Write-Host "⚠️  Aviso: Erro ao parar auth-client (pode já estar parado)" -ForegroundColor Yellow
}

Write-Host ""

# ========================================
# 4. REBUILD AUTH-CLIENT
# ========================================
Write-Host "🔨 PASSO 4/7: Rebuilding auth-client..." -ForegroundColor Yellow
Write-Host "   (Isso pode levar alguns minutos)" -ForegroundColor Gray

docker-compose build auth-client

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build concluído com sucesso" -ForegroundColor Green
} else {
    Write-Host "❌ Erro no build do auth-client" -ForegroundColor Red
    Write-Host "Restaurando backups..." -ForegroundColor Yellow
    
    # Restaurar backups
    Copy-Item "$backupDir/application-docker.backup" `
              "auth-client/src/main/resources/application-docker" -Force
    Copy-Item "$backupDir/AuthorizationService.java.backup" `
              "auth-client/src/main/java/com/example/auth_client/service/AuthorizationService.java" -Force
    
    exit 1
}

Write-Host ""

# ========================================
# 5. RESTART AUTH-CLIENT
# ========================================
Write-Host "🚀 PASSO 5/7: Iniciando auth-client..." -ForegroundColor Yellow
docker-compose up -d auth-client

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Auth-client iniciado" -ForegroundColor Green
} else {
    Write-Host "❌ Erro ao iniciar auth-client" -ForegroundColor Red
    exit 1
}

Write-Host ""

# ========================================
# 6. AGUARDAR INICIALIZAÇÃO
# ========================================
Write-Host "⏳ PASSO 6/7: Aguardando inicialização (25 segundos)..." -ForegroundColor Yellow

for ($i = 25; $i -gt 0; $i--) {
    Write-Host -NoNewline "`r   Restam $i segundos... " -ForegroundColor Gray
    Start-Sleep -Seconds 1
}

Write-Host "`r   ✅ Aguardando concluído               " -ForegroundColor Green
Write-Host ""

# ========================================
# 7. VERIFICAR HEALTH E LOGS
# ========================================
Write-Host "🔍 PASSO 7/7: Verificando status..." -ForegroundColor Yellow

# Health check
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -TimeoutSec 10 -ErrorAction Stop
    if ($health.status -eq "UP") {
        Write-Host "✅ Health check: AUTH-CLIENT está UP!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Health check: Status = $($health.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Health check falhou: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# Verificar logs para URL gerada
Write-Host "📋 Últimas linhas dos logs:" -ForegroundColor Cyan
docker-compose logs auth-client --tail=30 | Select-String -Pattern "AUTHORIZATION URL|Started AuthClientApplication|error|exception" | Select-Object -Last 10

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  ✅ CORREÇÃO CONCLUÍDA COM SUCESSO!                       ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""

# ========================================
# 8. INSTRUÇÕES DE TESTE
# ========================================
Write-Host "🧪 COMO TESTAR:" -ForegroundColor Cyan
Write-Host ""
Write-Host "   1️⃣  Abra o navegador: http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "   2️⃣  Preencha o formulário:" -ForegroundColor White
Write-Host "       CPF: 064.694.021-00" -ForegroundColor Gray
Write-Host "       Permissões: (deixe marcadas)" -ForegroundColor Gray
Write-Host ""
Write-Host "   3️⃣  Clique em 'Conectar Banco'" -ForegroundColor White
Write-Host ""
Write-Host "   4️⃣  VERIFIQUE A URL DO NAVEGADOR:" -ForegroundColor White
Write-Host "       ✅ CORRETO: http://localhost:8080/oauth2/authorize?..." -ForegroundColor Green
Write-Host "       ❌ ERRADO:  http://auth-server:8080/oauth2/authorize?..." -ForegroundColor Red
Write-Host ""
Write-Host "   5️⃣  Se aparecer tela de login do auth-server → SUCESSO! 🎉" -ForegroundColor White
Write-Host "       Login: user" -ForegroundColor Gray
Write-Host "       Senha: password" -ForegroundColor Gray
Write-Host ""

Write-Host "📁 Backups salvos em: $backupDir" -ForegroundColor Cyan
Write-Host ""

# Perguntar se deve abrir o navegador
$openBrowser = Read-Host "Deseja abrir o navegador agora? (S/N)"
if ($openBrowser -eq "S" -or $openBrowser -eq "s") {
    Start-Process "http://localhost:8081"
    Write-Host "✅ Navegador aberto!" -ForegroundColor Green
}

Write-Host ""
Write-Host "🔧 Para ver logs em tempo real:" -ForegroundColor Cyan
Write-Host "   docker-compose logs -f auth-client" -ForegroundColor White
Write-Host ""