package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.infrastructure.dto.CreateGuestRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO para crear una nueva reserva.
 * RN-004: Validaciones de Reserva
 * RN-005: Capacidad de Habitaciones
 */
public record CreateReservationRequest(
        @Valid
        @NotNull(message = "Los datos del huésped son obligatorios")
        CreateGuestRequest guest,

        @NotNull(message = "El ID de la habitación es obligatorio")
        Long roomId,

        @NotNull(message = "La fecha de entrada es obligatoria")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate checkInDate,

        @NotNull(message = "La fecha de salida es obligatoria")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate checkOutDate,

        @NotNull(message = "El número de huéspedes es obligatorio")
        @Min(value = 1, message = "Debe haber al menos 1 huésped")
        @Max(value = 10, message = "El número de huéspedes no puede exceder 10")
        Integer numberOfGuests
) {
}
