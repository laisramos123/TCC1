 <!--
# 🔐 TCC Open Finance  + Segurança Pós-Quântica (Dilithium)

Sistema completo de Open Finance  implementando segurança pós-quântica com algoritmos Dilithium para assinatura digital e OAuth2 + OpenID Connect para autorização.

## 🏗️ Arquitetura do Sistema

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Auth Client   │───▶│  Auth Server   │───▶│ Resource Server │
│   (Port 8081)   │    │   (Port 8080)   │    │   (Port 8082)   │
│                 │    │                 │    │                 │
│ • Frontend      │    │                 │    │                 │
│/Back-end        │    │ • OAuth2/OIDC   │    │ • APIs Bancárias│
│ • Login OAuth2  │    │ • Dilithium     │    │ • Validação JWT │
│ • Dashboard     │    │ • JWT Tokens    │    │ • Contas/Trans. │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## ⚡ Execução do Sistema

### **🚀 Comando Único (Recomendado)**

```bash
.\build-robusto.bat
```

**Este comando executa automaticamente:**

- 📦 Compilação de todos os projetos Maven
- 🐳 Build e start dos containers Docker
- 🧪 Testes de funcionamento completos
- 🔐 Validação do algoritmo Dilithium
- 📊 Relatório final com URLs e comandos úteis

### **URLs após execução:**

- **💻 Frontend:** http://localhost:8081
- **🔐 Auth Server:** http://localhost:8080
- **📊 Resource APIs:** http://localhost:8082

---

## 🔧 Pré-requisitos

- ✅ Java 17+
- ✅ Maven 3.6+
- ✅ Docker Desktop
- ✅ Git

### Verificar instalação:

```bash
java --version
mvn --version
docker --version
docker-compose --version
```

---

## 🔧 Desenvolvimento e Manutenção

### **Para desenvolvimento diário:**

```bash
# Após modificar código, execute:
.\build-robusto.bat

# Ver logs específicos:
docker-compose logs -f auth-server
docker-compose logs -f resource-server
docker-compose logs -f auth-client

# Reiniciar serviço específico:
docker-compose restart auth-server

# Parar tudo:
docker-compose down
```

### **Estrutura de arquivos:**

```
TCC1/
├── build.bat          # Script principal (único necessário)
├── docker-compose.yml         # Orquestração dos containers
├── auth-server/              # OAuth2 + Dilithium
│   ├── Dockerfile
│   └── src/main/java/...
├── resource-server/          # APIs bancárias protegidas
│   ├── Dockerfile
│   └── src/main/java/...
└── auth-client/              # Frontend/Back-end OAuth2
    ├── Dockerfile
    └── src/main/java/...
```

---

## 🧪 APIs e Endpoints

### **🔐 Auth Server (8080) - OAuth2 + Dilithium**

| Endpoint                             | Método | Descrição                 |
| ------------------------------------ | ------ | ------------------------- |
| `/oauth2/authorize`                  | GET    | Autorização OAuth2        |
| `/oauth2/token`                      | POST   | Troca de tokens           |
| `/.well-known/openid_configuration`  | GET    | Configuração OIDC         |
| `/api/v1/dilithium/public/assinar`   | POST   | Assinatura Dilithium      |
| `/api/v1/dilithium/public/verificar` | POST   | Verificação de assinatura |
| `/api/v1/dilithium/info`             | GET    | Informações do algoritmo  |

### **📊 Resource Server (8082) - APIs Open Finance **

| Endpoint               | Método | Descrição                        |
| ---------------------- | ------ | -------------------------------- |
| `/api/v1/accounts`     | GET    | Listar contas (requer token)     |
| `/api/v1/transactions` | GET    | Listar transações (requer token) |
| `/api/v1/credit-cards` | GET    | Listar cartões (requer token)    |

### **💻 Auth Client (8081) - Frontend**

| Endpoint     | Método | Descrição              |
| ------------ | ------ | ---------------------- |
| `/`          | GET    | Página inicial         |
| `/login`     | GET    | Login OAuth2           |
| `/dashboard` | GET    | Dashboard (após login) |

---

## 🚨 Solução de Problemas

### **❌ Erro: "JARs não encontrados"**

```bash
# O script build-robusto.bat detecta e reporta automaticamente
# Se houver erro, execute com logs detalhados:
cd auth-server && mvn clean package -DskipTests -X
```

### **❌ Erro: "Containers não sobem"**

```bash
# Verificar logs dos containers:
docker-compose logs auth-server

# Verificar se portas estão ocupadas:
netstat -an | findstr "8080"
netstat -an | findstr "8081"
netstat -an | findstr "8082"
```

### **❌ Erro: "Health checks falhando"**

```bash
# Aguardar mais tempo (aplicações Spring demoram para subir)
# O script já aguarda 60 segundos automaticamente
# Para forçar verificação manual:
curl http://localhost:8080/actuator/health
```

---

## 🎯 Demonstração para Banca

### **Cenário 1: Sistema Completo em 1 Comando**

1. Execute: `.\build-robusto.bat`
2. Aguarde o relatório final (~3-4 minutos)
3. Acesse: http://localhost:8081

### **Cenário 2: Fluxo OAuth2 Open Finance **

1. Acessar http://localhost:8081
2. Clicar em "Login"
3. Ser redirecionado para autorização OAuth2
4. Aprovar permissões bancárias
5. Ver dashboard com dados das APIs

### **Cenário 3: Segurança Pós-Quântica Dilithium**

```bash
# Testar assinatura digital pós-quântica:
curl -X POST http://localhost:8080/api/v1/dilithium/public/assinar \
  -H "Content-Type: application/json" \
  -d '{"data": "Demonstração Banca TCC"}'

# Ver especificações do algoritmo:
curl http://localhost:8080/api/v1/dilithium/info
```

---

## 📊 Métricas e Observabilidade

### **Monitoramento disponível:**

- `GET /actuator/health` - Status das aplicações
- `GET /actuator/metrics` - Métricas detalhadas
- `GET /actuator/info` - Informações de build
- `docker-compose logs -f` - Logs em tempo real

### **Performance Dilithium:**

```bash
# Medir tempo de assinatura:
time curl -X POST http://localhost:8080/api/v1/dilithium/public/assinar \
  -H "Content-Type: application/json" \
  -d '{"data": "performance test"}'
```

---

## 📝 Notas Técnicas

### **Algoritmo Dilithium:**

- **Nível de Segurança:** 3 (192-bit equivalent)
- **Tipo:** Assinatura digital pós-quântica
- **Padrão:** NIST Post-Quantum Cryptography
- **Resistência:** Computadores quânticos (Algoritmo de Shor)

### **OAuth2/OpenID Connect:**

- **Flow:** Authorization Code + PKCE
- **Tokens:** JWT assinados com Dilithium
- **Scopes:** `openid`, `accounts`, `transactions`, `credit-cards`
- **Compliance:** Open Finance  Brasil

### **Tecnologias:**

- **Backend:** Spring Boot 3.x + Spring Security 6.x
- **Banco:** H2 (desenvolvimento) / PostgreSQL (produção)
- **Containers:** Docker + Docker Compose
- **Build:** Maven 3.6+, Java 17+
- **Criptografia:** Bouncy Castle + Dilithium

---

**Este sistema demonstra:**

- ✅ **Implementação completa** de Open Finance  Brasil
- ✅ **Segurança pós-quântica** com algoritmo Dilithium
- ✅ **OAuth2 + OpenID Connect** padrão da indústria
- ✅ **Arquitetura microserviços** containerizada
- ✅ **APIs RESTful** documentadas e testáveis
- ✅ **Observabilidade** com health checks e métricas
- ✅ **Deploy automatizado** em um único comando

**Comando para demonstração da banca:** `.\build-robusto.bat` ⚡

**Sistema completo funcionando em ~3 minutos!** 🚀🎓
-->
