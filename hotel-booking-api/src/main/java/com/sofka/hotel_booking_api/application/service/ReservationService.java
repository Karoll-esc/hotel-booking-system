package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.repository.GuestRepository;
import com.sofka.hotel_booking_api.domain.repository.ReservationRepository;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateReservationRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.ReservationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar las reservas del hotel.
 * Historia 3.1: Crear reserva para un huésped
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final GuestService guestService;

    public ReservationService(ReservationRepository reservationRepository,
                            RoomRepository roomRepository,
                            GuestRepository guestRepository,
                            GuestService guestService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.guestService = guestService;
    }

    /**
     * Crea una nueva reserva.
     * 
     * @param request datos de la reserva
     * @return la reserva creada
     */
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        // TODO: Implementar en fase GREEN
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
