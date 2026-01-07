# Hotel Booking System - Docker Setup

Este proyecto incluye configuración completa de Docker para ejecutar el sistema de reservas de hotel.

## 📦 Servicios

- **PostgreSQL 16**: Base de datos
- **Backend (Spring Boot)**: API REST en puerto 8080
- **Frontend (React + Vite)**: Aplicación web en puerto 80

## 🚀 Inicio rápido

### Levantar todos los servicios

```bash
docker-compose up -d
```

### Ver logs

```bash
# Todos los servicios
docker-compose logs -f

# Solo backend
docker-compose logs -f hotel-booking-api

# Solo frontend
docker-compose logs -f hotel-booking-frontend
```

### Detener los servicios

```bash
docker-compose down
```

### Detener y eliminar volúmenes (base de datos)

```bash
docker-compose down -v
```

## 🔧 Reconstruir imágenes

Si haces cambios en el código:

```bash
# Reconstruir todo
docker-compose up -d --build

# Reconstruir solo el backend
docker-compose up -d --build hotel-booking-api

# Reconstruir solo el frontend
docker-compose up -d --build hotel-booking-frontend
```

## 🌐 Acceso a los servicios

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **PostgreSQL**: localhost:5432
  - Database: `hotel_booking`
  - Usuario: `hotel_admin`
  - Password: `hotel_password`

## 📝 Variables de entorno

Puedes personalizar las variables de entorno editando el archivo `docker-compose.yml` o creando un archivo `.env`:

```env
# Base de datos
POSTGRES_DB=hotel_booking
POSTGRES_USER=hotel_admin
POSTGRES_PASSWORD=hotel_password

# Spring Boot
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/hotel_booking
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

## 🐛 Troubleshooting

### El backend no se conecta a la base de datos

Espera a que PostgreSQL esté completamente iniciado. El servicio tiene healthcheck configurado.

### Puerto ocupado

Si los puertos 80, 8080 o 5432 están ocupados, puedes cambiarlos en `docker-compose.yml`:

```yaml
ports:
  - "3000:80"  # Frontend en puerto 3000
  - "8081:8080"  # Backend en puerto 8081
  - "5433:5432"  # PostgreSQL en puerto 5433
```

### Ver el estado de los contenedores

```bash
docker-compose ps
```

### Acceder a la terminal de un contenedor

```bash
# Backend
docker exec -it hotel-booking-api sh

# Frontend
docker exec -it hotel-booking-frontend sh

# PostgreSQL
docker exec -it hotel-booking-postgres psql -U hotel_admin -d hotel_booking
```

## 🏗️ Arquitectura

```
┌─────────────────┐
│   Frontend      │
│  (React+Vite)   │  Puerto 80
│   + Nginx       │
└────────┬────────┘
         │
         │ /api -> proxy
         │
┌────────▼────────┐
│   Backend       │
│  (Spring Boot)  │  Puerto 8080
└────────┬────────┘
         │
         │ JDBC
         │
┌────────▼────────┐
│   PostgreSQL    │  Puerto 5432
└─────────────────┘
```

## 📋 Requisitos

- Docker 20.10+
- Docker Compose 2.0+

## 🔄 Desarrollo

Para desarrollo local sin Docker, consulta los README específicos de cada proyecto:
- [Backend README](./hotel-booking-api/HELP.md)
- [Frontend README](./hotel-booking-fronted/README.md)
