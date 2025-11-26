#!/bin/bash

# Script de prueba para el sistema de autorización
# Uso: ./test-authorization.sh

BASE_URL="http://localhost:8080"

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para imprimir headers
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

# Función para hacer request y mostrar resultado
make_request() {
    local method=$1
    local endpoint=$2
    local user_id=$3
    local user_role=$4
    local data=$5
    local description=$6

    echo -e "${YELLOW}Test: ${description}${NC}"
    echo -e "Request: ${method} ${endpoint}"
    echo -e "User: ID=${user_id}, Role=${user_role}\n"

    if [ -z "$data" ]; then
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X ${method} ${BASE_URL}${endpoint} \
            -H "X-User-Id: ${user_id}" \
            -H "X-User-Role: ${user_role}" \
            -H "Content-Type: application/json")
    else
        response=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X ${method} ${BASE_URL}${endpoint} \
            -H "X-User-Id: ${user_id}" \
            -H "X-User-Role: ${user_role}" \
            -H "Content-Type: application/json" \
            -d "${data}")
    fi

    http_code=$(echo "$response" | grep "HTTP_CODE:" | cut -d: -f2)
    body=$(echo "$response" | sed '/HTTP_CODE:/d')

    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✅ Success (${http_code})${NC}"
    elif [ "$http_code" -eq 401 ]; then
        echo -e "${RED}❌ Unauthorized (${http_code})${NC}"
    elif [ "$http_code" -eq 403 ]; then
        echo -e "${RED}❌ Forbidden (${http_code})${NC}"
    else
        echo -e "${RED}❌ Error (${http_code})${NC}"
    fi

    if [ ! -z "$body" ]; then
        echo -e "Response: ${body}" | head -5
    fi
    echo ""
}

# ============================================
# TESTS
# ============================================

print_header "1. TESTS DE USUARIO (USER)"

make_request "GET" "/api/motels" "101" "USER" "" \
    "Usuario puede ver moteles"

make_request "GET" "/api/rooms" "101" "USER" "" \
    "Usuario puede ver habitaciones"

make_request "POST" "/api/motels" "101" "USER" \
    '{"name":"Test Motel","address":"Test St","city":"Test City"}' \
    "Usuario NO puede crear moteles (debe fallar con 403)"

make_request "POST" "/api/bookings" "101" "USER" \
    '{"userId":101,"roomId":1,"motelId":1,"checkInDate":"2025-12-01","checkOutDate":"2025-12-03","guestName":"Test User","guestEmail":"test@example.com","guestPhone":"123456789"}' \
    "Usuario puede crear reservas"

# ============================================

print_header "2. TESTS DE PROPIETARIO (PROPERTY_OWNER)"

make_request "GET" "/api/motels" "201" "PROPERTY_OWNER" "" \
    "Propietario puede ver moteles"

make_request "POST" "/api/motels" "201" "PROPERTY_OWNER" \
    '{"name":"Motel Paradise","address":"Av Principal 456","phoneNumber":"1234567","description":"Luxury motel","city":"Medellín","propertyId":201}' \
    "Propietario puede crear moteles"

make_request "POST" "/api/rooms" "201" "PROPERTY_OWNER" \
    '{"motelId":1,"number":"301","roomType":"Deluxe","price":150000,"description":"Deluxe room","isAvailable":true}' \
    "Propietario puede crear habitaciones"

make_request "GET" "/api/bookings" "201" "PROPERTY_OWNER" "" \
    "Propietario puede ver reservas"

# ============================================

print_header "3. TESTS DE ADMINISTRADOR (ADMIN)"

make_request "GET" "/api/motels" "1" "ADMIN" "" \
    "Admin puede ver moteles"

make_request "POST" "/api/motels" "1" "ADMIN" \
    '{"name":"Admin Motel","address":"Admin St","city":"Bogotá"}' \
    "Admin puede crear moteles"

make_request "DELETE" "/api/motels/999" "1" "ADMIN" "" \
    "Admin puede eliminar moteles (puede fallar si no existe el ID)"

make_request "GET" "/api/bookings" "1" "ADMIN" "" \
    "Admin puede ver todas las reservas"

# ============================================

print_header "4. TESTS DE VALIDACIÓN"

make_request "GET" "/api/motels" "" "" "" \
    "Request sin headers (debe fallar con 401)"

make_request "GET" "/api/motels" "101" "INVALID_ROLE" "" \
    "Request con rol inválido (debe fallar con 401)"

# ============================================

print_header "5. TESTS DE INTEGRACIÓN BOOKING → MOTEL"

echo -e "${YELLOW}Test: Crear reserva (valida integración con Motel Management)${NC}"
echo -e "Este test verifica que:"
echo -e "  1. Booking Service recibe la request con headers"
echo -e "  2. Booking Service llama a Motel Management vía Gateway"
echo -e "  3. Headers se propagan correctamente"
echo -e "  4. La habitación existe y está disponible\n"

make_request "POST" "/api/bookings" "101" "USER" \
    '{"userId":101,"roomId":1,"motelId":1,"checkInDate":"2025-12-15","checkOutDate":"2025-12-17","guestName":"Integration Test","guestEmail":"integration@test.com","guestPhone":"987654321"}' \
    "Crear reserva con integración completa"

# ============================================

print_header "TESTS COMPLETADOS"

echo -e "${GREEN}Todos los tests han sido ejecutados.${NC}"
echo -e "${YELLOW}Revisa los resultados arriba.${NC}"
echo -e "${BLUE}Para ver logs detallados, revisa los logs del Gateway.${NC}\n"
