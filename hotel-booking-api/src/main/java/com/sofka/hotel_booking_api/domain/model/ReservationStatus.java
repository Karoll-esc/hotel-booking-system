package com.sofka.hotel_booking_api.domain.model;

/**
 * Estados posibles de una reserva en el sistema.
 * Según RN-009: Estados de Reserva
 */
public enum ReservationStatus {
    /**
     * Reserva creada pero pendiente de pago.
     * Tiene tiempo límite de 24 horas para confirmar pago (RN-003).
     */
    PENDING("Pendiente"),

    /**
     * Reserva con pago confirmado.
     * La habitación está garantizada para el huésped.
     */
    CONFIRMED("Confirmada"),

    /**
     * Reserva activa - el huésped ha hecho check-in.
     * La habitación está ocupada.
     */
    ACTIVE("Activa"),

    /**
     * Reserva completada - el huésped ha hecho check-out.
     * La estadía finalizó normalmente.
     */
    COMPLETED("Completada"),

    /**
     * Reserva cancelada por el huésped o el hotel.
     * La habitación queda disponible nuevamente.
     */
    CANCELLED("Cancelada"),

    /**
     * Reserva expirada por falta de pago.
     * Se alcanzó el límite de tiempo sin confirmar pago.
     */
    EXPIRED("Expirada");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
