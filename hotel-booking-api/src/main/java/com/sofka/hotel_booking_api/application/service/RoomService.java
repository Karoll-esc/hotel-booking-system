package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.exception.DuplicateRoomNumberException;
import com.sofka.hotel_booking_api.domain.exception.InvalidDateRangeException;
import com.sofka.hotel_booking_api.domain.exception.RoomNotFoundException;
import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /**
     * Obtiene una habitación por su ID.
     *
     * @param id el ID de la habitación
     * @return la habitación encontrada
     * @throws RoomNotFoundException si la habitación no existe
     */
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));
        return RoomResponse.fromEntity(room);
    }

    /**
     * Actualiza una habitación existente.
     *
     * @param id el ID de la habitación a actualizar
     * @param request los nuevos datos de la habitación
     * @return la habitación actualizada
     * @throws RoomNotFoundException si la habitación no existe
     */
    @Transactional
    public RoomResponse updateRoom(Long id, CreateRoomRequest request) {
        // 1. Verificar que la habitación existe
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException(id));

        // 2. Actualizar los campos de la habitación
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setPricePerNight(request.getPricePerNight());

        // 3. Guardar los cambios
        Room updatedRoom = roomRepository.save(room);

        // 4. Retornar la respuesta
        return RoomResponse.fromEntity(updatedRoom);
    }

    /**
     * Elimina una habitación del sistema.
     *
     * @param id el ID de la habitación a eliminar
     * @throws RoomNotFoundException si la habitación no existe
     */
    @Transactional
    public void deleteRoom(Long id) {
        // 1. Verificar que la habitación existe
        if (!roomRepository.existsById(id)) {
            throw new RoomNotFoundException(id);
        }

        // 2. Eliminar la habitación
        roomRepository.deleteById(id);
    }

    /**
     * Obtiene las habitaciones disponibles en un rango de fechas.
     * Historia 2.2: Consultar estado de ocupación de habitaciones
     *
     * @param checkIn fecha de entrada
     * @param checkOut fecha de salida
     * @param roomType tipo de habitación (opcional)
     * @return lista de habitaciones disponibles
     * @throws InvalidDateRangeException si el rango de fechas es inválido
     */
    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, RoomType roomType) {
        // 1. Validar que checkIn no sea en el pasado
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException("La fecha de entrada no puede ser en el pasado");
        }

        // 2. Validar que checkIn sea antes de checkOut
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new InvalidDateRangeException("La fecha de entrada debe ser anterior a la fecha de salida");
        }

        // 3. Obtener todas las habitaciones disponibles
        List<Room> availableRooms;
        if (roomType != null) {
            // Filtrar por tipo de habitación
            availableRooms = roomRepository.findAll().stream()
                    .filter(room -> room.getIsAvailable() && room.getRoomType() == roomType)
                    .collect(Collectors.toList());
        } else {
            // Sin filtro de tipo
            availableRooms = roomRepository.findAll().stream()
                    .filter(Room::getIsAvailable)
                    .collect(Collectors.toList());
        }

        // 4. Convertir a DTO y retornar
        // TODO: En futuro, validar que no tengan reservas en el rango de fechas
        return availableRooms.stream()
                .map(RoomResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
