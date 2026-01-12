package com.sofka.hotel_booking_api.domain.exception;

import com.sofka.hotel_booking_api.infrastructure.constants.ValidationMessages;

/**
 * Excepción de dominio lanzada cuando se intenta registrar una habitación 
 * con un número ya existente en el sistema.
 * 
 * <p>Esta excepción se lanza para cumplir con la regla de negocio RN-006 que 
 * establece que los números de habitación deben ser únicos.</p>
 * 
 * @author Sistema Hotel Booking
 * @version 1.0
 * @since 2026-01-07
 * @see ValidationMessages#DUPLICATE_ROOM_NUMBER_MESSAGE
 */
public class DuplicateRoomNumberException extends RuntimeException {
    
    /**
     * Crea una nueva excepción con el número de habitación duplicado.
     * 
     * @param roomNumber el número de habitación que ya existe
     */
    public DuplicateRoomNumberException(String roomNumber) {
        super(String.format(ValidationMessages.DUPLICATE_ROOM_NUMBER_MESSAGE, roomNumber));
    }
}
