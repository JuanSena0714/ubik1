#!/bin/bash

# Script para iniciar todos los microservicios del proyecto
# Base de datos: PostgreSQL
# Autor: Claude
# Uso: ./start-services.sh

set -e

echo "🚀 Iniciando Microservicios Reactivos (PostgreSQL)..."
echo "======================================================"

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Función para verificar si un puerto está en uso
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1 ; then
        echo -e "${RED}❌ Puerto $port ya está en uso${NC}"
        echo "   Ejecuta: lsof -i :$port para ver qué proceso lo está usando"
        return 1
    else
        echo -e "${GREEN}✅ Puerto $port disponible${NC}"
        return 0
    fi
}

# Función para verificar PostgreSQL
check_postgres() {
    echo ""
    echo "🔍 Verificando conexión a PostgreSQL..."
    
    # Intentar conectar a PostgreSQL
    if PGPASSWORD=postgres psql -U postgres -h localhost -c "SELECT 1" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PostgreSQL está corriendo${NC}"
        
        # Verificar si existe la base de datos
        if PGPASSWORD=postgres psql -U postgres -h localhost -lqt | cut -d \| -f 1 | grep -qw motel_management_db; then
            echo -e "${GREEN}✅ Base de datos motel_management_db existe${NC}"
        else
            echo -e "${YELLOW}⚠️  Base de datos motel_management_db no existe${NC}"
            echo "   Ejecuta: psql -U postgres -f postgres-init-motel.sql"
            return 1
        fi
        return 0
    else
        echo -e "${RED}❌ No se puede conectar a PostgreSQL${NC}"
        echo "   Asegúrate de que PostgreSQL esté corriendo"
        echo "   Comandos útiles:"
        echo "     Linux: sudo systemctl start postgresql"
        echo "     Mac: brew services start postgresql"
        echo "     Docker: docker run --name postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres"
        return 1
    fi
}

# Verificar puertos
echo ""
echo "🔍 Verificando puertos disponibles..."
check_port 8080 || exit 1  # Gateway
check_port 8082 || exit 1  # Products (si está en tu proyecto)
check_port 8083 || exit 1  # Motel Management

# Verificar PostgreSQL
check_postgres || exit 1

# Compilar proyecto
echo ""
echo "📦 Compilando proyecto..."
mvn clean install -DskipTests

echo ""
echo "🎯 Iniciando servicios..."
echo ""

# Crear directorio para logs
mkdir -p logs

# Función para iniciar un servicio
start_service() {
    local service_name=$1
    local port=$2
    local module=$3
    
    echo -e "${YELLOW}🔄 Iniciando $service_name en puerto $port...${NC}"
    
    # Iniciar el servicio en background
    mvn -pl $module spring-boot:run > logs/$service_name.log 2>&1 &
    local pid=$!
    
    # Guardar el PID
    echo $pid > logs/$service_name.pid
    
    echo -e "${GREEN}   ✅ $service_name iniciado (PID: $pid)${NC}"
    echo "   📝 Log: logs/$service_name.log"
    
    # Esperar un momento antes de iniciar el siguiente
    sleep 3
}

# Iniciar servicios en orden
start_service "Gateway" "8080" "gateway"
# Si tienes el microservicio products, descomenta la siguiente línea
# start_service "Products" "8082" "products"
start_service "Motel-Management" "8083" "motelManegement"

echo ""
echo "=============================================="
echo -e "${GREEN}✅ Todos los servicios están iniciando${NC}"
echo "=============================================="
echo ""
echo "📊 Estado de los servicios:"
echo ""
echo "   🌐 Gateway:           http://localhost:8080"
echo "   🏨 Motel Management:  http://localhost:8083"
echo ""
echo "🔍 Health checks:"
echo "   curl http://localhost:8080/actuator/health"
echo "   curl http://localhost:8083/actuator/health"
echo ""
echo "📝 Logs en tiempo real:"
echo "   tail -f logs/Gateway.log"
echo "   tail -f logs/Motel-Management.log"
echo ""
echo "🛑 Para detener los servicios:"
echo "   ./stop-services.sh"
echo ""

# Esperar a que los servicios estén listos
echo "⏳ Esperando que los servicios estén listos (esto puede tomar 30-60 segundos)..."
sleep 10

# Verificar health de cada servicio
check_health() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:$port/actuator/health > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name está listo${NC}"
            return 0
        fi
        
        echo "   Intento $attempt/$max_attempts: $service_name no está listo aún..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name no respondió después de $max_attempts intentos${NC}"
    echo "   Revisa el log: logs/$service_name.log"
    return 1
}

echo ""
check_health "Gateway" "8080"
check_health "Motel-Management" "8083"

echo ""
echo "=============================================="
echo -e "${GREEN}🎉 ¡Sistema completamente operativo!${NC}"
echo "=============================================="
echo ""
echo "🧪 Prueba algunos endpoints:"
echo ""
echo "   # Listar moteles"
echo "   curl http://localhost:8080/api/motels"
echo ""
echo "   # Listar habitaciones"
echo "   curl http://localhost:8080/api/rooms"
echo ""
echo "   # Listar servicios"
echo "   curl http://localhost:8080/api/services"
echo ""
