package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.exception.DuplicateRoomNumberException;
import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
     *
     * @param request datos de la habitación a registrar
     * @return la habitación registrada
     * @throws DuplicateRoomNumberException si el número de habitación ya existe
     * 
     * Reglas de negocio aplicadas:
     * - RN-006: El número de habitación debe ser único
     * - RN-006: La capacidad debe estar entre 1 y 10 personas
     * - RN-006: El precio debe ser mayor a 0
     */
    @Transactional
    public RoomResponse registerRoom(CreateRoomRequest request) {
        // 1. Validar que el número de habitación no esté duplicado (RN-006)
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new DuplicateRoomNumberException(request.getRoomNumber());
        }

        // 2. Crear la entidad Room desde el request
        Room room = new Room(
            request.getRoomNumber(),
            request.getRoomType(),
            request.getCapacity(),
            request.getPricePerNight()
        );

        // 3. Guardar la habitación en la base de datos
        Room savedRoom = roomRepository.save(room);

        // 4. Convertir la entidad a DTO de respuesta
        return RoomResponse.fromEntity(savedRoom);
    }

    /**
     * Obtiene todas las habitaciones registradas en el sistema.
     *
     * @return lista de todas las habitaciones
     */
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll()
            .stream()
            .map(RoomResponse::fromEntity)
            .collect(Collectors.toList());
    }
}
