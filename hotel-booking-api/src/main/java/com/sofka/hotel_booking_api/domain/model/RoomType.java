package com.sofka.hotel_booking_api.domain.model;

/**
 * Tipos de habitación disponibles en el hotel.
 * Según RN-006: Validaciones de Habitación
 */
public enum RoomType {
    STANDARD("Estándar"),
    SUPERIOR("Superior"),
    SUITE("Suite");

    private final String displayName;

    RoomType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
