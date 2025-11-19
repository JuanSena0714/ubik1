#!/bin/bash

# Script para iniciar todo el sistema (Base de datos + Aplicación) en Docker

set -e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "🚀 Iniciando Sistema Completo con Docker..."
echo "=============================================="

# Verificar si Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker no está corriendo. Por favor, inicia Docker Desktop.${NC}"
    exit 1
fi

# Limpiar contenedores anteriores si existen
echo ""
echo "🧹 Limpiando contenedores anteriores..."
docker-compose -f docker-compose-full.yml down

# Construir y iniciar servicios
echo ""
echo "🔨 Construyendo imagen de la aplicación..."
docker-compose -f docker-compose-full.yml build

echo ""
echo "🚀 Iniciando servicios..."
docker-compose -f docker-compose-full.yml up -d

# Esperar a que PostgreSQL esté listo
echo ""
echo "⏳ Esperando a que PostgreSQL esté listo..."
max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if docker exec motel-postgres-db pg_isready -U postgres -d motel_management_db > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PostgreSQL está listo${NC}"
        break
    fi
    
    echo "   Intento $attempt/$max_attempts..."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo -e "${RED}❌ PostgreSQL no respondió${NC}"
    exit 1
fi

# Esperar a que la aplicación esté lista
echo ""
echo "⏳ Esperando a que la aplicación esté lista..."
sleep 10

max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if curl -s http://localhost:8083/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Aplicación está lista${NC}"
        break
    fi
    
    echo "   Intento $attempt/$max_attempts..."
    sleep 3
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo -e "${RED}❌ La aplicación no respondió${NC}"
    echo "Revisa los logs: docker logs motel-management-app"
    exit 1
fi

echo ""
echo "=============================================="
echo -e "${GREEN}🎉 ¡Sistema completamente operativo!${NC}"
echo "=============================================="
echo ""
echo "📊 Servicios disponibles:"
echo ""
echo "   🏨 API Motel Management:"
echo "      http://localhost:8083"
echo ""
echo "   🔍 Health Check:"
echo "      http://localhost:8083/actuator/health"
echo ""
echo "   🌐 Adminer (Base de datos):"
echo "      http://localhost:8081"
echo "      Sistema: PostgreSQL"
echo "      Servidor: postgres"
echo "      Usuario: postgres"
echo "      Password: carlosmanuel"
echo ""
echo "🧪 Prueba algunos endpoints:"
echo ""
echo "   # Health check"
echo "   curl http://localhost:8083/actuator/health"
echo ""
echo "   # Listar moteles"
echo "   curl http://localhost:8083/api/motels"
echo ""
echo "   # Listar habitaciones"
echo "   curl http://localhost:8083/api/rooms"
echo ""
echo "   # Listar servicios"
echo "   curl http://localhost:8083/api/services"
echo ""
echo "🔍 Ver logs:"
echo "   docker logs -f motel-management-app"
echo "   docker logs -f motel-postgres-db"
echo ""
echo "🛑 Detener servicios:"
echo "   docker-compose -f docker-compose-full.yml down"
echo ""
echo "🗑️  Eliminar todo (incluyendo datos):"
echo "   docker-compose -f docker-compose-full.yml down -v"
echo ""