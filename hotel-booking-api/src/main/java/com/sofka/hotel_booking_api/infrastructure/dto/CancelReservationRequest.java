package com.sofka.hotel_booking_api.infrastructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para solicitar la cancelación de una reserva.
 * Historia 7.1: Cancelar reserva existente
 */
public record CancelReservationRequest(
        @NotBlank(message = "El motivo de cancelación es obligatorio")
        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        String reason
) {
}
