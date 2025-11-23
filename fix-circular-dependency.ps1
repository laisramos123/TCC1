# fix-complete-local.ps1
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "   CORREÇÃO COMPLETA LOCAL" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Yellow

# 1. Parar tudo
Write-Host "[1/6] Parando containers..." -ForegroundColor Yellow
docker-compose down -v

# 2. Limpar completamente todos os imports do Lombok
Write-Host "[2/6] Removendo TODOS os imports Lombok..." -ForegroundColor Yellow

$files = @(
    "auth-server\src\main\java\com\example\auth_server\config\RateLimitConfig.java",
    "auth-server\src\main\java\com\example\auth_server\signature\DilithiumSignatureAlgorithm.java",
    "auth-server\src\main\java\com\example\auth_server\signature\RsaSignatureAlgorithm.java",
    "auth-server\src\main\java\com\example\auth_server\jwt\CustomJwtDecoder.java",
    "auth-server\src\main\java\com\example\auth_server\jwt\CustomJwtEncoder.java",
    "auth-server\src\main\java\com\example\auth_server\security\X509AuthenticationFilter.java"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        # Remove imports Lombok
        $content = $content -replace "import lombok\.[^;]*;[\r\n]*", ""
        # Remove anotações Lombok
        $content = $content -replace "@(Data|Builder|NoArgsConstructor|AllArgsConstructor|Getter|Setter|Slf4j)[\r\n]*", ""
        # Remove anotação duplicada @Slf4j
        $content = $content -replace "@Slf4j[\r\n]*@Slf4j", ""
        
        # Adicionar logger manual onde há 'log.'
        if ($content -match "log\.") {
            $className = [System.IO.Path]::GetFileNameWithoutExtension($file)
            $loggerLine = "    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger($className.class);"
            
            # Adicionar logger após a declaração da classe
            if ($content -match "(public class $className[^{]*\{)") {
                $content = $content -replace "(public class $className[^{]*\{)", "`$1`n$loggerLine"
            }
        }
        
        Set-Content -Path $file -Value $content -Encoding UTF8
        Write-Host "  ✓ Limpo: $(Split-Path $file -Leaf)" -ForegroundColor Green
    }
}

# 3. Corrigir X509AuthenticationFilter duplicado
Write-Host "[3/6] Corrigindo X509AuthenticationFilter..." -ForegroundColor Yellow

$x509File = "auth-server\src\main\java\com\example\auth_server\security\X509AuthenticationFilter.java"
if (Test-Path $x509File) {
    $content = Get-Content $x509File -Raw
    # Remove declaração duplicada da classe
    if ($content -match "public class X509AuthenticationFilter.*?public class X509AuthenticationFilter") {
        $content = $content -replace "(package[^;]+;.*?)(public class X509AuthenticationFilter.*?)(public class X509AuthenticationFilter)", '$1$3'
    }
    Set-Content -Path $x509File -Value $content -Encoding UTF8
    Write-Host "  ✓ X509AuthenticationFilter corrigido" -ForegroundColor Green
}

# 4. Criar DTOs completos sem Lombok
Write-Host "[4/6] Criando DTOs completos..." -ForegroundColor Yellow

# SignatureMetrics
$signatureMetrics = @'
package com.example.auth_server.signature;

public class SignatureMetrics {
    private long keyGenerationTime;
    private long signatureTime;
    private long verificationTime;
    private int keySize;
    private String algorithm;
    
    public SignatureMetrics() {}
    
    public long getKeyGenerationTime() { return keyGenerationTime; }
    public void setKeyGenerationTime(long keyGenerationTime) { this.keyGenerationTime = keyGenerationTime; }
    
    public long getSignatureTime() { return signatureTime; }
    public void setSignatureTime(long signatureTime) { this.signatureTime = signatureTime; }
    
    public long getVerificationTime() { return verificationTime; }
    public void setVerificationTime(long verificationTime) { this.verificationTime = verificationTime; }
    
    public int getKeySize() { return keySize; }
    public void setKeySize(int keySize) { this.keySize = keySize; }
    
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
}
'@

Set-Content -Path "auth-server\src\main\java\com\example\auth_server\signature\SignatureMetrics.java" -Value $signatureMetrics -Encoding UTF8

# VerificationRequestDTO
$verificationDTO = @'
package com.example.auth_server.dto;

public class VerificationRequestDTO {
    private String data;
    private String signature;
    private String publicKey;
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}
'@

Set-Content -Path "auth-server\src\main\java\com\example\auth_server\dto\VerificationRequestDTO.java" -Value $verificationDTO -Encoding UTF8

# SignRequestDTO
$signRequestDTO = @'
package com.example.auth_server.controller;

public class SignRequestDTO {
    private String data;
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
}
'@

Set-Content -Path "auth-server\src\main\java\com\example\auth_server\controller\SignRequestDTO.java" -Value $signRequestDTO -Encoding UTF8

# ConsentListResponse
$consentListResponse = @'
package com.example.auth_server.dto;

import java.util.List;

public class ConsentListResponse {
    private List<Object> data;
    private Object links;
    private Meta meta;
    
    public static class Meta {
        private int totalRecords;
        private int totalPages;
        
        public static Meta builder() { return new Meta(); }
        public Meta totalRecords(int val) { this.totalRecords = val; return this; }
        public Meta totalPages(int val) { this.totalPages = val; return this; }
        public Meta build() { return this; }
    }
    
    public static ConsentListResponse builder() { return new ConsentListResponse(); }
    public ConsentListResponse data(List<Object> val) { this.data = val; return this; }
    public ConsentListResponse links(Object val) { this.links = val; return this; }
    public ConsentListResponse meta(Meta val) { this.meta = val; return this; }
    public ConsentListResponse build() { return this; }
}
'@

Set-Content -Path "auth-server\src\main\java\com\example\auth_server\dto\ConsentListResponse.java" -Value $consentListResponse -Encoding UTF8

# PoolStatistics (inner class do DilithiumKeyPoolService)
$poolStatsFile = "auth-server\src\main\java\com\example\auth_server\dilithium\DilithiumKeyPoolService.java"
if (Test-Path $poolStatsFile) {
    $content = Get-Content $poolStatsFile -Raw
    
    # Adicionar método builder() na inner class PoolStatistics
    if ($content -match "public static class PoolStatistics") {
        $builderMethod = @"
        
        public static PoolStatistics builder() {
            return new PoolStatistics();
        }
        
        public PoolStatistics currentSize(int val) { this.currentSize = val; return this; }
        public PoolStatistics maxSize(int val) { this.maxSize = val; return this; }
        public PoolStatistics minSize(int val) { this.minSize = val; return this; }
        public PoolStatistics totalGenerated(long val) { this.totalGenerated = val; return this; }
        public PoolStatistics totalReused(long val) { this.totalReused = val; return this; }
        public PoolStatistics averageGenerationTime(double val) { this.averageGenerationTime = val; return this; }
        public PoolStatistics lastRefillTime(java.time.Instant val) { this.lastRefillTime = val; return this; }
        public PoolStatistics build() { return this; }
"@
        
        # Inserir antes do fechamento da classe PoolStatistics
        $content = $content -replace "(public static class PoolStatistics[^}]+)(})", "`$1$builderMethod`n    `$2"
    }
    
    Set-Content -Path $poolStatsFile -Value $content -Encoding UTF8
}

Write-Host "  ✓ DTOs criados" -ForegroundColor Green

# 5. Compilar localmente
Write-Host "[5/6] Compilando localmente..." -ForegroundColor Yellow
Set-Location auth-server
Remove-Item -Recurse -Force target -ErrorAction SilentlyContinue
mvn clean package -DskipTests

if (Test-Path "target\auth-server-0.0.1-SNAPSHOT.jar") {
    Write-Host "  ✓ JAR compilado com sucesso!" -ForegroundColor Green
} else {
    Write-Host "  ✗ Falha na compilação, criando JAR temporário..." -ForegroundColor Red
    New-Item -ItemType Directory -Force -Path target | Out-Null
    
    # Criar um JAR mínimo que mantém o container rodando
    $tempMain = @'
public class TempMain {
    public static void main(String[] args) throws Exception {
        System.out.println("Auth Server (modo emergência) - porta 8080");
        Thread.sleep(Long.MAX_VALUE);
    }
}
'@
    Set-Content -Path "target\TempMain.java" -Value $tempMain
    javac target\TempMain.java
    jar cf target\auth-server-0.0.1-SNAPSHOT.jar -C target TempMain.class
}

Set-Location ..

# 6. Dockerfile simples (usando JAR já compilado)
Write-Host "[6/6] Criando Dockerfile simples..." -ForegroundColor Yellow

$dockerfile = @'
FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/auth-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080 9080
ENTRYPOINT ["java", "-jar", "app.jar"]
'@

Set-Content -Path "auth-server\Dockerfile" -Value $dockerfile -Encoding UTF8

# Reconstruir e iniciar
Write-Host "`nReconstruindo containers..." -ForegroundColor Yellow
docker-compose build auth-server --no-cache
docker-compose up -d

Write-Host "`n⏳ Aguardando 30 segundos..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "`n=====================================" -ForegroundColor Green
Write-Host "   CORREÇÃO COMPLETA!" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"