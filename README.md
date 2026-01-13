# 🏨 Hotel Booking System

Sistema de gestión de reservas de hotel desarrollado con **Spring Boot** (Backend) siguiendo arquitectura limpia y principios SOLID.

> ⚠️ **Nota:** El frontend (React + TypeScript) no se completó. Este README se centra en el backend.

---

## 📑 Tabla de Contenidos

- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Patrón de Diseño](#-patrón-de-diseño-utilizado)
- [Diagramas](#-diagramas)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Tecnologías](#-tecnologías)
- [Ejecutar el Proyecto](#-ejecutar-el-proyecto)
- [Ejecutar Tests y Pipeline](#-ejecutar-tests-y-pipeline)
- [API Endpoints](#-api-endpoints)
- [Reglas de Negocio](#-reglas-de-negocio)

---

## 🏗️ Arquitectura del Sistema

El backend implementa una **Arquitectura en Capas** inspirada en **Clean Architecture** (Arquitectura Limpia)
### Principios Aplicados

| Principio | Implementación |
|-----------|----------------|
| **Separación de responsabilidades** | Cada capa tiene una responsabilidad única y bien definida |
| **Inversión de dependencias** | Las capas internas no dependen de las externas |
| **Independencia del framework** | El dominio no conoce Spring ni JPA directamente |
| **Testabilidad** | Cada capa puede probarse de forma aislada |

### Capas de la Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                    🌐 INFRASTRUCTURE LAYER                       │
│  (Controllers, DTOs, Exception Handlers, Config)                │
│  Responsabilidad: Comunicación con el mundo exterior            │
├─────────────────────────────────────────────────────────────────┤
│                    ⚙️ APPLICATION LAYER                          │
│  (Services)                                                     │
│  Responsabilidad: Orquestación de casos de uso                  │
├─────────────────────────────────────────────────────────────────┤
│                    💎 DOMAIN LAYER                               │
│  (Entities, Repositories Interfaces, Business Exceptions)       │
│  Responsabilidad: Lógica de negocio pura                        │
└─────────────────────────────────────────────────────────────────┘
```

### Descripción de Capas

#### 1. **Domain Layer** (Capa de Dominio)
La capa más interna y estable. Contiene:
- **Entidades**: `Reservation`, `Room`, `Guest`
- **Enums**: `ReservationStatus`, `RoomType`
- **Repositorios (interfaces)**: Contratos de persistencia
- **Excepciones de dominio**: `RoomNotFoundException`, `InvalidDateRangeException`

```
domain/
├── model/
│   ├── Reservation.java      # Entidad principal con lógica de negocio
│   ├── Room.java             # Entidad de habitación
│   ├── Guest.java            # Entidad de huésped
│   ├── ReservationStatus.java
│   └── RoomType.java
├── repository/
│   ├── ReservationRepository.java
│   ├── RoomRepository.java
│   └── GuestRepository.java
└── exception/
    ├── RoomNotFoundException.java
    ├── ReservationNotFoundException.java
    ├── InvalidDateRangeException.java
    └── DuplicateRoomNumberException.java
```

#### 2. **Application Layer** (Capa de Aplicación)
Orquesta los casos de uso de negocio:
- **ReservationService**: Crear reservas, búsquedas, check-in/check-out
- **PaymentService**: Confirmar pagos
- **RoomService**: Gestión de habitaciones
- **GuestService**: Registro de huéspedes

```
application/
└── service/
    ├── ReservationService.java   # Casos de uso de reservas
    ├── PaymentService.java       # Confirmación de pagos
    ├── RoomService.java          # CRUD de habitaciones
    └── GuestService.java         # Registro de huéspedes
```

#### 3. **Infrastructure Layer** (Capa de Infraestructura)
Implementaciones concretas y comunicación externa:
- **Controllers**: Endpoints REST
- **DTOs**: Objetos de transferencia de datos
- **Exception Handlers**: Manejo global de errores
- **Config**: Configuración de Spring Security

```
infrastructure/
├── controller/
│   ├── ReservationController.java
│   └── RoomController.java
├── dto/
│   ├── CreateReservationRequest.java
│   ├── ReservationResponse.java
│   ├── CreateRoomRequest.java
│   └── ...
├── exception/
│   └── GlobalExceptionHandler.java
└── config/
    └── OpenApiConfig.java
```

---

## 🎯 Patrón de Diseño Principal

### Factory Method - Creacional

**¿Qué es?**  
El patrón Factory Method define una interfaz para crear objetos, pero permite que las subclases o métodos estáticos decidan qué clase instanciar. Encapsula la lógica de creación de objetos complejos.

**¿Por qué se eligió como patrón principal?**

| Beneficio | Aplicación en el Proyecto |
|-----------|---------------------------|
| **Encapsulación de creación** | La conversión Entity → DTO está centralizada en un solo lugar |
| **Código limpio y legible** | `ReservationResponse.fromEntity(reservation)` es autodescriptivo |
| **Principio SRP** | El DTO sabe cómo crearse a sí mismo desde una entidad |
| **Mantenibilidad** | Si cambia la entidad, solo se modifica el factory method |
| **Reutilización** | El mismo método se usa en múltiples servicios |

**¿Dónde se usa en el proyecto?**

```java
// En ReservationResponse.java (infrastructure/dto)
public record ReservationResponse(
        Long id,
        String reservationNumber,
        GuestResponse guest,
        RoomResponse room,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer numberOfGuests,
        Long numberOfNights,
        BigDecimal totalAmount,
        ReservationStatus status,
        LocalDateTime createdAt
) {
    /**
     * FACTORY METHOD: Crea un DTO desde una entidad de dominio
     * Encapsula toda la lógica de transformación
     */
    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                GuestResponse.fromEntity(reservation.getGuest()),  // Factory anidado
                RoomResponse.fromEntity(reservation.getRoom()),    // Factory anidado
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getNumberOfGuests(),
                reservation.getNumberOfNights(),
                reservation.getTotalAmount(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
```

**Uso en los servicios:**

```java
// En ReservationService.java - Se usa el Factory Method
public ReservationResponse createReservation(CreateReservationRequest request) {
    // ... lógica de negocio ...
    Reservation savedReservation = reservationRepository.save(reservation);
    
    // Factory Method: conversión limpia y encapsulada
    return ReservationResponse.fromEntity(savedReservation);
}

// En búsquedas - Lista de conversiones
public List<ReservationResponse> searchReservations(String guestName) {
    return reservationRepository.findByGuestNameContainingIgnoreCase(guestName)
            .stream()
            .map(ReservationResponse::fromEntity)  // Factory Method como referencia
            .collect(Collectors.toList());
}
```

**Factory Methods implementados en el proyecto:**

| Clase | Método | Propósito |
|-------|--------|-----------|
| `ReservationResponse` | `fromEntity(Reservation)` | Convierte reserva a DTO de respuesta |
| `RoomResponse` | `fromEntity(Room)` | Convierte habitación a DTO de respuesta |
| `GuestResponse` | `fromEntity(Guest)` | Convierte huésped a DTO de respuesta |

### Otros Patrones Aplicados

| Patrón | Tipo | Uso en el Proyecto |
|--------|------|-------------------|
| **Repository** | Estructural | Abstracción de persistencia (`ReservationRepository`) |
| **DTO Pattern** | Estructural | Separación entre entidades y respuestas API |
| **Service Layer** | Arquitectónico | Encapsulación de lógica de negocio |
| **Dependency Injection** | Creacional | Inyección de dependencias vía constructor |

---

## 📊 Diagramas

### Diagrama de Arquitectura por Capas

```
┌────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTE (HTTP)                                 │
└─────────────────────────────────────┬──────────────────────────────────────┘
                                      │
                                      ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                         INFRASTRUCTURE LAYER                                │
│  ┌──────────────────┐  ┌───────────────┐  ┌─────────────────────────────┐  │
│  │   Controllers    │  │     DTOs      │  │   GlobalExceptionHandler    │  │
│  │                  │  │               │  │                             │  │
│  │ • Reservation    │  │ • Request     │  │ • 400 Bad Request           │  │
│  │ • Room           │  │ • Response    │  │ • 404 Not Found             │  │
│  └────────┬─────────┘  └───────────────┘  │ • 409 Conflict              │  │
│           │                               └─────────────────────────────┘  │
└───────────┼────────────────────────────────────────────────────────────────┘
            │
            ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                          APPLICATION LAYER                                  │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │                           SERVICES                                    │  │
│  │                                                                       │  │
│  │  ReservationService    PaymentService    RoomService    GuestService │  │
│  │  • createReservation   • confirmPayment  • createRoom   • register   │  │
│  │  • searchReservations                    • findAll                   │  │
│  │  • getTodayReservations                  • findAvailable             │  │
│  │  • checkIn / checkOut                                                │  │
│  └──────────────────────────────────────────────────────────────────────┘  │
└───────────┬────────────────────────────────────────────────────────────────┘
            │
            ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                            DOMAIN LAYER                                     │
│  ┌──────────────────┐  ┌───────────────────┐  ┌──────────────────────────┐ │
│  │     ENTITIES     │  │   REPOSITORIES    │  │      EXCEPTIONS          │ │
│  │                  │  │   (Interfaces)    │  │                          │ │
│  │  • Reservation   │  │                   │  │  • RoomNotFound          │ │
│  │  • Room          │  │  • Reservation    │  │  • ReservationNotFound   │ │
│  │  • Guest         │  │  • Room           │  │  • InvalidDateRange      │ │
│  │                  │  │  • Guest          │  │  • DuplicateRoomNumber   │ │
│  └──────────────────┘  └───────────────────┘  └──────────────────────────┘ │
└───────────┬────────────────────────────────────────────────────────────────┘
            │
            ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                        PERSISTENCE (Spring Data JPA)                        │
│                              PostgreSQL Database                            │
└────────────────────────────────────────────────────────────────────────────┘
```

### Diagrama de Flujo: Crear Reserva

```
┌─────────┐     ┌────────────────────┐     ┌───────────────────┐
│ Cliente │────▶│ ReservationController│────▶│ ReservationService │
└─────────┘     └────────────────────┘     └─────────┬─────────┘
                                                      │
                      ┌───────────────────────────────┼───────────────────────┐
                      │                               ▼                       │
                      │                    ┌──────────────────┐               │
                      │                    │ 1. Validar fechas │               │
                      │                    │    (RN-004)       │               │
                      │                    └────────┬─────────┘               │
                      │                             │                         │
                      │                             ▼                         │
                      │                    ┌──────────────────┐               │
                      │                    │ 2. Buscar Room   │               │
                      │                    │    (Repository)   │               │
                      │                    └────────┬─────────┘               │
                      │                             │                         │
                      │                             ▼                         │
                      │                    ┌──────────────────┐               │
                      │                    │ 3. Validar       │               │
                      │                    │    capacidad     │               │
                      │                    └────────┬─────────┘               │
                      │                             │                         │
                      │                             ▼                         │
                      │                    ┌──────────────────┐               │
                      │                    │ 4. Verificar     │               │
                      │                    │  disponibilidad  │               │
                      │                    │  (sin solapamiento)│              │
                      │                    └────────┬─────────┘               │
                      │                             │                         │
                      │                             ▼                         │
                      │                    ┌──────────────────┐               │
                      │                    │ 5. Registrar     │               │
                      │                    │    huésped       │               │
                      │                    └────────┬─────────┘               │
                      │                             │                         │
                      │                             ▼                         │
                      │                    ┌──────────────────┐               │
                      │                    │ 6. Calcular      │               │
                      │                    │    total         │               │
                      │                    └────────┬─────────┘               │
                      │                             │                         │
                      │                             ▼                         │
                      │                    ┌──────────────────┐               │
                      │                    │ 7. Crear y       │               │
                      │                    │  guardar reserva │               │
                      │                    │  (Status: PENDING)│              │
                      │                    └────────┬─────────┘               │
                      │                             │                         │
                      └─────────────────────────────┼─────────────────────────┘
                                                    │
                                                    ▼
                                          ┌──────────────────┐
                                          │ ReservationResponse│
                                          │  (201 CREATED)    │
                                          └──────────────────┘
```

### Diagrama de Secuencia: Confirmar Pago

```
┌────────┐          ┌─────────────────────┐       ┌────────────────┐       ┌──────────────────────┐       ┌────────────┐
│Cliente │          │ReservationController│       │ PaymentService │       │ReservationRepository │       │ Reservation│
└───┬────┘          └──────────┬──────────┘       └───────┬────────┘       └──────────┬───────────┘       └─────┬──────┘
    │                          │                          │                           │                         │
    │ POST /api/reservations   │                          │                           │                         │
    │ /{id}/confirm-payment    │                          │                           │                         │
    │ ─────────────────────────▶                          │                           │                         │
    │                          │                          │                           │                         │
    │                          │ confirmPayment(id,       │                           │                         │
    │                          │ method, amount, ref)     │                           │                         │
    │                          │ ─────────────────────────▶                           │                         │
    │                          │                          │                           │                         │
    │                          │                          │ 1. validatePaymentMethod()│                         │
    │                          │                          │ ◀─────────────────────────│                         │
    │                          │                          │                           │                         │
    │                          │                          │ 2. validateReference()    │                         │
    │                          │                          │ ◀─────────────────────────│                         │
    │                          │                          │                           │                         │
    │                          │                          │ 3. findById(id)           │                         │
    │                          │                          │ ──────────────────────────▶                         │
    │                          │                          │                           │                         │
    │                          │                          │    Reservation            │                         │
    │                          │                          │ ◀──────────────────────────                         │
    │                          │                          │                           │                         │
    │                          │                          │ 4. validate status == PENDING                       │
    │                          │                          │ ──────────────────────────────────────────────────▶│
    │                          │                          │                           │                         │
    │                          │                          │ 5. validate amount matches│                         │
    │                          │                          │ ──────────────────────────────────────────────────▶│
    │                          │                          │                           │                         │
    │                          │                          │ 6. confirmPayment()       │                         │
    │                          │                          │ ──────────────────────────────────────────────────▶│
    │                          │                          │                           │       status=CONFIRMED  │
    │                          │                          │                           │ ◀─────────────────────────
    │                          │                          │                           │                         │
    │                          │                          │ 7. save(reservation)      │                         │
    │                          │                          │ ──────────────────────────▶                         │
    │                          │                          │                           │                         │
    │                          │       void               │                           │                         │
    │                          │ ◀─────────────────────────                           │                         │
    │                          │                          │                           │                         │
    │     200 OK               │                          │                           │                         │
    │ ◀─────────────────────────                          │                           │                         │
    │                          │                          │                           │                         │
```

### Diagrama de Estados: Reserva

```
                                    ┌─────────────────┐
                                    │     INICIO      │
                                    └────────┬────────┘
                                             │
                                             │ crear reserva
                                             ▼
                               ┌─────────────────────────┐
                               │        PENDING          │
                               │  (Pendiente de pago)    │
                               │                         │
                               └─────────────┬───────────┘
                                             │
                    ┌────────────────────────┴────────────────────────┐
                    │                                                 │
                    │ confirmar pago                                  │ cancelar
                    ▼                                                 ▼
        ┌───────────────────┐                             ┌───────────────────┐
        │     CONFIRMED     │                             │     CANCELLED     │
        │   (Pago recibido) │                             │   (Cancelada)     │
        │                   │                             │                   │
        └─────────┬─────────┘                             └───────────────────┘
                  │
                  │ check-in
                  ▼
        ┌───────────────────┐
        │      ACTIVE       │
        │  (Huésped en      │
        │   hotel)          │
        └─────────┬─────────┘
                  │
                  │ check-out
                  ▼
        ┌───────────────────┐
        │     COMPLETED     │
        │   (Estadía        │
        │    finalizada)    │
        └───────────────────┘
```

---

## 📁 Estructura del Proyecto

```
hotel-booking-system/
├── 📂 hotel-booking-api/           # Backend Spring Boot
│   ├── 📂 src/
│   │   ├── 📂 main/java/com/sofka/hotel_booking_api/
│   │   │   ├── 📂 domain/          # Capa de dominio
│   │   │   │   ├── model/          # Entidades JPA
│   │   │   │   ├── repository/     # Interfaces de repositorio
│   │   │   │   └── exception/      # Excepciones de dominio
│   │   │   ├── 📂 application/     # Capa de aplicación
│   │   │   │   └── service/        # Servicios de negocio
│   │   │   ├── 📂 infrastructure/  # Capa de infraestructura
│   │   │   │   ├── controller/     # REST Controllers
│   │   │   │   ├── dto/            # Data Transfer Objects
│   │   │   │   ├── exception/      # Exception Handlers
│   │   │   │   └── config/         # Configuraciones
│   │   │   └── 📄 HotelBookingApiApplication.java
│   │   └── 📂 resources/
│   │       ├── application.yaml    # Configuración de Spring
│   │       └── db/init.sql         # Script inicial de BD
│   └── 📂 src/test/                # Tests
│       └── java/com/sofka/hotel_booking_api/
│           ├── application/service/   # Tests unitarios
│           ├── domain/model/          # Tests de entidades
│           └── infrastructure/controller/  # Tests de integración
├── 📂 hotel-booking-frontend/      # Frontend (incompleto)
├── 📂 docs/                        # Documentación
│   ├── HISTORIAS_USUARIO.md
│   └── REGLAS_NEGOCIO.md
├── 📄 docker-compose.yml           # Orquestación Docker
└── 📄 README.md
```

---

## 🛠️ Tecnologías

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 17 | Lenguaje de programación |
| **Spring Boot** | 3.4.1 | Framework principal |
| **Spring Data JPA** | - | Persistencia de datos |
| **Spring Security** | - | Seguridad (OAuth2 preparado) |
| **Spring Validation** | - | Validación de DTOs |
| **PostgreSQL** | 16 | Base de datos |
| **H2 Database** | - | BD en memoria para tests |
| **Gradle** | 8.x | Build tool |
| **JaCoCo** | 0.8.11 | Cobertura de código |
| **Docker** | - | Contenedorización |

---

## 🚀 Ejecutar el Proyecto

### Prerrequisitos
- Docker y Docker Compose instalados
- Java 17+ (para desarrollo local)

### Opción 1: Con Docker (Recomendado)

```bash
# Clonar el repositorio
git clone <url-del-repo>
cd hotel-booking-system

# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f hotel-booking-api
```

### Opción 2: Desarrollo Local

```bash
# 1. Iniciar solo PostgreSQL
docker-compose up -d postgres

# 2. Ejecutar el backend
cd hotel-booking-api
./gradlew bootRun
```

### Acceso a Servicios

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| Backend API | http://localhost:8080 | - |
| PostgreSQL | localhost:5432 | hotel_admin / hotel_password |
| pgAdmin | http://localhost:5050 | admin@hotel.com / admin123 |

---

## 🧪 Ejecutar Tests y Pipeline

### Ejecutar Tests Unitarios e Integración

```bash
cd hotel-booking-api

# Ejecutar todos los tests
./gradlew test

# En Windows
.\gradlew.bat test
```

### Ejecutar Tests con Reporte de Cobertura

```bash
# Genera reporte HTML en build/reports/jacoco/test/html/index.html
./gradlew test jacocoTestReport

# En Windows
.\gradlew.bat test jacocoTestReport
```

### Verificar Cobertura Mínima

```bash
# Falla si no se alcanza el umbral de cobertura configurado
./gradlew test  

# En Windows
.\gradlew.bat test jacocoTestCoverageVerification
```

### Umbrales de Cobertura Configurados

| Capa | Cobertura Mínima |
|------|------------------|
| Services (`application.service.*`) | 70% |
| Models con lógica (`domain.model.Reservation`) | 50% |

### Ver Reporte de Cobertura

Después de ejecutar los tests, abrir:
```
hotel-booking-api/build/reports/jacoco/test/html/index.html
```

### Ver Reporte de Tests

```
hotel-booking-api/build/reports/tests/test/index.html
```

### Pipeline Completo (Build + Test + Cobertura)

```bash
cd hotel-booking-api

# Ejecutar verificación completa
./gradlew clean build

# En Windows
.\gradlew.bat clean build
```

Este comando ejecuta:
1. ✅ Compilación del código
2. ✅ Ejecución de tests unitarios
3. ✅ Ejecución de tests de integración
4. ✅ Generación de reporte JaCoCo
5. ✅ Verificación de cobertura mínima

---

## 📡 API Endpoints

### Habitaciones (Rooms)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/rooms` | Crear habitación |
| GET | `/api/rooms` | Listar todas las habitaciones |
| GET | `/api/rooms/available` | Listar habitaciones disponibles por fecha |

### Reservas (Reservations)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/reservations` | Crear reserva |
| POST | `/api/reservations/{id}/confirm-payment` | Confirmar pago |
| GET | `/api/reservations/search` | Buscar por número o nombre |
| GET | `/api/reservations/today` | Reservas del día (check-in/out) |
| POST | `/api/reservations/{id}/check-in` | Realizar check-in |
| POST | `/api/reservations/{id}/check-out` | Realizar check-out |

---

## 📋 Reglas de Negocio

Las principales reglas implementadas:

| Código | Regla | Descripción |
|--------|-------|-------------|
| RN-003 | Tiempo Límite Pago | 24 horas para confirmar pago |
| RN-004 | Validaciones Reserva | Mín 1 noche, Máx 30 noches |
| RN-005 | Capacidad | Validar huéspedes vs capacidad habitación |
| RN-009 | Estados Reserva | PENDING → CONFIRMED → ACTIVE → COMPLETED |

📖 Ver documentación completa: [REGLAS_NEGOCIO.md](docs/REGLAS_NEGOCIO.md)

---

## 📖 Documentación Adicional

- [Historias de Usuario](docs/HISTORIAS_USUARIO.md)
- [Reglas de Negocio](docs/REGLAS_NEGOCIO.md)
