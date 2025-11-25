# Guía de Integración - Booking Service + Motel Management

## 📋 Descripción

Este documento describe la integración completa entre el **Booking Service** y el **Motel Management Service**, utilizando el **API Gateway** como punto único de entrada con control de permisos basado en roles.

## 🏗️ Arquitectura

```
┌─────────────────┐
│    Cliente      │
│  (Frontend/App) │
└────────┬────────┘
         │ Headers: X-User-Id, X-User-Role
         ↓
┌─────────────────────────────────────────┐
│         API Gateway (8080)              │
│  ┌───────────────────────────────────┐  │
│  │   AuthorizationFilter             │  │
│  │   - Valida headers                │  │
│  │   - Verifica permisos por rol     │  │
│  │   - Propaga headers               │  │
│  └───────────────────────────────────┘  │
│  ┌───────────────────────────────────┐  │
│  │   RequestLoggingFilter            │  │
│  │   - Auditoría de requests         │  │
│  └───────────────────────────────────┘  │
└──────────┬──────────────────────────────┘
           │
     ┌─────┴─────┐
     │           │
     ↓           ↓
┌─────────┐  ┌──────────────────┐
│ Booking │  │ Motel Management │
│ Service │←─│    Service       │
│ (8083)  │  │    (8084)        │
└─────────┘  └──────────────────┘
  MySQL         PostgreSQL
```

## 🔑 Características Implementadas

### ✅ 1. Control de Acceso Basado en Roles (RBAC)

Tres roles con permisos diferenciados:
- **ADMIN**: Acceso completo a todas las operaciones
- **PROPERTY_OWNER**: Gestión de moteles, habitaciones y servicios
- **USER**: Solo lectura de moteles/habitaciones y gestión de sus propias reservas

### ✅ 2. Propagación Automática de Headers

Los headers de autenticación se propagan automáticamente a través de toda la cadena:
- Cliente → Gateway
- Gateway → Booking Service
- Booking Service → Gateway → Motel Management

### ✅ 3. Integración Service-to-Service

Booking Service se comunica con Motel Management a través del Gateway:
- **Validación de habitaciones** antes de crear reserva
- **Actualización de disponibilidad** al confirmar/cancelar reserva
- **Enriquecimiento de datos** en respuestas (nombre del motel, tipo de habitación, etc.)

### ✅ 4. Auditoría y Logging

Todos los requests se registran con:
- Usuario que realiza la operación
- Rol del usuario
- Endpoint accedido
- Resultado (status code)
- Tiempo de respuesta

## 🚀 Inicio Rápido

### Prerrequisitos

1. **Java 17+**
2. **Maven 3.8+**
3. **MySQL** (para Booking Service)
4. **PostgreSQL** (para Motel Management)

### Configuración de Bases de Datos

#### MySQL (Booking Service)

```bash
mysql -u root -p
```

```sql
CREATE DATABASE booking_db;
USE booking_db;
SOURCE microservicios/microreactivo/mysql-init.sql;
```

#### PostgreSQL (Motel Management)

```bash
psql -U postgres
```

```sql
CREATE DATABASE motel_management_db;
\c motel_management_db
\i microservicios/microreactivo/motelManegement/src/main/resources/Postgres-init-motel.sql
```

### Iniciar Servicios

**Opción 1: Con Maven (desarrollo)**

Terminal 1 - Gateway:
```bash
cd microservicios/microreactivo/gateway
mvn spring-boot:run
```

Terminal 2 - Motel Management:
```bash
cd microservicios/microreactivo/motelManegement
mvn spring-boot:run
```

Terminal 3 - Booking Service:
```bash
cd microservicios/microreactivo/bookingService
mvn spring-boot:run
```

**Opción 2: Con Maven desde raíz**

```bash
cd microservicios/microreactivo
mvn clean install
mvn spring-boot:run -pl gateway
mvn spring-boot:run -pl motelManegement
mvn spring-boot:run -pl bookingService
```

### Verificar que Todo Está Corriendo

```bash
# Gateway
curl http://localhost:8080/actuator/health

# Motel Management
curl http://localhost:8084/actuator/health

# Booking Service
curl http://localhost:8083/actuator/health
```

## 🧪 Pruebas

### Ejecutar Suite de Pruebas Automatizada

```bash
cd microservicios/microreactivo
./test-authorization.sh
```

Este script ejecuta automáticamente pruebas para:
- ✅ Permisos de usuarios normales (USER)
- ✅ Permisos de propietarios (PROPERTY_OWNER)
- ✅ Permisos de administradores (ADMIN)
- ✅ Validación de headers
- ✅ Integración entre servicios

### Pruebas Manuales

#### 1. Como Usuario Normal (USER)

```bash
# Ver moteles disponibles
curl http://localhost:8080/api/motels \
  -H "X-User-Id: 101" \
  -H "X-User-Role: USER"

# Ver habitaciones disponibles en un motel
curl http://localhost:8080/api/rooms/motel/1/available \
  -H "X-User-Id: 101" \
  -H "X-User-Role: USER"

# Crear una reserva
curl -X POST http://localhost:8080/api/bookings \
  -H "X-User-Id: 101" \
  -H "X-User-Role: USER" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 101,
    "roomId": 1,
    "motelId": 1,
    "checkInDate": "2025-12-01",
    "checkOutDate": "2025-12-03",
    "guestName": "Juan Pérez",
    "guestEmail": "juan@example.com",
    "guestPhone": "+57 300 123 4567"
  }'

# Ver mis reservas
curl http://localhost:8080/api/bookings/user/101 \
  -H "X-User-Id: 101" \
  -H "X-User-Role: USER"
```

#### 2. Como Propietario (PROPERTY_OWNER)

```bash
# Crear un motel
curl -X POST http://localhost:8080/api/motels \
  -H "X-User-Id: 201" \
  -H "X-User-Role: PROPERTY_OWNER" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Motel Paradise",
    "address": "Avenida Principal 456",
    "phoneNumber": "+57 1 234 5678",
    "description": "Motel de lujo con todas las comodidades",
    "city": "Medellín",
    "propertyId": 201
  }'

# Agregar habitación al motel
curl -X POST http://localhost:8080/api/rooms \
  -H "X-User-Id: 201" \
  -H "X-User-Role: PROPERTY_OWNER" \
  -H "Content-Type: application/json" \
  -d '{
    "motelId": 1,
    "number": "201",
    "roomType": "Suite",
    "price": 250000.00,
    "description": "Suite presidencial con jacuzzi",
    "isAvailable": true
  }'

# Ver todas las reservas (para gestionar propiedades)
curl http://localhost:8080/api/bookings \
  -H "X-User-Id: 201" \
  -H "X-User-Role: PROPERTY_OWNER"
```

#### 3. Como Administrador (ADMIN)

```bash
# Todas las operaciones están permitidas
curl http://localhost:8080/api/motels \
  -H "X-User-Id: 1" \
  -H "X-User-Role: ADMIN"

# Eliminar un motel
curl -X DELETE http://localhost:8080/api/motels/5 \
  -H "X-User-Id: 1" \
  -H "X-User-Role: ADMIN"
```

## 📊 Flujo de una Reserva Completa

### Paso a Paso

1. **Cliente envía request al Gateway**
   ```
   POST /api/bookings
   Headers: X-User-Id: 101, X-User-Role: USER
   ```

2. **Gateway valida permisos**
   - ✅ AuthorizationFilter verifica que USER puede crear reservas
   - 📝 RequestLoggingFilter registra la operación

3. **Gateway enruta a Booking Service**
   - Headers son propagados automáticamente

4. **Booking Service procesa la reserva**
   - Valida datos de entrada
   - **Llama a Gateway** para obtener datos de la habitación:
     ```
     GET /api/rooms/1
     Headers: X-User-Id: 101, X-User-Role: USER (propagados)
     ```

5. **Gateway reenvía a Motel Management**
   - AuthorizationFilter valida que USER puede ver habitaciones
   - Propaga headers

6. **Motel Management retorna datos**
   - Información de la habitación (precio, disponibilidad, etc.)

7. **Booking Service completa la reserva**
   - Calcula precio total (precio × noches)
   - Verifica disponibilidad
   - Crea registro de reserva
   - **Llama a Gateway** para actualizar disponibilidad:
     ```
     PUT /api/rooms/1
     Headers: X-User-Id: 101, X-User-Role: USER
     ```

8. **Response al cliente**
   - Datos de la reserva enriquecidos con información del motel

### Diagrama de Secuencia

```
Cliente          Gateway         Booking         Gateway         Motel Mgmt
  │                 │               │               │                │
  │──POST /bookings─→               │               │                │
  │ (headers)        │               │               │                │
  │                 │               │               │                │
  │                 │──Validate────→│               │                │
  │                 │  perms        │               │                │
  │                 │               │               │                │
  │                 │──Forward─────→│               │                │
  │                 │ (headers)     │               │                │
  │                 │               │               │                │
  │                 │               │──GET /rooms──→│                │
  │                 │               │  (headers)    │                │
  │                 │               │               │                │
  │                 │               │               │──Validate─────→│
  │                 │               │               │  perms         │
  │                 │               │               │                │
  │                 │               │               │──Forward──────→│
  │                 │               │               │                │
  │                 │               │               │←─Room data────│
  │                 │               │←─────────────│                │
  │                 │               │               │                │
  │                 │               │──Create──────→│                │
  │                 │               │  booking      │                │
  │                 │               │               │                │
  │                 │               │──PUT /rooms──→│                │
  │                 │               │  (update)     │                │
  │                 │               │               │──Forward──────→│
  │                 │               │               │                │
  │                 │               │               │←─Updated room─│
  │                 │               │←─────────────│                │
  │                 │               │               │                │
  │                 │←─Booking─────│               │                │
  │←─────────────Response           │               │                │
  │ (enriched data)  │               │               │                │
```

## 🔒 Seguridad

### Headers Obligatorios

Todos los requests **DEBEN** incluir:
- `X-User-Id`: Identificador del usuario
- `X-User-Role`: Rol (ADMIN, PROPERTY_OWNER, USER)

### Errores Comunes

| Error | Causa | Solución |
|-------|-------|----------|
| 401 Unauthorized | Falta header X-User-Id o X-User-Role | Agregar headers requeridos |
| 403 Forbidden | Rol sin permisos para la operación | Verificar matriz de permisos |
| 500 Internal Server Error | Servicio caído o error de integración | Verificar logs del servicio |

### CORS

El Gateway tiene CORS configurado para permitir:
- Todos los orígenes (desarrollo)
- Métodos: GET, POST, PUT, DELETE, OPTIONS
- Headers custom: X-User-Id, X-User-Role, X-User-Email

**⚠️ PRODUCCIÓN:** Restringir `allowed-origins` a dominios específicos

## 📝 Logs y Monitoreo

### Ver Logs del Gateway

```bash
# Si usas Spring Boot con Maven
tail -f gateway/target/spring.log

# O revisa la consola donde ejecutaste mvn spring-boot:run
```

### Ejemplo de Log

```
2025-11-25 10:30:15 INFO  Gateway Request: POST /api/bookings | User: 101 | Role: USER | IP: 192.168.1.10
2025-11-25 10:30:15 DEBUG Authorization: Checking permissions for USER on POST /api/bookings
2025-11-25 10:30:15 DEBUG Authorization: ✅ Permission granted
2025-11-25 10:30:15 INFO  Forwarding to: http://localhost:8083/api/bookings
2025-11-25 10:30:16 INFO  Gateway Response: POST /api/bookings | Status: 200 | Duration: 245ms | User: 101
```

## 🛠️ Desarrollo

### Agregar Nuevo Endpoint

1. **Implementar en el servicio** (Booking o Motel Management)

2. **Actualizar Gateway** (`application.yml`):
   ```yaml
   - id: mi-nuevo-endpoint
     uri: http://localhost:8083
     predicates:
       - Path=/api/mi-endpoint/**
     filters:
       - StripPrefix=0
       - name: AuthorizationFilter
   ```

3. **Actualizar AuthorizationFilter** si necesita permisos especiales

4. **Documentar** en `AUTHORIZATION_SYSTEM.md`

### Agregar Nuevo Rol

1. **Actualizar AuthorizationFilter**:
   ```java
   private boolean isValidRole(String role) {
       return role.equals("ADMIN") ||
              role.equals("PROPERTY_OWNER") ||
              role.equals("USER") ||
              role.equals("MI_NUEVO_ROL");  // ← Agregar aquí
   }
   ```

2. **Implementar lógica de permisos**:
   ```java
   private boolean hasPermission(String role, String method, String path, String userId) {
       if (role.equals("MI_NUEVO_ROL")) {
           return hasMiNuevoRolPermission(method, path);
       }
       // ...
   }
   ```

3. **Documentar** permisos en `AUTHORIZATION_SYSTEM.md`

## 📚 Documentación Adicional

- **[AUTHORIZATION_SYSTEM.md](AUTHORIZATION_SYSTEM.md)**: Documentación completa del sistema de autorización
- **[TESTING_MOTEL_GATEWAY.md](TESTING_MOTEL_GATEWAY.md)**: Guía de pruebas del gateway
- **[test-authorization.sh](test-authorization.sh)**: Script de pruebas automatizado

## 🤝 Contribuir

1. Crear feature branch
2. Implementar cambios
3. Actualizar documentación
4. Agregar tests
5. Crear pull request

## 📞 Soporte

Para problemas o preguntas:
1. Revisar logs del Gateway
2. Verificar que todos los servicios estén corriendo
3. Consultar `AUTHORIZATION_SYSTEM.md` para matriz de permisos

## 🎯 Roadmap

Mejoras futuras recomendadas:
- [ ] JWT Tokens en lugar de headers simples
- [ ] Rate limiting por usuario
- [ ] Cache de datos de moteles/habitaciones
- [ ] Circuit breaker para resilencia
- [ ] Métricas con Prometheus
- [ ] Tracing distribuido con Zipkin
- [ ] API Documentation con Swagger/OpenAPI
