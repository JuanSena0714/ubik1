# Sistema de Autorización y Control de Permisos

## Descripción General

Este sistema implementa un control de acceso basado en roles (RBAC - Role-Based Access Control) a través del API Gateway. Todos los requests deben pasar por el gateway, que valida los permisos del usuario basándose en headers HTTP.

## Arquitectura

```
Cliente → Gateway (Puerto 8080) → Microservicios
                ↓
         [AuthorizationFilter]
         [RequestLoggingFilter]
                ↓
         Validación de Roles
                ↓
         Propagación de Headers
```

## Roles Disponibles

### 1. ADMIN
**Descripción:** Administrador del sistema con acceso completo.

**Permisos:**
- ✅ Todas las operaciones en todos los endpoints
- ✅ Gestión de moteles (CRUD)
- ✅ Gestión de habitaciones (CRUD)
- ✅ Gestión de servicios (CRUD)
- ✅ Gestión de reservas (CRUD)

### 2. PROPERTY_OWNER
**Descripción:** Propietario de motel que puede gestionar sus propiedades.

**Permisos:**
- ✅ Ver, crear, editar y eliminar moteles
- ✅ Ver, crear, editar y eliminar habitaciones
- ✅ Ver, crear, editar y eliminar servicios
- ✅ Ver reservas (todas)
- ✅ Confirmar o cancelar reservas
- ❌ No puede crear reservas como usuario

### 3. USER
**Descripción:** Usuario final que puede buscar y reservar habitaciones.

**Permisos:**
- ✅ Ver moteles (GET)
- ✅ Ver habitaciones (GET)
- ✅ Ver servicios (GET)
- ✅ Crear reservas (POST)
- ✅ Ver sus propias reservas (GET)
- ✅ Confirmar o cancelar sus propias reservas (PUT)
- ❌ No puede crear, editar o eliminar moteles
- ❌ No puede crear, editar o eliminar habitaciones
- ❌ No puede crear, editar o eliminar servicios

## Headers Requeridos

Todos los requests al API Gateway **DEBEN** incluir estos headers:

| Header | Tipo | Requerido | Descripción | Ejemplo |
|--------|------|-----------|-------------|---------|
| `X-User-Id` | String | ✅ Sí | Identificador único del usuario | `123` |
| `X-User-Role` | String | ✅ Sí | Rol del usuario | `USER`, `PROPERTY_OWNER`, `ADMIN` |
| `X-User-Email` | String | ❌ No | Email del usuario (opcional) | `user@example.com` |

## Matriz de Permisos

| Endpoint | Método | ADMIN | PROPERTY_OWNER | USER |
|----------|--------|-------|----------------|------|
| `/api/motels` | GET | ✅ | ✅ | ✅ |
| `/api/motels` | POST | ✅ | ✅ | ❌ |
| `/api/motels/{id}` | PUT | ✅ | ✅ | ❌ |
| `/api/motels/{id}` | DELETE | ✅ | ✅ | ❌ |
| `/api/rooms` | GET | ✅ | ✅ | ✅ |
| `/api/rooms` | POST | ✅ | ✅ | ❌ |
| `/api/rooms/{id}` | PUT | ✅ | ✅ | ❌ |
| `/api/rooms/{id}` | DELETE | ✅ | ✅ | ❌ |
| `/api/services` | GET | ✅ | ✅ | ✅ |
| `/api/services` | POST | ✅ | ✅ | ❌ |
| `/api/services/{id}` | PUT | ✅ | ✅ | ❌ |
| `/api/services/{id}` | DELETE | ✅ | ✅ | ❌ |
| `/api/bookings` | GET | ✅ | ✅ | ✅ |
| `/api/bookings` | POST | ✅ | ❌ | ✅ |
| `/api/bookings/{id}/confirm` | PUT | ✅ | ✅ | ✅ |
| `/api/bookings/{id}/cancel` | PUT | ✅ | ✅ | ✅ |

## Ejemplos de Uso

### 1. Usuario Normal - Ver Moteles Disponibles

```bash
curl -X GET http://localhost:8080/api/motels \
  -H "X-User-Id: 101" \
  -H "X-User-Role: USER" \
  -H "X-User-Email: juan@example.com"
```

**Respuesta esperada:** ✅ 200 OK (Lista de moteles)

---

### 2. Usuario Normal - Crear Reserva

```bash
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
    "guestName": "Juan Perez",
    "guestEmail": "juan@example.com",
    "guestPhone": "+57 300 123 4567"
  }'
```

**Respuesta esperada:** ✅ 200 OK (Reserva creada)

---

### 3. Usuario Normal - Intentar Crear Motel (Denegado)

```bash
curl -X POST http://localhost:8080/api/motels \
  -H "X-User-Id: 101" \
  -H "X-User-Role: USER" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Hotel Test",
    "address": "Calle 123",
    "city": "Bogotá"
  }'
```

**Respuesta esperada:** ❌ 403 Forbidden
```json
{
  "error": "Insufficient permissions for POST /api/motels"
}
```

---

### 4. Propietario - Crear Motel

```bash
curl -X POST http://localhost:8080/api/motels \
  -H "X-User-Id: 201" \
  -H "X-User-Role: PROPERTY_OWNER" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Motel Paradise",
    "address": "Avenida Principal 456",
    "phoneNumber": "+57 1 234 5678",
    "description": "Motel con todas las comodidades",
    "city": "Medellín",
    "propertyId": 201
  }'
```

**Respuesta esperada:** ✅ 200 OK (Motel creado)

---

### 5. Propietario - Agregar Habitación

```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "X-User-Id: 201" \
  -H "X-User-Role: PROPERTY_OWNER" \
  -H "Content-Type: application/json" \
  -d '{
    "motelId": 1,
    "number": "101",
    "roomType": "Deluxe",
    "price": 150000.00,
    "description": "Habitación deluxe con jacuzzi",
    "isAvailable": true
  }'
```

**Respuesta esperada:** ✅ 200 OK (Habitación creada)

---

### 6. Propietario - Ver Reservas

```bash
curl -X GET http://localhost:8080/api/bookings \
  -H "X-User-Id: 201" \
  -H "X-User-Role: PROPERTY_OWNER"
```

**Respuesta esperada:** ✅ 200 OK (Lista de reservas)

---

### 7. Administrador - Todas las Operaciones

```bash
# Ver todas las reservas
curl -X GET http://localhost:8080/api/bookings \
  -H "X-User-Id: 1" \
  -H "X-User-Role: ADMIN"

# Eliminar un motel
curl -X DELETE http://localhost:8080/api/motels/5 \
  -H "X-User-Id: 1" \
  -H "X-User-Role: ADMIN"
```

**Respuesta esperada:** ✅ 200 OK (Operación exitosa)

---

### 8. Request sin Headers (Denegado)

```bash
curl -X GET http://localhost:8080/api/motels
```

**Respuesta esperada:** ❌ 401 Unauthorized
```
X-Error-Message: Missing X-User-Id header
```

---

## Códigos de Respuesta

| Código | Significado | Cuándo Ocurre |
|--------|-------------|---------------|
| `200` | OK | Operación exitosa |
| `401` | Unauthorized | Falta header `X-User-Id` o `X-User-Role` |
| `403` | Forbidden | El usuario no tiene permisos para la operación |
| `404` | Not Found | Recurso no encontrado |
| `500` | Internal Server Error | Error interno del servidor |

## Propagación de Headers

### Flujo de Headers

1. **Cliente → Gateway:**
   - Cliente envía request con headers `X-User-Id`, `X-User-Role`

2. **Gateway → Microservicio:**
   - Gateway valida headers y permisos
   - Gateway propaga headers al microservicio downstream
   - Microservicio recibe headers en el contexto de la request

3. **Microservicio → Otro Microservicio:**
   - Booking Service → Gateway → Motel Management
   - Headers son propagados automáticamente vía `HeaderPropagationWebClientFilter`

### Ejemplo: Crear Reserva (Flujo Completo)

```
Cliente
  ↓ POST /api/bookings + X-User-Id: 101 + X-User-Role: USER
Gateway (8080)
  ↓ [AuthorizationFilter] ✅ USER puede crear reservas
  ↓ [RequestLoggingFilter] 📝 Log: "User 101 creating booking"
  ↓ Propagate headers
Booking Service (8083)
  ↓ Valida datos de la reserva
  ↓ GET /api/rooms/1 + X-User-Id: 101 + X-User-Role: USER
Gateway (8080)
  ↓ [AuthorizationFilter] ✅ USER puede ver habitaciones
  ↓ Propagate headers
Motel Management (8084)
  ↓ Retorna datos de la habitación
  ↓ Response →
Booking Service (8083)
  ↓ Crea la reserva
  ↓ Response →
Cliente
```

## Logging y Auditoría

El sistema registra todas las operaciones:

```
2025-11-25 10:30:15 INFO  Gateway Request: POST /api/bookings | User: 101 | Role: USER | IP: 192.168.1.10
2025-11-25 10:30:16 INFO  Gateway Response: POST /api/bookings | Status: 200 | Duration: 245ms | User: 101
```

## Configuración

### Gateway (application.yml)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: booking-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/bookings/**
          filters:
            - StripPrefix=0
            - name: AuthorizationFilter
```

### Booking Service (application.yml)

```yaml
services:
  motel-management:
    url: http://localhost:8080  # Gateway URL
```

## Componentes Implementados

### Gateway
- ✅ `AuthorizationFilter` - Valida permisos basados en rol
- ✅ `RequestLoggingFilter` - Auditoría de requests
- ✅ CORS configurado globalmente

### Booking Service
- ✅ `UserContextWebFilter` - Extrae headers de la request
- ✅ `HeaderPropagationWebClientFilter` - Propaga headers a servicios downstream
- ✅ `MotelServiceClient` - Actualizado para usar gateway

## Mejores Prácticas

1. **Siempre incluir headers:** Todos los requests deben incluir `X-User-Id` y `X-User-Role`
2. **Usar el gateway:** Nunca llamar directamente a los microservicios (puertos 8083, 8084)
3. **Validar roles:** El backend valida, pero el frontend también debe ocultar opciones no permitidas
4. **Logs:** Revisar logs del gateway para auditoría

## Seguridad Adicional Recomendada

Para producción, se recomienda:

1. **JWT Tokens:** En lugar de headers simples, usar JWT firmados
2. **HTTPS:** Encriptar comunicación con TLS
3. **Rate Limiting:** Limitar requests por usuario
4. **API Keys:** Para servicios externos
5. **OAuth2:** Para autenticación de terceros

## Troubleshooting

### Error: "Missing X-User-Id header"
**Solución:** Asegúrate de incluir el header `X-User-Id` en tu request

### Error: "Insufficient permissions"
**Solución:** Verifica que tu rol tenga permisos para esa operación (ver Matriz de Permisos)

### Error: "Connection refused"
**Solución:** Verifica que el gateway esté corriendo en el puerto 8080

### Request no llega al microservicio
**Solución:** Verifica los logs del gateway para ver si el filtro de autorización está bloqueando el request
