#!/bin/bash

# Script para detener servicios Docker

# Colores
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${YELLOW}🛑 Deteniendo servicios...${NC}"

# Verificar qué compose file usar
if [ "$1" == "all" ]; then
    echo "Deteniendo sistema completo..."
    docker-compose -f docker-compose-full.yml down
elif [ "$1" == "clean" ]; then
    echo "Deteniendo y eliminando volúmenes (se perderán los datos)..."
    docker-compose -f docker-compose-full.yml down -v
    docker-compose down -v
else
    echo "Deteniendo solo la base de datos..."
    docker-compose down
fi

echo -e "${GREEN}✅ Servicios detenidos${NC}"
echo ""
echo "💡 Opciones:"
echo "   ./stop-services.sh        - Detiene solo la base de datos"
echo "   ./stop-services.sh all    - Detiene todo (DB + App)"
echo "   ./stop-services.sh clean  - Detiene y elimina volúmenes"
echo ""