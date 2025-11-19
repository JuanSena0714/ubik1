# Guía de Pruebas - Sistema de Reservas (Booking Service)

## Configuración Inicial

### 1. Iniciar las Bases de Datos

**PostgreSQL (Motel Management):**
```bash
# Asegúrate de que PostgreSQL esté corriendo
sudo systemctl start postgresql
```

**MySQL (Booking Service):**
```bash
# Asegúrate de que MySQL esté corriendo
sudo systemctl start mysql

# Inicializar la base de datos
mysql -u root -p < /home/user/ubik1/microservicios/microreactivo/mysql-init.sql
```

### 2. Iniciar los Microservicios

**Terminal 1 - Motel Management Service (Puerto 8084):**
```bash
cd /home/user/ubik1/microservicios/microreactivo/motelManegement
mvn spring-boot:run
```

**Terminal 2 - Booking Service (Puerto 8083):**
```bash
cd /home/user/ubik1/microservicios/microreactivo/bookingService
mvn spring-boot:run
```

**Terminal 3 - API Gateway (Puerto 8080):**
```bash
cd /home/user/ubik1/microservicios/microreactivo/gateway
mvn spring-boot:run
```

---

## Prerequisitos: Crear Datos de Prueba

### Crear un Motel
```bash
curl -X POST http://localhost:8080/api/motels \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Grand Motel",
    "address": "123 Main Street",
    "phoneNumber": "555-1234",
    "description": "Luxury motel in the city center",
    "city": "Miami",
    "propertyId": 1
  }'
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "name": "Grand Motel",
  "address": "123 Main Street",
  "phoneNumber": "555-1234",
  "description": "Luxury motel in the city center",
  "city": "Miami",
  "propertyId": 1,
  "dateCreated": "2025-11-19T..."
}
```

### Crear Habitaciones

**Habitación Suite:**
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "motelId": 1,
    "number": "101",
    "roomType": "Suite",
    "price": 150.00,
    "description": "Luxury suite with ocean view",
    "isAvailable": true
  }'
```

**Habitación Standard:**
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "motelId": 1,
    "number": "102",
    "roomType": "Standard",
    "price": 80.00,
    "description": "Comfortable standard room",
    "isAvailable": true
  }'
```

**Habitación Deluxe:**
```bash
curl -X POST http://localhost:8080/api/rooms \
  -H "Content-Type: application/json" \
  -d '{
    "motelId": 1,
    "number": "103",
    "roomType": "Deluxe",
    "price": 120.00,
    "description": "Deluxe room with king bed",
    "isAvailable": true
  }'
```

### Verificar Habitaciones Creadas
```bash
curl http://localhost:8080/api/rooms
```

---

## Pruebas del Sistema de Reservas

### 1. Crear una Reserva (POST /api/bookings)

**Caso Exitoso - 2 noches:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "roomId": 1,
    "motelId": 1,
    "checkInDate": "2025-11-20",
    "checkOutDate": "2025-11-22",
    "guestName": "Juan Pérez",
    "guestEmail": "juan.perez@email.com",
    "guestPhone": "555-0001",
    "specialRequests": "Late check-in preferred"
  }'
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "userId": 1,
  "roomId": 1,
  "motelId": 1,
  "checkInDate": "2025-11-20",
  "checkOutDate": "2025-11-22",
  "totalPrice": 300.00,
  "status": "PENDING",
  "guestName": "Juan Pérez",
  "guestEmail": "juan.perez@email.com",
  "guestPhone": "555-0001",
  "specialRequests": "Late check-in preferred",
  "createdAt": "2025-11-19T...",
  "updatedAt": "2025-11-19T...",
  "motelName": "Grand Motel",
  "roomNumber": "101",
  "roomType": "Suite"
}
```

**Caso Error - Fecha de salida antes de entrada:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "roomId": 1,
    "motelId": 1,
    "checkInDate": "2025-11-25",
    "checkOutDate": "2025-11-23",
    "guestName": "Juan Pérez",
    "guestEmail": "juan.perez@email.com",
    "guestPhone": "555-0001"
  }'
```

**Respuesta esperada:**
```
Error creating booking: Check-out date must be after check-in date
```

**Caso Error - Habitación no disponible:**
```bash
# Primero crear una reserva para la habitación 1
# Luego intentar crear otra reserva para la misma habitación
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "roomId": 1,
    "motelId": 1,
    "checkInDate": "2025-11-20",
    "checkOutDate": "2025-11-22",
    "guestName": "María García",
    "guestEmail": "maria.garcia@email.com",
    "guestPhone": "555-0002"
  }'
```

**Respuesta esperada:**
```
Error creating booking: Room is not available
```

---

### 2. Obtener Todas las Reservas (GET /api/bookings)

```bash
curl http://localhost:8080/api/bookings
```

**Respuesta esperada:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "roomId": 1,
    "motelId": 1,
    "checkInDate": "2025-11-20",
    "checkOutDate": "2025-11-22",
    "totalPrice": 300.00,
    "status": "PENDING",
    "guestName": "Juan Pérez",
    "guestEmail": "juan.perez@email.com",
    "guestPhone": "555-0001",
    "specialRequests": "Late check-in preferred",
    "createdAt": "2025-11-19T...",
    "updatedAt": "2025-11-19T...",
    "motelName": "Grand Motel",
    "roomNumber": "101",
    "roomType": "Suite"
  }
]
```

---

### 3. Obtener Reserva por ID (GET /api/bookings/{id})

```bash
curl http://localhost:8080/api/bookings/1
```

**Respuesta esperada:** Igual que la creación de la reserva

**Caso Error - ID inexistente:**
```bash
curl http://localhost:8080/api/bookings/999
```

**Respuesta esperada:** HTTP 404 Not Found

---

### 4. Obtener Reservas por Usuario (GET /api/bookings/user/{userId})

```bash
curl http://localhost:8080/api/bookings/user/1
```

**Respuesta esperada:** Array con todas las reservas del usuario 1

---

### 5. Confirmar Reserva (PUT /api/bookings/{id}/confirm)

```bash
curl -X PUT http://localhost:8080/api/bookings/1/confirm
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "userId": 1,
  "roomId": 1,
  "motelId": 1,
  "checkInDate": "2025-11-20",
  "checkOutDate": "2025-11-22",
  "totalPrice": 300.00,
  "status": "CONFIRMED",
  "guestName": "Juan Pérez",
  "guestEmail": "juan.perez@email.com",
  "guestPhone": "555-0001",
  "specialRequests": "Late check-in preferred",
  "createdAt": "2025-11-19T...",
  "updatedAt": "2025-11-19T...",
  "motelName": "Grand Motel",
  "roomNumber": "101",
  "roomType": "Suite"
}
```

**Nota:** El `status` cambió de `PENDING` a `CONFIRMED`

---

### 6. Cancelar Reserva (PUT /api/bookings/{id}/cancel)

```bash
curl -X PUT http://localhost:8080/api/bookings/1/cancel
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "userId": 1,
  "roomId": 1,
  "motelId": 1,
  "checkInDate": "2025-11-20",
  "checkOutDate": "2025-11-22",
  "totalPrice": 300.00,
  "status": "CANCELLED",
  "guestName": "Juan Pérez",
  "guestEmail": "juan.perez@email.com",
  "guestPhone": "555-0001",
  "specialRequests": "Late check-in preferred",
  "createdAt": "2025-11-19T...",
  "updatedAt": "2025-11-19T...",
  "motelName": "Grand Motel",
  "roomNumber": "101",
  "roomType": "Suite"
}
```

**Importante:** Cuando se cancela una reserva, la habitación se marca como disponible nuevamente.

**Verificar que la habitación volvió a estar disponible:**
```bash
curl http://localhost:8080/api/rooms/1
```

La respuesta debe mostrar `"isAvailable": true`

---

## Flujo de Prueba Completo

### Escenario 1: Reserva Exitosa Completa

```bash
# 1. Crear reserva
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "roomId": 2,
    "motelId": 1,
    "checkInDate": "2025-11-21",
    "checkOutDate": "2025-11-24",
    "guestName": "Ana López",
    "guestEmail": "ana.lopez@email.com",
    "guestPhone": "555-0003"
  }'

# 2. Verificar que se creó (debe tener status PENDING)
curl http://localhost:8080/api/bookings/2

# 3. Verificar que la habitación 2 ya no está disponible
curl http://localhost:8080/api/rooms/2

# 4. Confirmar la reserva
curl -X PUT http://localhost:8080/api/bookings/2/confirm

# 5. Verificar que cambió a CONFIRMED
curl http://localhost:8080/api/bookings/2

# 6. Ver todas las reservas del usuario
curl http://localhost:8080/api/bookings/user/1
```

### Escenario 2: Cancelación de Reserva

```bash
# 1. Crear una nueva reserva
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "roomId": 3,
    "motelId": 1,
    "checkInDate": "2025-11-25",
    "checkOutDate": "2025-11-27",
    "guestName": "Carlos Ruiz",
    "guestEmail": "carlos.ruiz@email.com",
    "guestPhone": "555-0004"
  }'

# 2. Verificar que la habitación 3 no está disponible
curl http://localhost:8080/api/rooms/3

# 3. Cancelar la reserva
curl -X PUT http://localhost:8080/api/bookings/3/cancel

# 4. Verificar que el status es CANCELLED
curl http://localhost:8080/api/bookings/3

# 5. Verificar que la habitación 3 volvió a estar disponible
curl http://localhost:8080/api/rooms/3
```

### Escenario 3: Múltiples Reservas para Diferentes Habitaciones

```bash
# Usuario 1 reserva habitación 1
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "roomId": 1,
    "motelId": 1,
    "checkInDate": "2025-12-01",
    "checkOutDate": "2025-12-05",
    "guestName": "Pedro Sánchez",
    "guestEmail": "pedro.sanchez@email.com",
    "guestPhone": "555-0005"
  }'

# Usuario 1 reserva habitación 2
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "roomId": 2,
    "motelId": 1,
    "checkInDate": "2025-12-10",
    "checkOutDate": "2025-12-12",
    "guestName": "Pedro Sánchez",
    "guestEmail": "pedro.sanchez@email.com",
    "guestPhone": "555-0005"
  }'

# Ver todas las reservas del usuario 1
curl http://localhost:8080/api/bookings/user/1

# Debe mostrar 2 reservas
```

---

## Validaciones Implementadas

### 1. Fechas
- ✅ Check-out debe ser posterior a check-in
- ✅ Debe haber al menos 1 noche de estadía
- ✅ Las fechas deben ser futuras (validado en BookingRequest con @Future)

### 2. Disponibilidad
- ✅ La habitación debe estar disponible al crear la reserva
- ✅ Al crear reserva, la habitación se marca como no disponible
- ✅ Al cancelar reserva, la habitación se marca como disponible

### 3. Cálculo de Precio
- ✅ Precio total = precio por noche × número de noches
- ✅ El cálculo se hace automáticamente

### 4. Estados de Reserva
- ✅ PENDING: Estado inicial al crear reserva
- ✅ CONFIRMED: Estado después de confirmar
- ✅ CANCELLED: Estado después de cancelar
- ✅ (COMPLETED: Para futuras implementaciones con check-out)

---

## Verificar Salud de los Servicios

```bash
# Gateway
curl http://localhost:8080/actuator/health

# Booking Service
curl http://localhost:8083/actuator/health

# Motel Management Service
curl http://localhost:8084/actuator/health
```

Todos deben responder:
```json
{"status":"UP"}
```

---

## Troubleshooting

### Error: Connection refused

**Problema:** El servicio no está corriendo

**Solución:**
1. Verificar que el servicio esté iniciado
2. Verificar que esté en el puerto correcto
3. Revisar logs en la terminal del servicio

### Error: Room is not available

**Problema:** La habitación ya tiene una reserva activa

**Solución:**
1. Cancelar la reserva existente
2. Usar otra habitación
3. Verificar disponibilidad con `GET /api/rooms`

### Error: Check-out date must be after check-in date

**Problema:** Las fechas son inválidas

**Solución:**
1. Asegurar que checkOutDate > checkInDate
2. Usar fechas futuras
3. Verificar el formato: "YYYY-MM-DD"

### Base de datos no existe

**Problema:** La base de datos booking_db no fue creada

**Solución:**
```bash
mysql -u root -p < /home/user/ubik1/microservicios/microreactivo/mysql-init.sql
```

---

## Arquitectura de Puertos

- **Gateway:** 8080
- **Booking Service:** 8083
- **Motel Management Service:** 8084
- **Products Service:** 8082

---

## Notas Importantes

1. **Orden de inicio:** Primero iniciar las bases de datos, luego los microservicios (orden no importa), finalmente el gateway.

2. **Datos de prueba:** Es necesario crear al menos un motel y habitaciones antes de poder crear reservas.

3. **Validación de emails:** El campo guestEmail debe tener formato de email válido (validado con @Email).

4. **Disponibilidad de habitaciones:** El sistema automáticamente gestiona la disponibilidad. No es necesario actualizar manualmente.

5. **Timestamps:** Los campos createdAt y updatedAt se gestionan automáticamente por la base de datos.
