package com.sofka.hotel_booking_api.domain.exception;

/**
 * Excepción lanzada cuando se intenta registrar una habitación con un número ya existente.
 * Según RN-006: Los números de habitación deben ser únicos
 */
public class DuplicateRoomNumberException extends RuntimeException {
    
    public DuplicateRoomNumberException(String roomNumber) {
        super(String.format("Ya existe una habitación registrada con el número '%s'", roomNumber));
    }
}
