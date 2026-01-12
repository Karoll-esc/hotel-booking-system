-- Inicialización de la base de datos Hotel Booking
-- Este script se ejecuta automáticamente al crear el contenedor

-- Crear extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Crear schema para la aplicación (opcional)
CREATE SCHEMA IF NOT EXISTS hotel;

-- Mensaje de confirmación
DO $$
BEGIN
    RAISE NOTICE 'Base de datos hotel_booking inicializada correctamente';
    RAISE NOTICE 'Usuario: hotel_admin';
    RAISE NOTICE 'Las tablas serán creadas automáticamente por Hibernate';
END $$;
