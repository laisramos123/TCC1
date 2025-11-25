# ============================================
# SCRIPT DE AUDITORIA COMPLETA - TCC
# ============================================
# Verifica TODOS os componentes do projeto
# Gera relatório detalhado em HTML
# ============================================

param(
    [string]$ProjectPath = "C:\Users\laisr\IdeaProjects\TCC1"
)

# Cores para output
$ErrorColor = "Red"
$WarningColor = "Yellow"
$SuccessColor = "Green"
$InfoColor = "Cyan"

# Contadores
$totalChecks = 0
$passedChecks = 0
$failedChecks = 0
$warnings = 0

# Resultados
$results = @()

# Função auxiliar para verificar arquivo
function Test-FileExists {
    param([string]$Path, [string]$Description, [bool]$Critical = $true)
    
    $global:totalChecks++
    $fullPath = Join-Path $ProjectPath $Path
    
    $result = @{
        Category = ""
        Item = $Description
        Path = $Path
        Status = ""
        Message = ""
        Critical = $Critical
    }
    
    if (Test-Path $fullPath) {
        $global:passedChecks++
        $result.Status = "✅ OK"
        $result.Message = "Arquivo encontrado"
        Write-Host "  ✅ $Description" -ForegroundColor $SuccessColor
    } else {
        if ($Critical) {
            $global:failedChecks++
            $result.Status = "❌ ERRO"
            $result.Message = "Arquivo NÃO encontrado (CRÍTICO)"
            Write-Host "  ❌ $Description - NÃO ENCONTRADO (CRÍTICO)" -ForegroundColor $ErrorColor
        } else {
            $global:warnings++
            $result.Status = "⚠️ AVISO"
            $result.Message = "Arquivo NÃO encontrado (OPCIONAL)"
            Write-Host "  ⚠️ $Description - NÃO ENCONTRADO (OPCIONAL)" -ForegroundColor $WarningColor
        }
    }
    
    return $result
}

# Função para verificar conteúdo de arquivo
function Test-FileContent {
    param(
        [string]$Path,
        [string]$Description,
        [string]$Pattern,
        [bool]$Critical = $true
    )
    
    $global:totalChecks++
    $fullPath = Join-Path $ProjectPath $Path
    
    $result = @{
        Category = ""
        Item = $Description
        Path = $Path
        Status = ""
        Message = ""
        Critical = $Critical
    }
    
    if (-not (Test-Path $fullPath)) {
        $global:failedChecks++
        $result.Status = "❌ ERRO"
        $result.Message = "Arquivo não existe"
        Write-Host "  ❌ $Description - ARQUIVO NÃO EXISTE" -ForegroundColor $ErrorColor
        return $result
    }
    
    $content = Get-Content $fullPath -Raw -ErrorAction SilentlyContinue
    
    if ($content -match $Pattern) {
        $global:passedChecks++
        $result.Status = "✅ OK"
        $result.Message = "Conteúdo encontrado"
        Write-Host "  ✅ $Description" -ForegroundColor $SuccessColor
    } else {
        if ($Critical) {
            $global:failedChecks++
            $result.Status = "❌ ERRO"
            $result.Message = "Conteúdo esperado NÃO encontrado"
            Write-Host "  ❌ $Description - CONTEÚDO NÃO ENCONTRADO" -ForegroundColor $ErrorColor
        } else {
            $global:warnings++
            $result.Status = "⚠️ AVISO"
            $result.Message = "Conteúdo esperado NÃO encontrado (OPCIONAL)"
            Write-Host "  ⚠️ $Description - CONTEÚDO NÃO ENCONTRADO" -ForegroundColor $WarningColor
        }
    }
    
    return $result
}

# Função para verificar diretório
function Test-DirectoryExists {
    param([string]$Path, [string]$Description, [bool]$Critical = $true)
    
    $global:totalChecks++
    $fullPath = Join-Path $ProjectPath $Path
    
    $result = @{
        Category = ""
        Item = $Description
        Path = $Path
        Status = ""
        Message = ""
        Critical = $Critical
    }
    
    if (Test-Path $fullPath -PathType Container) {
        $global:passedChecks++
        $result.Status = "✅ OK"
        $result.Message = "Diretório encontrado"
        Write-Host "  ✅ $Description" -ForegroundColor $SuccessColor
    } else {
        if ($Critical) {
            $global:failedChecks++
            $result.Status = "❌ ERRO"
            $result.Message = "Diretório NÃO encontrado (CRÍTICO)"
            Write-Host "  ❌ $Description - NÃO ENCONTRADO (CRÍTICO)" -ForegroundColor $ErrorColor
        } else {
            $global:warnings++
            $result.Status = "⚠️ AVISO"
            $result.Message = "Diretório NÃO encontrado (OPCIONAL)"
            Write-Host "  ⚠️ $Description - NÃO ENCONTRADO (OPCIONAL)" -ForegroundColor $WarningColor
        }
    }
    
    return $result
}

# ============================================
# INÍCIO DA AUDITORIA
# ============================================

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor $InfoColor
Write-Host "║         AUDITORIA COMPLETA - PROJETO TCC                   ║" -ForegroundColor $InfoColor
Write-Host "║  Implementação de Criptografia Pós-Quântica               ║" -ForegroundColor $InfoColor
Write-Host "║           Open Finance Brasil + Dilithium3                 ║" -ForegroundColor $InfoColor
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor $InfoColor
Write-Host ""
Write-Host "📂 Diretório do Projeto: $ProjectPath" -ForegroundColor $InfoColor
Write-Host "🕐 Data/Hora: $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')" -ForegroundColor $InfoColor
Write-Host ""

if (-not (Test-Path $ProjectPath)) {
    Write-Host "❌ ERRO CRÍTICO: Diretório do projeto não encontrado!" -ForegroundColor $ErrorColor
    Write-Host "   Caminho: $ProjectPath" -ForegroundColor $ErrorColor
    exit 1
}

# ============================================
# 1. ESTRUTURA DE DIRETÓRIOS
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "📁 1. VERIFICANDO ESTRUTURA DE DIRETÓRIOS" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Estrutura"

$r = Test-DirectoryExists "auth-server" "Auth Server - Diretório Principal" $true
$r.Category = $category; $results += $r

$r = Test-DirectoryExists "resource-server" "Resource Server - Diretório Principal" $true
$r.Category = $category; $results += $r

$r = Test-DirectoryExists "auth-client" "Auth Client - Diretório Principal" $true
$r.Category = $category; $results += $r

$r = Test-DirectoryExists "monitoring" "Monitoring - Diretório Principal" $false
$r.Category = $category; $results += $r

$r = Test-DirectoryExists "monitoring/prometheus" "Prometheus - Configuração" $false
$r.Category = $category; $results += $r

$r = Test-DirectoryExists "monitoring/grafana" "Grafana - Configuração" $false
$r.Category = $category; $results += $r

$r = Test-DirectoryExists "jmeter" "JMeter - Testes" $false
$r.Category = $category; $results += $r

$r = Test-DirectoryExists "init-scripts" "Init Scripts - PostgreSQL" $true
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# 2. ARQUIVOS DOCKER
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "🐳 2. VERIFICANDO CONFIGURAÇÕES DOCKER" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Docker"

$r = Test-FileExists "docker-compose.yml" "Docker Compose - Configuração Principal" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "docker-compose.yml" "Docker Compose - Serviço Prometheus" "prometheus:" $false
$r.Category = $category; $results += $r

$r = Test-FileContent "docker-compose.yml" "Docker Compose - Serviço Grafana" "grafana:" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "auth-server/Dockerfile" "Auth Server - Dockerfile" $true
$r.Category = $category; $results += $r

$r = Test-FileExists "resource-server/Dockerfile" "Resource Server - Dockerfile" $true
$r.Category = $category; $results += $r

$r = Test-FileExists "auth-client/Dockerfile" "Auth Client - Dockerfile" $true
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# 3. DEPENDÊNCIAS MAVEN
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "📦 3. VERIFICANDO DEPENDÊNCIAS MAVEN" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Maven"

$r = Test-FileExists "auth-server/pom.xml" "Auth Server - pom.xml" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "auth-server/pom.xml" "Auth Server - Spring Boot Starter" "spring-boot-starter" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "auth-server/pom.xml" "Auth Server - Bouncy Castle (Dilithium)" "bcprov-jdk18on" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "auth-server/pom.xml" "Auth Server - Micrometer Prometheus" "micrometer-registry-prometheus" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "resource-server/pom.xml" "Resource Server - pom.xml" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "resource-server/pom.xml" "Resource Server - Spring Security OAuth2" "spring-security-oauth2" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "resource-server/pom.xml" "Resource Server - Micrometer Prometheus" "micrometer-registry-prometheus" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "auth-client/pom.xml" "Auth Client - pom.xml" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "auth-client/pom.xml" "Auth Client - Spring Boot Web" "spring-boot-starter-web" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "auth-client/pom.xml" "Auth Client - Micrometer Prometheus" "micrometer-registry-prometheus" $false
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# 4. CONFIGURAÇÕES SPRING BOOT
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "⚙️ 4. VERIFICANDO CONFIGURAÇÕES SPRING BOOT" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Spring Boot"

$r = Test-FileExists "auth-server/src/main/resources/application.yml" "Auth Server - application.yml" $true
$r.Category = $category; $results += $r

$r = Test-FileExists "auth-server/src/main/resources/application-docker.yml" "Auth Server - application-docker.yml" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "auth-server/src/main/resources/application-docker.yml" "Auth Server - Management Endpoints" "management:" $false
$r.Category = $category; $results += $r

$r = Test-FileContent "auth-server/src/main/resources/application-docker.yml" "Auth Server - Prometheus Endpoint" "prometheus" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "resource-server/src/main/resources/application.yml" "Resource Server - application.yml" $true
$r.Category = $category; $results += $r

$r = Test-FileExists "resource-server/src/main/resources/application-docker.yml" "Resource Server - application-docker.yml" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "resource-server/src/main/resources/application-docker.yml" "Resource Server - Management Endpoints" "management:" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "auth-client/src/main/resources/application.yml" "Auth Client - application.yml" $true
$r.Category = $category; $results += $r

$r = Test-FileExists "auth-client/src/main/resources/application-docker.yml" "Auth Client - application-docker.yml" $true
$r.Category = $category; $results += $r

$r = Test-FileContent "auth-client/src/main/resources/application-docker.yml" "Auth Client - Management Endpoints" "management:" $false
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# 5. CÓDIGO-FONTE PRINCIPAL
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "💻 5. VERIFICANDO CÓDIGO-FONTE PRINCIPAL" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Código-fonte"

# Auth Server
$r = Test-DirectoryExists "auth-server/src/main/java" "Auth Server - src/main/java" $true
$r.Category = $category; $results += $r

# Resource Server
$r = Test-DirectoryExists "resource-server/src/main/java" "Resource Server - src/main/java" $true
$r.Category = $category; $results += $r

# Auth Client
$r = Test-DirectoryExists "auth-client/src/main/java" "Auth Client - src/main/java" $true
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# 6. SCRIPTS DE AUTOMAÇÃO
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "🔧 6. VERIFICANDO SCRIPTS DE AUTOMAÇÃO" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Scripts"

$r = Test-FileExists "build-robusto.bat" "Build Robusto - Script de Build" $true
$r.Category = $category; $results += $r

$r = Test-FileExists "demonstracao.bat" "Demonstração - Script de Demo" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "run-jmeter-tests.ps1" "JMeter - Script de Testes" $false
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# 7. MONITORAMENTO
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "📊 7. VERIFICANDO CONFIGURAÇÕES DE MONITORAMENTO" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Monitoramento"

$r = Test-FileExists "monitoring/prometheus/prometheus.yml" "Prometheus - Configuração Principal" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "monitoring/grafana/provisioning/datasources/prometheus.yml" "Grafana - Datasource Prometheus" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "monitoring/grafana/provisioning/dashboards/dashboard-provider.yml" "Grafana - Dashboard Provider" $false
$r.Category = $category; $results += $r

$r = Test-FileExists "monitoring/grafana/dashboards/open-finance-pqc-dashboard.json" "Grafana - Dashboard Principal" $false
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# 8. TESTES
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "🧪 8. VERIFICANDO TESTES" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Testes"

$r = Test-FileExists "jmeter/oauth2-complete-flow-test.jmx" "JMeter - Plano de Teste OAuth2" $false
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# 9. BANCO DE DADOS
# ============================================

Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host "🗄️ 9. VERIFICANDO SCRIPTS DE BANCO DE DADOS" -ForegroundColor $InfoColor
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor $InfoColor
Write-Host ""

$category = "Banco de Dados"

$r = Test-FileExists "init-scripts/01-create-databases.sql" "PostgreSQL - Criar Databases" $true
$r.Category = $category; $results += $r

$r = Test-FileExists "init-scripts/02-init-schema.sql" "PostgreSQL - Init Schema" $false
$r.Category = $category; $results += $r

Write-Host ""

# ============================================
# RELATÓRIO FINAL
# ============================================

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor $InfoColor
Write-Host "║                   RELATÓRIO FINAL                          ║" -ForegroundColor $InfoColor
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor $InfoColor
Write-Host ""

Write-Host "📊 ESTATÍSTICAS:" -ForegroundColor $InfoColor
Write-Host "   Total de Verificações: $totalChecks" -ForegroundColor White
Write-Host "   ✅ Aprovadas: $passedChecks" -ForegroundColor $SuccessColor
Write-Host "   ❌ Falhas: $failedChecks" -ForegroundColor $ErrorColor
Write-Host "   ⚠️  Avisos: $warnings" -ForegroundColor $WarningColor
Write-Host ""

$successRate = [math]::Round(($passedChecks / $totalChecks) * 100, 2)
Write-Host "   Taxa de Sucesso: $successRate%" -ForegroundColor $(if ($successRate -ge 80) { $SuccessColor } elseif ($successRate -ge 60) { $WarningColor } else { $ErrorColor })
Write-Host ""

# Status geral
if ($failedChecks -eq 0) {
    Write-Host "✅ PROJETO APROVADO!" -ForegroundColor $SuccessColor
    Write-Host "   Todos os componentes críticos estão presentes." -ForegroundColor $SuccessColor
    if ($warnings -gt 0) {
        Write-Host "   ⚠️  Existem $warnings componentes opcionais faltando." -ForegroundColor $WarningColor
    }
} else {
    Write-Host "❌ PROJETO COM PROBLEMAS!" -ForegroundColor $ErrorColor
    Write-Host "   Existem $failedChecks componentes críticos faltando." -ForegroundColor $ErrorColor
    Write-Host "   Revise os itens marcados com ❌ acima." -ForegroundColor $ErrorColor
}

Write-Host ""

# ============================================
# GERAR RELATÓRIO HTML
# ============================================

$htmlReport = @"
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Auditoria TCC - $(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            padding: 20px;
            color: #333;
        }
        .container {
            max-width: 1200px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        .header h1 { font-size: 2.5em; margin-bottom: 10px; }
        .header p { font-size: 1.2em; opacity: 0.9; }
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            padding: 30px;
            background: #f8f9fa;
        }
        .stat-card {
            background: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            text-align: center;
        }
        .stat-card h3 { color: #666; font-size: 0.9em; margin-bottom: 10px; }
        .stat-card .number {
            font-size: 2.5em;
            font-weight: bold;
            margin-bottom: 5px;
        }
        .stat-card.success .number { color: #28a745; }
        .stat-card.error .number { color: #dc3545; }
        .stat-card.warning .number { color: #ffc107; }
        .stat-card.info .number { color: #667eea; }
        .results {
            padding: 30px;
        }
        .category {
            margin-bottom: 30px;
        }
        .category h2 {
            font-size: 1.5em;
            color: #667eea;
            margin-bottom: 15px;
            padding-bottom: 10px;
            border-bottom: 2px solid #667eea;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #e0e0e0;
        }
        th {
            background: #f8f9fa;
            font-weight: 600;
            color: #666;
        }
        tr:hover { background: #f8f9fa; }
        .status-ok { color: #28a745; font-weight: bold; }
        .status-error { color: #dc3545; font-weight: bold; }
        .status-warning { color: #ffc107; font-weight: bold; }
        .footer {
            background: #f8f9fa;
            padding: 20px;
            text-align: center;
            color: #666;
            font-size: 0.9em;
        }
        .progress-bar {
            width: 100%;
            height: 30px;
            background: #e0e0e0;
            border-radius: 15px;
            overflow: hidden;
            margin: 20px 0;
        }
        .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #28a745, #20c997);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-weight: bold;
            transition: width 0.3s ease;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔍 Auditoria Completa - TCC</h1>
            <p>Implementação de Criptografia Pós-Quântica em Open Finance Brasil</p>
            <p style="font-size: 0.9em; margin-top: 10px;">$(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')</p>
        </div>
        
        <div class="stats">
            <div class="stat-card info">
                <h3>Total de Verificações</h3>
                <div class="number">$totalChecks</div>
            </div>
            <div class="stat-card success">
                <h3>Aprovadas</h3>
                <div class="number">$passedChecks</div>
            </div>
            <div class="stat-card error">
                <h3>Falhas</h3>
                <div class="number">$failedChecks</div>
            </div>
            <div class="stat-card warning">
                <h3>Avisos</h3>
                <div class="number">$warnings</div>
            </div>
        </div>
        
        <div style="padding: 0 30px;">
            <div class="progress-bar">
                <div class="progress-fill" style="width: $successRate%">$successRate%</div>
            </div>
        </div>
        
        <div class="results">
"@

# Agrupar resultados por categoria
$categories = $results | Group-Object -Property Category | Sort-Object Name

foreach ($cat in $categories) {
    $htmlReport += @"
            <div class="category">
                <h2>$($cat.Name)</h2>
                <table>
                    <thead>
                        <tr>
                            <th style="width: 40%">Item</th>
                            <th style="width: 40%">Caminho</th>
                            <th style="width: 10%">Status</th>
                            <th style="width: 10%">Crítico</th>
                        </tr>
                    </thead>
                    <tbody>
"@
    
    foreach ($item in $cat.Group) {
        $statusClass = switch -Regex ($item.Status) {
            "✅" { "status-ok" }
            "❌" { "status-error" }
            "⚠️" { "status-warning" }
            default { "" }
        }
        
        $criticalText = if ($item.Critical) { "Sim" } else { "Não" }
        
        $htmlReport += @"
                        <tr>
                            <td>$($item.Item)</td>
                            <td style="font-family: monospace; font-size: 0.9em;">$($item.Path)</td>
                            <td class="$statusClass">$($item.Status)</td>
                            <td>$criticalText</td>
                        </tr>
"@
    }
    
    $htmlReport += @"
                    </tbody>
                </table>
            </div>
"@
}

$htmlReport += @"
        </div>
        
        <div class="footer">
            <p><strong>Projeto:</strong> $ProjectPath</p>
            <p><strong>Gerado por:</strong> Script de Auditoria Automatizado</p>
        </div>
    </div>
</body>
</html>
"@

# Salvar relatório HTML
$reportPath = Join-Path $ProjectPath "auditoria-relatorio.html"
$htmlReport | Out-File -FilePath $reportPath -Encoding UTF8

Write-Host "📄 Relatório HTML gerado: $reportPath" -ForegroundColor $InfoColor
Write-Host ""
Write-Host "🌐 Abrindo relatório no navegador..." -ForegroundColor $InfoColor
Start-Process $reportPath

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor $InfoColor
Write-Host "║                  AUDITORIA CONCLUÍDA                       ║" -ForegroundColor $InfoColor
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor $InfoColor
Write-Host ""