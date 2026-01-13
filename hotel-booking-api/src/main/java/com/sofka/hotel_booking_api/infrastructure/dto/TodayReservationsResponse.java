package com.sofka.hotel_booking_api.infrastructure.dto;

import java.util.List;

/**
 * DTO para la respuesta de reservas del día.
 * Historia 5.2: Ver reservas del día
 */
public record TodayReservationsResponse(
        List<ReservationResponse> checkIns,
        List<ReservationResponse> checkOuts
) {
}
