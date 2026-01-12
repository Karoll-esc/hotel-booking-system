package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.domain.model.Guest;

import java.time.LocalDateTime;

/**
 * DTO de respuesta con información de un huésped.
 */
public record GuestResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String documentNumber,
        String email,
        String phone,
        LocalDateTime createdAt
) {
    /**
     * Crea un GuestResponse desde una entidad Guest.
     * @param guest la entidad Guest
     * @return el DTO de respuesta
     */
    public static GuestResponse fromEntity(Guest guest) {
        return new GuestResponse(
                guest.getId(),
                guest.getFirstName(),
                guest.getLastName(),
                guest.getFullName(),
                guest.getDocumentNumber(),
                guest.getEmail(),
                guest.getPhone(),
                guest.getCreatedAt()
        );
    }
}
