package com.sofka.hotel_booking_api.infrastructure.dto;

import jakarta.validation.constraints.*;

/**
 * DTO para crear o actualizar un huésped.
 * RN-007: Validaciones de Huésped
 */
public record CreateGuestRequest(
        @NotNull(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        String firstName,

        @NotNull(message = "El apellido es obligatorio")
        @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
        String lastName,

        @NotNull(message = "El número de documento es obligatorio")
        @Size(min = 5, max = 50, message = "El documento debe tener entre 5 y 50 caracteres")
        String documentNumber,

        @NotNull(message = "El correo electrónico es obligatorio")
        @Email(message = "El correo electrónico debe tener un formato válido")
        String email,

        @NotNull(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "El teléfono debe tener un formato válido")
        String phone
) {
}
