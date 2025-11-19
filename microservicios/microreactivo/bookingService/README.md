# Booking Service - Sistema de Reservas de Moteles

Microservicio reactivo para gestión de reservas de habitaciones de moteles, construido con Spring Boot 3.5.3 y Spring WebFlux.

## Características

- ✅ **Arquitectura Reactiva**: Basado en Spring WebFlux y R2DBC para operaciones no bloqueantes
- ✅ **Gestión Completa de Reservas**: Crear, confirmar, cancelar y consultar reservas
- ✅ **Integración con Motel Management**: Comunicación reactiva vía WebClient
- ✅ **Gestión Automática de Disponibilidad**: Actualiza automáticamente el estado de las habitaciones
- ✅ **Validaciones Robustas**: Validación de fechas, disponibilidad y datos requeridos
- ✅ **Cálculo Automático de Precios**: Basado en número de noches y precio por noche
- ✅ **Manejo de Estados**: PENDING → CONFIRMED → COMPLETED / CANCELLED
- ✅ **API RESTful**: Endpoints bien definidos y documentados
- ✅ **Pruebas Comprehensivas**: Suite completa de tests unitarios

## Tecnologías

- **Java 17+**
- **Spring Boot 3.5.3**
- **Spring WebFlux** - Framework reactivo
- **Spring Data R2DBC** - Acceso reactivo a base de datos
- **MySQL 8+** - Base de datos
- **R2DBC MySQL** - Driver reactivo para MySQL
- **JUnit 5 + Mockito** - Testing
- **Reactor Test** - Testing de flujos reactivos
- **Maven** - Gestión de dependencias

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                      │
└───────────────────────┬─────────────────────────────────────┘
                        │
          ┌─────────────┴─────────────┐
          │                           │
          ▼                           ▼
┌──────────────────────┐    ┌──────────────────────┐
│  Booking Service     │◄───│ Motel Management     │
│     (8083)           │    │    Service (8084)    │
└──────┬───────────────┘    └──────┬───────────────┘
       │                           │
       ▼                           ▼
  ┌─────────┐               ┌─────────────┐
  │  MySQL  │               │ PostgreSQL  │
  │booking_db│              │motel_mgmt_db│
  └─────────┘               └─────────────┘
```

## Modelo de Datos

### Entidad: Booking

```java
@Table("bookings")
public record Booking(
    Long id,                      // ID único (auto-generado)
    Long userId,                  // ID del usuario que reserva
    Long roomId,                  // ID de la habitación
    Long motelId,                 // ID del motel
    LocalDate checkInDate,        // Fecha de entrada
    LocalDate checkOutDate,       // Fecha de salida
    BigDecimal totalPrice,        // Precio total calculado
    String status,                // PENDING | CONFIRMED | CANCELLED | COMPLETED
    String guestName,             // Nombre del huésped
    String guestEmail,            // Email del huésped
    String guestPhone,            // Teléfono del huésped
    String specialRequests,       // Solicitudes especiales (opcional)
    LocalDateTime createdAt,      // Timestamp de creación
    LocalDateTime updatedAt       // Timestamp de última actualización
)
```

### DTOs

**BookingRequest** - Datos para crear una reserva:
```java
{
  "userId": Long,
  "roomId": Long,
  "motelId": Long,
  "checkInDate": "YYYY-MM-DD",     // Debe ser fecha futura
  "checkOutDate": "YYYY-MM-DD",    // Debe ser posterior a checkInDate
  "guestName": String,
  "guestEmail": String,            // Formato email válido
  "guestPhone": String,
  "specialRequests": String        // Opcional
}
```

**BookingResponse** - Respuesta enriquecida:
```java
{
  "id": Long,
  "userId": Long,
  "roomId": Long,
  "motelId": Long,
  "checkInDate": "YYYY-MM-DD",
  "checkOutDate": "YYYY-MM-DD",
  "totalPrice": BigDecimal,
  "status": String,
  "guestName": String,
  "guestEmail": String,
  "guestPhone": String,
  "specialRequests": String,
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  "motelName": String,             // Información del motel
  "roomNumber": String,            // Información de la habitación
  "roomType": String               // Información de la habitación
}
```

## API Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/bookings` | Obtener todas las reservas |
| GET | `/api/bookings/{id}` | Obtener reserva por ID |
| GET | `/api/bookings/user/{userId}` | Obtener reservas de un usuario |
| POST | `/api/bookings` | Crear nueva reserva |
| PUT | `/api/bookings/{id}/confirm` | Confirmar reserva |
| PUT | `/api/bookings/{id}/cancel` | Cancelar reserva |

## Lógica de Negocio

### Crear Reserva

1. ✅ Valida que checkOutDate > checkInDate
2. ✅ Verifica disponibilidad de la habitación en Motel Service
3. ✅ Calcula precio total: `pricePerNight × numberOfNights`
4. ✅ Crea booking con status PENDING
5. ✅ Guarda en base de datos
6. ✅ Marca habitación como NO disponible en Motel Service
7. ✅ Retorna respuesta enriquecida con datos del motel y habitación

### Confirmar Reserva

1. ✅ Busca booking por ID
2. ✅ Actualiza status a CONFIRMED
3. ✅ Actualiza timestamp
4. ✅ Retorna respuesta actualizada

### Cancelar Reserva

1. ✅ Busca booking por ID
2. ✅ Actualiza status a CANCELLED
3. ✅ Marca habitación como DISPONIBLE en Motel Service
4. ✅ Retorna respuesta actualizada

## Configuración

### application.yml

```yaml
server:
  port: 8083

spring:
  application:
    name: booking-service
  r2dbc:
    url: r2dbc:mysql://localhost:3306/booking_db
    username: root
    password: root
  data:
    r2dbc:
      repositories:
        enabled: true

services:
  motel-management:
    url: http://localhost:8084
  user-management:
    url: http://localhost:8081

logging:
  level:
    root: INFO
    com.ubik.bookingservice: DEBUG
    io.r2dbc.mysql: DEBUG

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

## Instalación y Configuración

### 1. Configurar Base de Datos

```bash
# Iniciar MySQL
sudo systemctl start mysql

# Ejecutar script de inicialización
mysql -u root -p < /home/user/ubik1/microservicios/microreactivo/mysql-init.sql
```

El script crea:
- Base de datos `booking_db`
- Tabla `bookings` con índices optimizados
- Usuario y permisos necesarios

### 2. Compilar el Proyecto

```bash
cd /home/user/ubik1/microservicios/microreactivo/bookingService
mvn clean install
```

### 3. Ejecutar Tests

```bash
mvn test
```

### 4. Iniciar el Servicio

```bash
mvn spring-boot:run
```

El servicio estará disponible en: `http://localhost:8083`

### 5. Verificar Salud

```bash
curl http://localhost:8083/actuator/health
```

Respuesta esperada: `{"status":"UP"}`

## Dependencias con Otros Servicios

### Motel Management Service (8084)

**Endpoints utilizados:**

- `GET /api/rooms/{id}` - Obtener información de habitación
- `GET /api/motels/{id}` - Obtener información de motel
- `PUT /api/rooms/{id}` - Actualizar disponibilidad de habitación

**Comunicación:**
- Protocolo: HTTP/REST
- Cliente: WebClient (reactivo)
- Timeout: Configurable
- Manejo de errores: Propagación reactiva

### User Management Service (8081)

**Estado:** Pendiente de implementación

**Uso futuro:**
- Validación de usuarios
- Información de perfil
- Historial de reservas

## Ejecución del Sistema Completo

### Orden de Inicio Recomendado:

1. **Bases de datos**
   ```bash
   sudo systemctl start mysql
   sudo systemctl start postgresql
   ```

2. **Motel Management Service** (8084)
   ```bash
   cd motelManegement
   mvn spring-boot:run
   ```

3. **Booking Service** (8083)
   ```bash
   cd bookingService
   mvn spring-boot:run
   ```

4. **API Gateway** (8080)
   ```bash
   cd gateway
   mvn spring-boot:run
   ```

## Pruebas

### Tests Unitarios

El proyecto incluye una suite comprehensiva de tests en:
- `BookingServiceTests.java` - Tests del servicio principal

**Casos cubiertos:**
- ✅ Creación exitosa de reserva
- ✅ Rechazo por habitación no disponible
- ✅ Validación de fechas inválidas
- ✅ Confirmación de reserva
- ✅ Cancelación de reserva
- ✅ Consultas por ID y usuario
- ✅ Cálculo correcto de precios
- ✅ Gestión de disponibilidad de habitaciones

### Tests de Integración

Ver: `TEST_BOOKING_ENDPOINTS.md` para pruebas completas con curl.

## Validaciones Implementadas

### Validaciones de Datos

- ✅ **userId**: Requerido, no nulo
- ✅ **roomId**: Requerido, no nulo
- ✅ **motelId**: Requerido, no nulo
- ✅ **checkInDate**: Requerido, debe ser fecha futura
- ✅ **checkOutDate**: Requerido, debe ser fecha futura y posterior a checkInDate
- ✅ **guestName**: Requerido
- ✅ **guestEmail**: Requerido, formato email válido
- ✅ **guestPhone**: Requerido
- ✅ **specialRequests**: Opcional

### Validaciones de Negocio

- ✅ La habitación debe estar disponible
- ✅ Debe haber al menos 1 noche de estadía
- ✅ Check-out debe ser posterior a check-in
- ✅ La habitación se bloquea al crear reserva
- ✅ La habitación se libera al cancelar reserva

## Manejo de Errores

### Errores HTTP Devueltos

- **400 Bad Request**: Datos inválidos o validaciones fallidas
  - Fechas inválidas
  - Habitación no disponible
  - Campos requeridos faltantes

- **404 Not Found**: Recurso no encontrado
  - Booking ID no existe
  - Habitación o motel no encontrado

- **500 Internal Server Error**: Errores del servidor
  - Problemas de comunicación con Motel Service
  - Errores de base de datos

## Monitoreo

### Actuator Endpoints

- `/actuator/health` - Estado del servicio
- `/actuator/info` - Información de la aplicación

### Logs

Los logs incluyen:
- Nivel DEBUG para `com.ubik.bookingservice`
- Nivel DEBUG para `io.r2dbc.mysql`
- Nivel INFO para el resto

## Mejoras Futuras

- [ ] Implementar paginación en consultas
- [ ] Agregar filtros por fecha y estado
- [ ] Implementar sistema de notificaciones por email
- [ ] Agregar validación de conflictos de fechas
- [ ] Implementar check-in/check-out automático
- [ ] Agregar gestión de pagos
- [ ] Implementar sistema de reviews
- [ ] Agregar soporte para modificación de reservas
- [ ] Implementar caché con Redis
- [ ] Agregar métricas con Micrometer

## Contribuir

1. Fork el proyecto
2. Crear rama de feature (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## Licencia

Este proyecto es parte del sistema de gestión de moteles Ubik.

## Contacto

Para preguntas o soporte, contactar al equipo de desarrollo.

---

**Versión:** 1.0.0-SNAPSHOT
**Última actualización:** 2025-11-19
