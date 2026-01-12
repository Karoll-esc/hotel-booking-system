package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.domain.model.Reservation;
import com.sofka.hotel_booking_api.domain.model.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta con información de una reserva.
 */
public record ReservationResponse(
        Long id,
        String reservationNumber,
        GuestResponse guest,
        RoomResponse room,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        Integer numberOfGuests,
        Long numberOfNights,
        BigDecimal totalAmount,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime
) {
    /**
     * Crea un ReservationResponse desde una entidad Reservation.
     * @param reservation la entidad Reservation
     * @return el DTO de respuesta
     */
    public static ReservationResponse fromEntity(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                GuestResponse.fromEntity(reservation.getGuest()),
                RoomResponse.fromEntity(reservation.getRoom()),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                reservation.getNumberOfGuests(),
                reservation.getNumberOfNights(),
                reservation.getTotalAmount(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getCheckInTime(),
                reservation.getCheckOutTime()
        );
    }
}
