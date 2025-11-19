#!/bin/bash

# Script para iniciar solo la base de datos PostgreSQL en Docker
# Útil para desarrollo local donde quieres ejecutar la app desde el IDE

set -e

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo "🐘 Iniciando PostgreSQL en Docker..."
echo "======================================"

# Verificar si Docker está corriendo
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker no está corriendo. Por favor, inicia Docker Desktop.${NC}"
    exit 1
fi

# Verificar si el contenedor ya existe
if docker ps -a --format '{{.Names}}' | grep -q '^motel-postgres-db$'; then
    echo -e "${YELLOW}⚠️  El contenedor ya existe${NC}"
    
    # Verificar si está corriendo
    if docker ps --format '{{.Names}}' | grep -q '^motel-postgres-db$'; then
        echo -e "${GREEN}✅ PostgreSQL ya está corriendo${NC}"
    else
        echo "🔄 Iniciando contenedor existente..."
        docker start motel-postgres-db
        docker start motel-adminer 2>/dev/null || true
    fi
else
    echo "🚀 Creando e iniciando contenedores..."
    docker-compose up -d postgres adminer
fi

# Esperar a que PostgreSQL esté listo
echo ""
echo "⏳ Esperando a que PostgreSQL esté listo..."
sleep 3

max_attempts=30
attempt=1

while [ $attempt -le $max_attempts ]; do
    if docker exec motel-postgres-db pg_isready -U postgres -d motel_management_db > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PostgreSQL está listo y aceptando conexiones${NC}"
        break
    fi
    
    echo "   Intento $attempt/$max_attempts..."
    sleep 2
    attempt=$((attempt + 1))
done

if [ $attempt -gt $max_attempts ]; then
    echo -e "${RED}❌ PostgreSQL no respondió después de $max_attempts intentos${NC}"
    echo "Revisa los logs: docker logs motel-postgres-db"
    exit 1
fi

echo ""
echo "=============================================="
echo -e "${GREEN}🎉 PostgreSQL está listo para usar${NC}"
echo "=============================================="
echo ""
echo "📊 Información de conexión:"
echo "   Host:     localhost"
echo "   Puerto:   5432"
echo "   Base de datos: motel_management_db"
echo "   Usuario:  postgres"
echo "   Password: carlosmanuel"
echo ""
echo "🌐 Adminer (Web UI):"
echo "   URL: http://localhost:8081"
echo "   Sistema: PostgreSQL"
echo "   Servidor: postgres"
echo ""
echo "🔍 Comandos útiles:"
echo "   Ver logs:        docker logs -f motel-postgres-db"
echo "   Detener:         docker-compose stop postgres"
echo "   Reiniciar:       docker-compose restart postgres"
echo "   Eliminar todo:   docker-compose down -v"
echo ""
echo "💡 Ahora puedes ejecutar tu aplicación Spring Boot desde tu IDE"
echo "   usando el perfil 'local' o el application.yml por defecto"
echo ""