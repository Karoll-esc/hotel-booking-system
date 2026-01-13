package com.sofka.hotel_booking_api.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO con los detalles de una cancelación de reserva.
 * Incluye el cálculo de reembolso según la política RN-001.
 * Historia 7.1: Cancelar reserva existente
 */
public record CancelReservationResponse(
        String reservationNumber,
        LocalDateTime cancellationDate,
        BigDecimal totalAmount,
        BigDecimal refundAmount,
        BigDecimal penaltyAmount,
        int refundPercentage
) {
}
