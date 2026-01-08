package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import org.springframework.stereotype.Service;

/**
 * Servicio para gestionar las habitaciones del hotel.
 */
@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Registra una nueva habitación en el sistema.
     * Según Historia 2.1: Registrar habitaciones del hotel - Escenario: Registro exitoso
     *
     * @param request datos de la habitación a registrar
     * @return la habitación registrada
     */
    public RoomResponse registerRoom(CreateRoomRequest request) {
        // TODO: Implementar en fase GREEN
        throw new UnsupportedOperationException("Método no implementado aún - fase RED de TDD");
    }
}
