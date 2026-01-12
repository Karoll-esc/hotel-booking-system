package com.sofka.hotel_booking_api.domain.repository;

import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del repositorio de habitaciones.
 * Fase GREEN de TDD - Historia 2.1: Registrar habitaciones del hotel
 * Usa H2 in-memory database para tests aislados y rápidos
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RoomRepository - Tests de persistencia con H2")
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @Test
    @DisplayName("Debe guardar una habitación y recuperarla por número")
    void shouldSaveAndFindRoomByNumber() {
        // Given - Dado que tengo una habitación con datos válidos
        Room room = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));

        // When - Cuando la guardo en el repositorio
        Room savedRoom = roomRepository.save(room);

        // Then - Entonces debe estar persistida y puedo recuperarla por número
        assertThat(savedRoom.getId()).isNotNull();
        assertThat(savedRoom.getRoomNumber()).isEqualTo("301");
        assertThat(savedRoom.getRoomType()).isEqualTo(RoomType.SUITE);
        assertThat(savedRoom.getCapacity()).isEqualTo(4);
        assertThat(savedRoom.getPricePerNight()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(savedRoom.getIsAvailable()).isTrue();

        // Verificar que puedo encontrarla por número
        Optional<Room> foundRoom = roomRepository.findByRoomNumber("301");
        assertThat(foundRoom).isPresent();
        assertThat(foundRoom.get().getRoomNumber()).isEqualTo("301");
    }

    @Test
    @DisplayName("Debe verificar si existe una habitación por número")
    void shouldCheckIfRoomExistsByNumber() {
        // Given - Dado que tengo una habitación guardada
        Room room = new Room("302", RoomType.STANDARD, 2, new BigDecimal("100.00"));
        roomRepository.save(room);

        // When - Cuando verifico si existe por número
        boolean exists = roomRepository.existsByRoomNumber("302");
        boolean notExists = roomRepository.existsByRoomNumber("999");

        // Then - Entonces debe retornar true para la que existe y false para la que no
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Debe retornar empty cuando busco habitación que no existe")
    void shouldReturnEmptyWhenRoomNotFound() {
        // When - Cuando busco una habitación que no existe
        Optional<Room> foundRoom = roomRepository.findByRoomNumber("999");

        // Then - Entonces debe retornar empty
        assertThat(foundRoom).isEmpty();
    }
}
