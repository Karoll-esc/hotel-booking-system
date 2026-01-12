package com.sofka.hotel_booking_api.domain.exception;

/**
 * Excepción de dominio lanzada cuando se proporciona un rango de fechas inválido.
 * 
 * <p>Esta excepción se utiliza para validar consultas de disponibilidad 
 * y operaciones que requieren rangos de fechas válidos.</p>
 * 
 * @author Sistema Hotel Booking
 * @version 1.0
 * @since 2026-01-12
 */
public class InvalidDateRangeException extends RuntimeException {
    
    /**
     * Crea una nueva excepción con un mensaje específico.
     * 
     * @param message el mensaje de error descriptivo
     */
    public InvalidDateRangeException(String message) {
        super(message);
    }
}
