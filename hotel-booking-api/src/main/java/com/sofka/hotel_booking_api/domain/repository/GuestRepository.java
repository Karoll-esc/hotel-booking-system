package com.sofka.hotel_booking_api.domain.repository;

import com.sofka.hotel_booking_api.domain.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para gestionar los huéspedes del hotel.
 */
@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    /**
     * Busca un huésped por su número de documento.
     *
     * @param documentNumber el número de documento
     * @return el huésped si existe, empty si no
     */
    Optional<Guest> findByDocumentNumber(String documentNumber);

    /**
     * Verifica si existe un huésped con el número de documento dado.
     *
     * @param documentNumber el número de documento
     * @return true si existe, false si no
     */
    boolean existsByDocumentNumber(String documentNumber);
}
