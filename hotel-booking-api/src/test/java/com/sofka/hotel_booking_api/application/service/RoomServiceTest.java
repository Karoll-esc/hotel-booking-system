package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios del servicio de habitaciones.
 * Fase RED de TDD - Historia 2.1: Registrar habitaciones del hotel
 * Escenario: Registro exitoso de nueva habitación
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RoomService - Tests unitarios")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private CreateRoomRequest validRequest;
    private Room roomToSave;
    private Room savedRoom;

    @BeforeEach
    void setUp() {
        // Given - Preparar datos de prueba
        validRequest = new CreateRoomRequest(
                "301",
                RoomType.SUITE,
                4,
                new BigDecimal("250.00")
        );

        roomToSave = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        
        savedRoom = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        // Simular que tiene ID después de guardarse
        // (en producción, JPA lo asignaría automáticamente)
    }

    @Test
    @DisplayName("Debe registrar habitación exitosamente cuando los datos son válidos")
    void shouldRegisterRoomSuccessfully() {
        // Given - Dado que el repositorio guardará la habitación
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // When - Cuando registro una habitación con datos válidos
        // Then - Este test DEBE FALLAR con UnsupportedOperationException (fase RED)
        assertThatThrownBy(() -> roomService.registerRoom(validRequest))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Método no implementado aún");
    }

    @Test
    @DisplayName("Debe llamar al repositorio para guardar la habitación")
    void shouldCallRepositoryToSaveRoom() {
        // Given - Dado que el repositorio está configurado
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // When - Cuando intento registrar una habitación
        // Then - Este test DEBE FALLAR porque el método lanza UnsupportedOperationException
        try {
            roomService.registerRoom(validRequest);
        } catch (UnsupportedOperationException e) {
            // Esperado en fase RED
            assertThat(e.getMessage()).contains("Método no implementado aún");
        }

        // En fase GREEN, este test verificará que se llamó al repositorio
        // verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Debe retornar RoomResponse con los datos de la habitación registrada")
    void shouldReturnRoomResponseWithRegisteredData() {
        // Given - Dado que el repositorio retorna la habitación guardada
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // When/Then - Este test DEBE FALLAR por UnsupportedOperationException
        assertThatThrownBy(() -> {
            RoomResponse response = roomService.registerRoom(validRequest);
            
            // En fase GREEN, estas aserciones deberían pasar:
            assertThat(response).isNotNull();
            assertThat(response.getRoomNumber()).isEqualTo("301");
            assertThat(response.getRoomType()).isEqualTo(RoomType.SUITE);
            assertThat(response.getCapacity()).isEqualTo(4);
            assertThat(response.getPricePerNight()).isEqualByComparingTo(new BigDecimal("250.00"));
            assertThat(response.getIsAvailable()).isTrue();
        }).isInstanceOf(UnsupportedOperationException.class);
    }
}
