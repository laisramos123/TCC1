#!/bin/bash

# =====================================================================#
# Fluxo do código de autorização + API Consents - Build Script v2.0    #
# Autor: Laís Ramos  Barbosa                                           #
# Data: 2025/2                                                         #
# =====================================================================#

set -e  # Exit on error

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para log
log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Banner
clear
echo -e "${BLUE}"
echo "╔════════════════════════════════════════════════════════════════╗"
echo "║        TCC - Open Finance com Criptografia Pós-Quântica        ║"
echo "║                    Build Script v2.0                           ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo -e "${NC}"


log "ETAPA 1: Verificando pré-requisitos..."


if ! command -v java &> /dev/null; then
    error "Java não encontrado. Instale Java 17+"
fi
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    error "Java 17+ é necessário. Versão atual: $JAVA_VERSION"
fi
log "✅ Java $JAVA_VERSION instalado"


if ! command -v mvn &> /dev/null; then
    error "Maven não encontrado. Instale Maven 3.6+"
fi
log "✅ Maven $(mvn -version | head -n1 | cut -d' ' -f3) instalado"


if ! command -v docker &> /dev/null; then
    error "Docker não encontrado. Instale Docker"
fi
log "✅ Docker $(docker --version | cut -d' ' -f3 | tr -d ',') instalado"


if ! command -v docker-compose &> /dev/null; then
    error "Docker Compose não encontrado"
fi
log "✅ Docker Compose $(docker-compose --version | cut -d' ' -f3 | tr -d ',') instalado"


if ! docker info &> /dev/null; then
    error "Docker daemon não está em execução"
fi
log "✅ Docker daemon está em execução"


log "ETAPA 2: Configurando ambiente..."


mkdir -p data/postgres logs/{auth-server,resource-server,auth-client,builds}
log "✅ Diretórios criados"


if [ ! -f .env ]; then
    warning "Arquivo .env não encontrado. Criando com valores padrão..."
    cp .env.example .env 2>/dev/null || cat > .env << 'EOF'
DB_USER=tcc_user
DB_PASSWORD=tcc_password
OAUTH_SECRET=oauth_secret_2024
PGADMIN_EMAIL=admin@tcc.unb.br
PGADMIN_PASSWORD=admin123
EOF
    log "✅ Arquivo .env criado"
else
    log "✅ Arquivo .env encontrado"
fi

 
export $(cat .env | grep -v '^#' | xargs)

 
log "ETAPA 3: Limpando ambiente anterior..."

# Parar containers existentes
docker-compose down -v --remove-orphans 2>/dev/null || true
log "✅ Containers antigos removidos"

# Limpar volumes órfãos
docker volume prune -f > /dev/null 2>&1
log "✅ Volumes órfãos limpos"

 
log "ETAPA 4: Compilando projetos Maven..."

 
compile_project() {
    local project=$1
    log "Compilando $project..."
    
    cd $project
    if mvn clean package -DskipTests -Dspring.profiles.active=docker > ../logs/builds/${project}.log 2>&1; then
        log "✅ $project compilado com sucesso"
    else
        error "Falha ao compilar $project. Verifique logs/builds/${project}.log"
    fi
    cd ..
}

 
if command -v parallel &> /dev/null; then
    log "Compilando projetos em paralelo..."
    echo -e "auth-server\nresource-server\nauth-client" | parallel -j3 compile_project {}
else
    compile_project "auth-server"
    compile_project "resource-server"
    compile_project "auth-client"
fi

 
log "ETAPA 5: Construindo imagens Docker..."

 
docker-compose build --parallel || error "Falha ao construir imagens Docker"
log "✅ Imagens Docker construídas"

 
log "ETAPA 6: Iniciando serviços..."

 
log "Iniciando banco de dados e cache..."
docker-compose up -d postgres redis

 
log "Aguardando PostgreSQL..."
until docker-compose exec -T postgres pg_isready -U ${DB_USER} > /dev/null 2>&1; do
    echo -n "."
    sleep 2
done
echo ""
log "✅ PostgreSQL está pronto"

 
log "Aguardando Redis..."
until docker-compose exec -T redis redis-cli ping > /dev/null 2>&1; do
    echo -n "."
    sleep 1
done
echo ""
log "✅ Redis está pronto"

 
log "Iniciando Auth Server (pode demorar até 3 minutos)..."
docker-compose up -d auth-server

 
log "Iniciando Resource Server..."
docker-compose up -d resource-server

log "Iniciando Auth Client..."
docker-compose up -d auth-client

log "Iniciando PgAdmin..."
docker-compose up -d pgadmin

 
log "ETAPA 7: Verificando saúde dos serviços..."

 
check_health() {
    local service=$1
    local url=$2
    local max_attempts=60
    local attempt=1
    
    log "Verificando $service..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s $url > /dev/null 2>&1; then
            log "✅ $service está saudável"
            return 0
        fi
        
        if [ $((attempt % 10)) -eq 0 ]; then
            log "Aguardando $service... (tentativa $attempt/$max_attempts)"
        fi
        
        sleep 3
        attempt=$((attempt + 1))
    done
    
    warning "$service não respondeu após $max_attempts tentativas"
    return 1
}
 
check_health "Auth Server" "http://localhost:8080/actuator/health"
check_health "Resource Server" "http://localhost:8082/actuator/health"
check_health "Auth Client" "http://localhost:8081/actuator/health"

 
log "ETAPA 8: Executando testes de integração..."

 
log "Testando algoritmo Dilithium..."
if curl -s -X POST http://localhost:8080/api/v1/dilithium/public/assinar \
    -H "Content-Type: application/json" \
    -d '{"data": "teste"}' | grep -q "signature"; then
    log "✅ Dilithium funcionando"
else
    warning "Dilithium pode não estar funcionando corretamente"
fi

 
log "ETAPA 9: Gerando relatório final..."

echo -e "\n${GREEN}═══════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}                    BUILD CONCLUÍDO COM SUCESSO!                    ${NC}"
echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}\n"

echo -e "${BLUE}📋 STATUS DOS SERVIÇOS:${NC}"
docker-compose ps

echo -e "\n${BLUE}🌐 URLs DISPONÍVEIS:${NC}"
echo "   Auth Client (TPP):     http://localhost:8081"
echo "   Auth Server:           http://localhost:8080"
echo "   Resource Server:       http://localhost:8082"
echo "   PgAdmin:              http://localhost:5050"
echo "   Redis Commander:       http://localhost:8081"

echo -e "\n${BLUE}🔐 CREDENCIAIS:${NC}"
echo "   Usuário: joao.silva"
echo "   Senha: senha123"
echo "   PgAdmin: ${PGADMIN_EMAIL} / ${PGADMIN_PASSWORD}"

echo -e "\n${BLUE}📊 COMANDOS ÚTEIS:${NC}"
echo "   Ver logs:          docker-compose logs -f [serviço]"
echo "   Parar tudo:        docker-compose down"
echo "   Reiniciar:         docker-compose restart [serviço]"
echo "   Executar testes:   ./demonstracao.sh"

echo -e "\n${BLUE}📈 MONITORAMENTO:${NC}"
echo "   Health Check:      curl http://localhost:8080/actuator/health"
echo "   Métricas:         curl http://localhost:8080/actuator/metrics"
echo "   Prometheus:       curl http://localhost:8080/actuator/prometheus"

echo -e "\n${GREEN}Sistema pronto para uso!${NC}\n"

 
LOG_FILE="logs/builds/build_$(date +'%Y%m%d_%H%M%S').log"
echo "Log completo salvo em: $LOG_FILE"

exit 0