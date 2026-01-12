package com.sofka.hotel_booking_api.domain.repository;

import com.sofka.hotel_booking_api.domain.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar las habitaciones del hotel.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Busca una habitación por su número.
     *
     * @param roomNumber el número de la habitación
     * @return la habitación si existe, empty si no
     */
    Optional<Room> findByRoomNumber(String roomNumber);

    /**
     * Verifica si existe una habitación con el número dado.
     *
     * @param roomNumber el número de la habitación
     * @return true si existe, false si no
     */
    boolean existsByRoomNumber(String roomNumber);
}
