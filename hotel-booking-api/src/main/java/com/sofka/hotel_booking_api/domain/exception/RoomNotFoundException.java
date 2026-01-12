package com.sofka.hotel_booking_api.domain.exception;

/**
 * Excepción de dominio lanzada cuando se intenta acceder a una habitación 
 * que no existe en el sistema.
 * 
 * <p>Esta excepción se utiliza en operaciones de consulta, actualización 
 * y eliminación de habitaciones.</p>
 * 
 * @author Sistema Hotel Booking
 * @version 1.0
 * @since 2026-01-12
 */
public class RoomNotFoundException extends RuntimeException {
    
    /**
     * Crea una nueva excepción con el ID de la habitación no encontrada.
     * 
     * @param roomId el ID de la habitación que no fue encontrada
     */
    public RoomNotFoundException(Long roomId) {
        super(String.format("No se encontró la habitación con ID: %d", roomId));
    }
}
