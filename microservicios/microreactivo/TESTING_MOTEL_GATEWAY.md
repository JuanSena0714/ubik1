# Guía de Pruebas - Gateway con Motel Management

## Configuración Realizada

Se ha configurado correctamente el **API Gateway** para enrutar peticiones al microservicio **motelManagement** que corre en el puerto 8083.

### Rutas Configuradas en el Gateway

El gateway (puerto 8080) ahora redirige las siguientes rutas al microservicio motelManagement:

1. **Gestión de Moteles**: `/api/motels/**` → `http://localhost:8083`
2. **Gestión de Habitaciones**: `/api/rooms/**` → `http://localhost:8083`
3. **Gestión de Servicios**: `/api/services/**` → `http://localhost:8083`

## Pre-requisitos para Pruebas

### 1. Iniciar PostgreSQL

El microservicio motelManagement requiere PostgreSQL corriendo en `localhost:5432`.

**Opción A - Docker:**
```bash
docker run --name postgres-motel \
  -e POSTGRES_PASSWORD=carlosmanuel \
  -e POSTGRES_DB=motel_management_db \
  -p 5432:5432 \
  -d postgres
```

**Opción B - PostgreSQL Local:**
```bash
# Linux
sudo systemctl start postgresql

# Mac
brew services start postgresql

# Crear la base de datos
createdb -U postgres motel_management_db
```

### 2. Compilar el Proyecto

```bash
cd /home/user/Ubik-App/microservicios/microreactivo
mvn clean install -DskipTests
```

### 3. Iniciar los Servicios

**Opción A - Usar el script automático:**
```bash
cd /home/user/Ubik-App/microservicios/microreactivo/motelManegement
chmod +x start-services-postgres.sh
./start-services-postgres.sh
```

**Opción B - Iniciar manualmente:**

Terminal 1 - Gateway:
```bash
cd /home/user/Ubik-App/microservicios/microreactivo/gateway
mvn spring-boot:run
```

Terminal 2 - Motel Management:
```bash
cd /home/user/Ubik-App/microservicios/microreactivo/motelManegement
mvn spring-boot:run
```

## Pruebas de Endpoints

### 1. Verificar Health de los Servicios

```bash
# Gateway health
curl http://localhost:8080/actuator/health

# Motel Management health (directo)
curl http://localhost:8083/actuator/health
```

### 2. Pruebas de Moteles (via Gateway)

**Listar todos los moteles:**
```bash
curl http://localhost:8080/api/motels
```

**Crear un motel:**
```bash
curl -X POST http://localhost:8080/api/motels \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Motel Paradise",
    "address": "Calle Principal 123",
    "city": "Bogotá",
    "phone": "+57 1 234 5678"
  }'
```

**Obtener motel por ID:**
```bash
curl http://localhost:8080/api/motels/1
```

**Obtener moteles por ciudad:**
```bash
curl http://localhost:8080/api/motels/city/Bogotá
```

**Actualizar motel:**
```bash
curl -X PUT http://localhost:8080/api/motels/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Motel Paradise Updated",
    "address": "Calle Principal 123",
    "city": "Bogotá",
    "phone": "+57 1 234 5678"
  }'
```

**Eliminar motel:**
```bash
curl -X DELETE http://localhost:8080/api/motels/1
```

### 3. Pruebas de Habitaciones (via Gateway)

**Listar todas las habitaciones:**
```bash
curl http://localhost:8080/api/rooms
```

**Crear una habitación:**
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "motelId": 1,
    "number": "101",
    "type": "SUITE",
    "pricePerHour": 50000.0,
    "available": true
  }'
```

**Obtener habitación por ID:**
```bash
curl http://localhost:8080/api/rooms/1
```

**Obtener habitaciones de un motel:**
```bash
curl http://localhost:8080/api/rooms/motel/1
```

**Obtener habitaciones disponibles de un motel:**
```bash
curl http://localhost:8080/api/rooms/motel/1/available
```

**Actualizar habitación:**
```bash
curl -X PUT http://localhost:8080/api/rooms/1 \
  -H "Content-Type: application/json" \
  -d '{
    "motelId": 1,
    "number": "101",
    "type": "SUITE",
    "pricePerHour": 55000.0,
    "available": false
  }'
```

**Eliminar habitación:**
```bash
curl -X DELETE http://localhost:8080/api/rooms/1
```

### 4. Pruebas de Servicios (via Gateway)

**Listar todos los servicios:**
```bash
curl http://localhost:8080/api/services
```

**Crear un servicio:**
```bash
curl -X POST http://localhost:8080/api/services \
  -H "Content-Type: application/json" \
  -d '{
    "name": "WiFi",
    "description": "Internet de alta velocidad",
    "price": 5000.0
  }'
```

**Obtener servicio por ID:**
```bash
curl http://localhost:8080/api/services/1
```

**Obtener servicio por nombre:**
```bash
curl http://localhost:8080/api/services/name/WiFi
```

**Asociar servicio a habitación:**
```bash
curl -X POST http://localhost:8080/api/services/room/1/service/1
```

**Obtener servicios de una habitación:**
```bash
curl http://localhost:8080/api/services/room/1
```

**Eliminar servicio de habitación:**
```bash
curl -X DELETE http://localhost:8080/api/services/room/1/service/1
```

**Actualizar servicio:**
```bash
curl -X PUT http://localhost:8080/api/services/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "WiFi Premium",
    "description": "Internet de ultra alta velocidad",
    "price": 10000.0
  }'
```

**Eliminar servicio:**
```bash
curl -X DELETE http://localhost:8080/api/services/1
```

## Verificación de Enrutamiento

Para verificar que el gateway está enrutando correctamente, puedes comparar:

**Acceso directo al microservicio:**
```bash
curl http://localhost:8083/api/motels
```

**Acceso via gateway:**
```bash
curl http://localhost:8080/api/motels
```

Ambos deberían devolver el mismo resultado, confirmando que el gateway está funcionando correctamente.

## Logs y Debugging

**Ver logs del Gateway:**
```bash
tail -f logs/Gateway.log
```

**Ver logs de Motel Management:**
```bash
tail -f logs/Motel-Management.log
```

## Detener los Servicios

Si usaste el script automático, detén los servicios con:
```bash
./stop-services.sh
```

O manualmente:
```bash
# Encontrar y matar procesos
ps aux | grep spring-boot
kill -9 <PID>
```

## Resumen de Cambios

**Archivo modificado:** `/microservicios/microreactivo/gateway/src/main/resources/application.yml`

Se agregaron tres rutas para motelManagement:
- `motel-management-motels`: Enruta `/api/motels/**`
- `motel-management-rooms`: Enruta `/api/rooms/**`
- `motel-management-services`: Enruta `/api/services/**`

Todas las rutas apuntan a `http://localhost:8083` con `StripPrefix=0` para mantener el path completo.
