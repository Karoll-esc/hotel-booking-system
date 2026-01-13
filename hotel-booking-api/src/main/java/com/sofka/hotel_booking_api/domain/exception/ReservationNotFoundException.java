package com.sofka.hotel_booking_api.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una reserva.
 */
public class ReservationNotFoundException extends RuntimeException {
    
    public ReservationNotFoundException(Long id) {
        super(String.format("No se encontró la reserva con ID: %d", id));
    }
    
    public ReservationNotFoundException(String reservationNumber) {
        super(String.format("No se encontró la reserva con número: %s", reservationNumber));
    }
}
