# ============================================
# DIAGNOSTICAR E CORRIGIR authorizationServerInternal
# ============================================

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   DIAGNOSTICAR authorizationServerInternal no Auth Client  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

$baseDir = "C:\Users\laisr\IdeaProjects\TCC1"
Set-Location $baseDir

# ============================================
# 1. VER ONDE authorizationServerInternal É USADO
# ============================================

Write-Host "1️⃣  Procurando 'authorizationServerInternal' no código..." -ForegroundColor Yellow
Write-Host ""

$usages = Get-ChildItem -Path "auth-client\src\main\java" -Recurse -Filter *.java | Select-String "authorizationServerInternal" -Context 2

if ($usages) {
    Write-Host "   Encontrado em:" -ForegroundColor White
    Write-Host ""
    
    foreach ($usage in $usages) {
        Write-Host "   📄 $($usage.Filename):$($usage.LineNumber)" -ForegroundColor Cyan
        Write-Host ""
        
        # Mostrar contexto
        $usage.Context.PreContext | ForEach-Object { Write-Host "      $_" -ForegroundColor Gray }
        Write-Host "      $($usage.Line.Trim())" -ForegroundColor Yellow
        $usage.Context.PostContext | ForEach-Object { Write-Host "      $_" -ForegroundColor Gray }
        Write-Host ""
    }
} else {
    Write-Host "   ℹ️  Variável 'authorizationServerInternal' não encontrada" -ForegroundColor Cyan
}

Write-Host ""

# ============================================
# 2. VER ONDE A VARIÁVEL É DEFINIDA
# ============================================

Write-Host "2️⃣  Procurando definição da variável..." -ForegroundColor Yellow
Write-Host ""

$definitions = Get-ChildItem -Path "auth-client\src\main\java" -Recurse -Filter *.java | Select-String "@Value.*authorization.*server|authorizationServer|authorization.*server" -Context 3

if ($definitions) {
    Write-Host "   Definições encontradas:" -ForegroundColor White
    Write-Host ""
    
    foreach ($def in $definitions | Select-Object -First 10) {
        Write-Host "   📄 $($def.Filename):$($def.LineNumber)" -ForegroundColor Cyan
        Write-Host "      $($def.Line.Trim())" -ForegroundColor White
        Write-Host ""
    }
}

Write-Host ""

# ============================================
# 3. VER CONFIGURAÇÃO ATUAL
# ============================================

Write-Host "3️⃣  Configuração atual (application-docker.yml)..." -ForegroundColor Yellow
Write-Host ""

if (Test-Path "auth-client\src\main\resources\application-docker.yml") {
    $config = Get-Content "auth-client\src\main\resources\application-docker.yml" | Select-String "authorization|auth.*server|8080"
    
    if ($config) {
        $config | ForEach-Object {
            if ($_.Line -match "localhost:8080") {
                Write-Host "   ✅ $($_.Line.Trim())" -ForegroundColor Green
            } elseif ($_.Line -match "auth-server:8080") {
                Write-Host "   ❌ $($_.Line.Trim())" -ForegroundColor Red
            } else {
                Write-Host "   ℹ️  $($_.Line.Trim())" -ForegroundColor Gray
            }
        }
    }
} else {
    Write-Host "   ⚠️  application-docker.yml não encontrado!" -ForegroundColor Yellow
}

Write-Host ""

# ============================================
# 4. SUGERIR CORREÇÃO
# ============================================

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                        SOLUÇÃO                             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

Write-Host "💡 O Auth Client precisa de DUAS configurações:" -ForegroundColor Yellow
Write-Host ""

Write-Host "   1️⃣  Para OAuth2 (redirecionamentos do navegador):" -ForegroundColor White
Write-Host "      spring.security.oauth2.client.provider.*.authorization-uri" -ForegroundColor Gray
Write-Host "      Deve ser: http://localhost:8080/oauth2/authorize" -ForegroundColor Green
Write-Host ""

Write-Host "   2️⃣  Para API de consents (requisições HTTP diretas):" -ForegroundColor White
Write-Host "      Variável customizada no application-docker.yml" -ForegroundColor Gray
Write-Host ""

Write-Host "📝 Adicionar no application-docker.yml:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   authorization-server:" -ForegroundColor White
Write-Host "     internal: http://auth-server:8080     # Comunicação interna Docker" -ForegroundColor Gray
Write-Host "     public: http://localhost:8080         # URLs para o navegador" -ForegroundColor Gray
Write-Host ""

Write-Host "🔧 E no código Java, usar:" -ForegroundColor Yellow
Write-Host ""
Write-Host "   @Value(" -NoNewline -ForegroundColor White
Write-Host '"\${authorization-server.internal}"' -NoNewline -ForegroundColor Gray
Write-Host ")" -ForegroundColor White
Write-Host "   private String authorizationServerInternal;" -ForegroundColor White
Write-Host ""

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Cyan
Write-Host ""

# ============================================
# 5. OFERECER CORREÇÃO AUTOMÁTICA
# ============================================

Write-Host "🤖 Correção automática disponível!" -ForegroundColor Yellow
Write-Host ""

$autoFix = Read-Host "Deseja que eu crie o arquivo application-docker.yml corrigido? (S/N)"

if ($autoFix -eq "S" -or $autoFix -eq "s") {
    Write-Host ""
    Write-Host "📥 Baixe o arquivo corrigido aqui:" -ForegroundColor Green
    Write-Host "   auth-client-application-docker-COMPLETO-CORRIGIDO.yml" -ForegroundColor White
    Write-Host ""
    Write-Host "📁 Substitua em:" -ForegroundColor Yellow
    Write-Host "   auth-client\src\main\resources\application-docker.yml" -ForegroundColor White
    Write-Host ""
    Write-Host "🔨 Depois, rebuild:" -ForegroundColor Yellow
    Write-Host "   mvn clean package -DskipTests -f auth-client/pom.xml" -ForegroundColor White
    Write-Host "   docker-compose build auth-client" -ForegroundColor White
    Write-Host "   docker-compose up -d auth-client" -ForegroundColor White
    Write-Host ""
}

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║                 DIAGNÓSTICO CONCLUÍDO                      ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""