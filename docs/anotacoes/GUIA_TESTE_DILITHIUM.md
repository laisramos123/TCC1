# 🧪 Guia Passo a Passo - Testando Assinatura Dilithium

## 📋 Pré-requisitos

### 1. **Verificar Dependências no pom.xml**

Primeiro, certifique-se de que o Bouncy Castle está no `pom.xml` do auth-server:

```xml
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.77</version>
</dependency>
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcpkix-jdk18on</artifactId>
    <version>1.77</version>
</dependency>
```

## 🚀 Passo 1: Executar a Aplicação

### **1.1 Compilar o Projeto**

```bash
cd c:\Users\laisr\IdeaProjects\TCC1\auth-server
mvn clean compile
```

### **1.2 Executar a Aplicação**

```bash
mvn spring-boot:run
```

**Resultado Esperado:**

- A aplicação deve iniciar na porta 8080
- Você verá logs dos testes Dilithium no console automaticamente
- Se houver erro, verifique as dependências

## 📊 Passo 2: Analisar Saída do Console

Quando a aplicação iniciar, você verá automaticamente:

```
============================================================
🔐 TESTE DE CRIPTOGRAFIA PÓS-QUÂNTICA DILITHIUM
============================================================

=== Comparação de Níveis de Segurança Dilithium ===

--- DILITHIUM2 (128-bit) ---
Tempo geração de chaves: 15 ms
Tempo de assinatura: 8 ms
Tempo de verificação: 12 ms
Tamanho chave pública: 1312 bytes
Tamanho chave privada: 2528 bytes
Tamanho da assinatura: 2420 bytes
Assinatura válida: ✅ SIM
Parâmetro usado: dilithium2

--- DILITHIUM3 (192-bit) - RECOMENDADO ---
Tempo geração de chaves: 18 ms
Tempo de assinatura: 10 ms
Tempo de verificação: 15 ms
Tamanho chave pública: 1952 bytes
Tamanho chave privada: 4000 bytes
Tamanho da assinatura: 3293 bytes
Assinatura válida: ✅ SIM
Parâmetro usado: dilithium3

[... mais resultados]
```

## 🌐 Passo 3: Testar via Endpoints REST

### **3.1 Testar Endpoint de Informações (GET)**

```bash
curl -X GET http://localhost:8080/api/v1/dilithium/info
```

**Resposta Esperada:**

```json
{
  "algorithm": "Dilithium",
  "securityLevel": "192-bit (Level 3)",
  "quantumResistant": true,
  "standardized": "NIST PQC Standard"
}
```

### **3.2 Testar Assinatura de Dados (POST)**

```bash
curl -X POST http://localhost:8080/api/v1/dilithium/assinar \
  -H "Content-Type: application/json" \
  -d '{
    "data": "Meu texto para assinar com Dilithium"
  }'
```

**Resposta Esperada:**

```json
{
  "signature": "MEUCIQDx3...[base64 longo]",
  "publicKey": "MFkwEwYH...[base64 longo]",
  "algorithm": "Dilithium3",
  "timestamp": 1694234567890
}
```

### **3.3 Copiar e Salvar os Dados**

Da resposta anterior, copie:

- `signature` (a assinatura)
- `publicKey` (a chave pública)

### **3.4 Testar Verificação da Assinatura**

```bash
curl -X POST http://localhost:8080/api/v1/dilithium/verificar-assinatura \
  -H "Content-Type: application/json" \
  -d '{
    "data": "TWV1IHRleHRvIHBhcmEgYXNzaW5hciBjb20gRGlsaXRoaXVt",
    "signature": "[COLE_AQUI_A_SIGNATURE_DO_PASSO_ANTERIOR]",
    "publicKey": "[COLE_AQUI_A_PUBLIC_KEY_DO_PASSO_ANTERIOR]"
  }'
```

**Nota:** O campo `data` deve estar em Base64. "Meu texto para assinar com Dilithium" em Base64 é "TWV1IHRleHRvIHBhcmEgYXNzaW5hciBjb20gRGlsaXRoaXVt"

## 🧮 Passo 4: Converter Texto para Base64

### **4.1 Usando PowerShell (Windows)**

```powershell
[Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("Meu texto para assinar"))
```

### **4.2 Usando Bash (Linux/Mac)**

```bash
echo -n "Meu texto para assinar" | base64
```

### **4.3 Usando Site Online**

- Acesse: https://www.base64encode.org/
- Cole seu texto
- Copie o resultado

## 📱 Passo 5: Teste com Postman

### **5.1 Importar Coleção**

Crie uma nova coleção no Postman com estas requisições:

**Request 1: Obter Info**

- Method: GET
- URL: `http://localhost:8080/api/v1/dilithium/info`

**Request 2: Assinar**

- Method: POST
- URL: `http://localhost:8080/api/v1/dilithium/assinar`
- Headers: `Content-Type: application/json`
- Body (raw JSON):

```json
{
  "data": "Teste do Postman com Dilithium"
}
```

**Request 3: Verificar**

- Method: POST
- URL: `http://localhost:8080/api/v1/dilithium/verificar-assinatura`
- Headers: `Content-Type: application/json`
- Body: (use os dados da resposta do Request 2)

## 🔍 Passo 6: Testar JWT Específico do Open Banking

### **6.1 Endpoint Dedicado**

```bash
curl -X POST "http://localhost:8080/api/v1/dilithium/verificar-token?token=eyJhbGciOiJEaWxpdGhpdW0zIiwidHlwIjoiSldUIn0.eyJjb25zZW50X2lkIjoidXJuOmJhbms6Y29uc2VudDoxMjM0NSIsImNsaWVudF9pZCI6Im9hdXRoLWNsaWVudCIsInNjb3BlIjoiYWNjb3VudHM6cmVhZCIsInN0YXR1cyI6IkFXQUlUSU5HX0FVVEhPUklTQVRJT04ifQ.fake-signature"
```

## 🐛 Passo 7: Solução de Problemas

### **7.1 Erro: "Provider BCPQC not found"**

**Solução:**

1. Verificar dependências Bouncy Castle no pom.xml
2. Recompilar: `mvn clean compile`
3. Verificar versão Java (deve ser 17+)

### **7.2 Erro: "NoSuchAlgorithmException: Dilithium"**

**Solução:**

1. Verificar se BouncyCastleProvider está registrado
2. Adicionar no main ou configuração:

```java
Security.addProvider(new BouncyCastleProvider());
```

### **7.3 Aplicação não inicia**

**Solução:**

1. Verificar porta 8080 livre: `netstat -an | findstr :8080`
2. Verificar logs de erro
3. Testar com porta diferente: `--server.port=8081`

## 📊 Passo 8: Interpretar Resultados

### **8.1 Métricas de Performance**

- **Tempo de geração de chaves:** < 50ms é bom
- **Tempo de assinatura:** < 20ms é excelente
- **Tempo de verificação:** < 30ms é adequado

### **8.2 Tamanhos Esperados**

| Algoritmo  | Chave Pública | Chave Privada | Assinatura   |
| ---------- | ------------- | ------------- | ------------ |
| Dilithium2 | ~1,312 bytes  | ~2,528 bytes  | ~2,420 bytes |
| Dilithium3 | ~1,952 bytes  | ~4,000 bytes  | ~3,293 bytes |
| Dilithium5 | ~2,592 bytes  | ~4,864 bytes  | ~4,595 bytes |

### **8.3 Comparação com RSA**

- Dilithium3 é ~7x maior que RSA-2048
- Mas é resistente a computadores quânticos
- Performance similar ou melhor que RSA-2048

## ✅ Checklist de Teste Completo

- [ ] Aplicação inicia sem erros
- [ ] Logs mostram testes automáticos de Dilithium
- [ ] Endpoint `/info` responde corretamente
- [ ] Endpoint `/assinar` gera assinatura
- [ ] Endpoint `/verificar-assinatura` valida corretamente
- [ ] Todos os 3 níveis (2, 3, 5) funcionam
- [ ] Performance está dentro do esperado
- [ ] Assinaturas são sempre diferentes para mesmos dados (randomização)

## 🎯 Próximos Passos

Após confirmar que tudo funciona:

1. Integrar com sistema OAuth2 do projeto
2. Implementar cache de chaves para performance
3. Adicionar logs de auditoria
4. Configurar para produção com chaves persistentes
5. Implementar rotação de chaves

---

**💡 Dica:** Execute primeiro os testes automáticos (console) para verificar se a biblioteca está funcionando, depois teste os endpoints REST!
