# ========================================
# 🔍 DIAGNÓSTICO - Verificar Dependências
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔍 DIAGNÓSTICO - Verificar Dependências" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location "C:\Users\laisr\IdeaProjects\TCC1\auth-server"

# ============================================
# 1. VERIFICAR POM.XML
# ============================================
Write-Host "[1/4] Verificando pom.xml..." -ForegroundColor Yellow
Write-Host ""

Select-String -Path "pom.xml" -Pattern "httpclient5" | Out-File -FilePath "temp-httpclient.txt"

Write-Host "📋 Dependências httpclient5 no pom.xml:" -ForegroundColor Cyan
Get-Content "temp-httpclient.txt"
Write-Host ""

$duplicatas = (Select-String -Path "pom.xml" -Pattern "httpclient5").Count

if ($duplicatas -gt 2) {
    Write-Host "❌ ALERTA: $duplicatas ocorrências de httpclient5 encontradas!" -ForegroundColor Red
    Write-Host "   Deveria ter apenas 1 dependency com versão 5.3.1" -ForegroundColor Red
    Write-Host ""
} else {
    Write-Host "✅ Número de dependências parece OK" -ForegroundColor Green
    Write-Host ""
}

Remove-Item "temp-httpclient.txt" -ErrorAction SilentlyContinue

# ============================================
# 2. VERIFICAR MAVEN DEPENDENCIES
# ============================================
Write-Host "[2/4] Verificando árvore de dependências Maven..." -ForegroundColor Yellow
Write-Host ""

mvn dependency:tree -Dincludes=org.apache.httpcomponents.client5:httpclient5 | Out-File -FilePath "dependency-tree.txt"

Write-Host "📋 Árvore de dependências:" -ForegroundColor Cyan
Get-Content "dependency-tree.txt"
Write-Host ""

# ============================================
# 3. VERIFICAR JAR (se existir)
# ============================================
Write-Host "[3/4] Verificando JAR (se existir)..." -ForegroundColor Yellow
Write-Host ""

if (Test-Path "target\auth-server-0.0.1-SNAPSHOT.jar") {
    Write-Host "JAR encontrado. Extraindo informações..." -ForegroundColor Green
    Write-Host ""
    
    Set-Location "target"
    
    # Listar todas as libs incluídas
    jar -tf auth-server-0.0.1-SNAPSHOT.jar | Select-String "BOOT-INF/lib.*httpclient" | Out-File -FilePath "libs.txt"
    
    Write-Host "📦 Bibliotecas httpclient no JAR:" -ForegroundColor Cyan
    Get-Content "libs.txt"
    Write-Host ""
    
    # Verificar TlsSocketStrategy
    $tlsCheck = jar -tf auth-server-0.0.1-SNAPSHOT.jar | Select-String "TlsSocketStrategy"
    
    if ($null -ne $tlsCheck -and $tlsCheck.Count -gt 0) {
        Write-Host "✅ TlsSocketStrategy ENCONTRADO no JAR!" -ForegroundColor Green
        $tlsCheck | ForEach-Object { Write-Host $_ -ForegroundColor Green }
    } else {
        Write-Host "❌ TlsSocketStrategy NÃO encontrado no JAR!" -ForegroundColor Red
    }
    Write-Host ""
    
    Remove-Item "libs.txt" -ErrorAction SilentlyContinue
    Set-Location ".."
} else {
    Write-Host "⚠️ JAR não encontrado. Execute 'mvn package' primeiro." -ForegroundColor Yellow
}
Write-Host ""

# ============================================
# 4. VERIFICAR CACHE MAVEN LOCAL
# ============================================
Write-Host "[4/4] Verificando cache Maven local..." -ForegroundColor Yellow
Write-Host ""

$MavenRepo = "$env:USERPROFILE\.m2\repository\org\apache\httpcomponents"

if (Test-Path "$MavenRepo\client5\httpclient5") {
    Write-Host "📁 Versões de httpclient5 no cache Maven:" -ForegroundColor Cyan
    Get-ChildItem "$MavenRepo\client5\httpclient5" -Directory | Select-Object -ExpandProperty Name
    Write-Host ""
} else {
    Write-Host "⚠️ httpclient5 não encontrado no cache Maven" -ForegroundColor Yellow
    Write-Host ""
}

if (Test-Path "$MavenRepo\core5\httpcore5") {
    Write-Host "📁 Versões de httpcore5 no cache Maven:" -ForegroundColor Cyan
    Get-ChildItem "$MavenRepo\core5\httpcore5" -Directory | Select-Object -ExpandProperty Name
    Write-Host ""
} else {
    Write-Host "⚠️ httpcore5 não encontrado no cache Maven" -ForegroundColor Yellow
    Write-Host ""
}

# Verificar versão 4 (não deve existir)
if (Test-Path "$MavenRepo\httpclient") {
    Write-Host "❌ ALERTA: HttpClient versão 4 encontrado no cache!" -ForegroundColor Red
    Write-Host "   Isso pode causar conflitos." -ForegroundColor Red
    Get-ChildItem "$MavenRepo\httpclient" -Directory | Select-Object -ExpandProperty Name
    Write-Host ""
} else {
    Write-Host "✅ HttpClient versão 4 não está no cache (correto)" -ForegroundColor Green
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 DIAGNÓSTICO CONCLUÍDO" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "💡 Próximos passos:" -ForegroundColor Yellow
Write-Host ""
Write-Host "Se encontrou problemas:" -ForegroundColor Yellow
Write-Host "  1. Execute: .\rebuild-completo.ps1" -ForegroundColor White
Write-Host ""
Write-Host "Se está tudo OK:" -ForegroundColor Yellow
Write-Host "  1. Execute: mvn clean package -DskipTests" -ForegroundColor White
Write-Host "  2. Execute: docker-compose build --no-cache auth-server" -ForegroundColor White
Write-Host "  3. Execute: docker-compose up" -ForegroundColor White
Write-Host ""

Read-Host "Pressione Enter para sair"