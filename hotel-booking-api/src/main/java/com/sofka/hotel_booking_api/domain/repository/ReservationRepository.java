package com.sofka.hotel_booking_api.domain.repository;

import com.sofka.hotel_booking_api.domain.model.Reservation;
import com.sofka.hotel_booking_api.domain.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar las reservas del hotel.
 */
@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Busca una reserva por su número de reserva.
     *
     * @param reservationNumber el número de reserva
     * @return la reserva si existe, empty si no
     */
    Optional<Reservation> findByReservationNumber(String reservationNumber);

    /**
     * Verifica si existe una reserva con el número dado.
     *
     * @param reservationNumber el número de reserva
     * @return true si existe, false si no
     */
    boolean existsByReservationNumber(String reservationNumber);

    /**
     * Busca reservas que se solapan con un rango de fechas para una habitación específica.
     * Excluye reservas canceladas y expiradas.
     *
     * @param room la habitación
     * @param checkInDate fecha de entrada
     * @param checkOutDate fecha de salida
     * @return lista de reservas que se solapan
     */
    @Query("SELECT r FROM Reservation r WHERE r.room = :room " +
           "AND r.status NOT IN ('CANCELLED', 'EXPIRED', 'COMPLETED') " +
           "AND ((r.checkInDate < :checkOutDate AND r.checkOutDate > :checkInDate))")
    List<Reservation> findOverlappingReservations(
            @Param("room") Room room,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    /**
     * Busca reservas por nombre del huésped (firstName o lastName).
     * Búsqueda parcial y case-insensitive.
     * Historia 5.1: Buscar reservas por nombre de huésped
     *
     * @param name el nombre a buscar (firstName o lastName)
     * @return lista de reservas ordenadas por fecha de check-in
     */
    @Query("SELECT r FROM Reservation r " +
           "WHERE LOWER(r.guest.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(r.guest.lastName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY r.checkInDate ASC")
    List<Reservation> findByGuestNameContainingIgnoreCase(@Param("name") String name);
}
