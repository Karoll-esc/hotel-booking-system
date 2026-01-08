package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.exception.DuplicateRoomNumberException;
import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Fase GREEN de TDD - Historia 2.1: Registrar habitaciones del hotel
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

        savedRoom = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        savedRoom.setId(1L); // Simular que JPA asignó un ID
    }

    @Test
    @DisplayName("Debe registrar habitación exitosamente cuando los datos son válidos")
    void shouldRegisterRoomSuccessfully() {
        // Given - Dado que el número de habitación no existe
        when(roomRepository.existsByRoomNumber("301")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // When - Cuando registro una habitación con datos válidos
        RoomResponse response = roomService.registerRoom(validRequest);

        // Then - La habitación debe quedar registrada correctamente
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRoomNumber()).isEqualTo("301");
        assertThat(response.getRoomType()).isEqualTo(RoomType.SUITE);
        assertThat(response.getCapacity()).isEqualTo(4);
        assertThat(response.getPricePerNight()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(response.getIsAvailable()).isTrue();

        // Verificar que se validó la duplicidad
        verify(roomRepository, times(1)).existsByRoomNumber("301");
        // Verificar que se guardó la habitación
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Debe llamar al repositorio para guardar la habitación")
    void shouldCallRepositoryToSaveRoom() {
        // Given
        when(roomRepository.existsByRoomNumber("301")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // When
        roomService.registerRoom(validRequest);

        // Then - Debe llamar exactamente una vez al método save
        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository, times(1)).save(roomCaptor.capture());

        // Verificar que los datos del Room son correctos
        Room capturedRoom = roomCaptor.getValue();
        assertThat(capturedRoom.getRoomNumber()).isEqualTo("301");
        assertThat(capturedRoom.getRoomType()).isEqualTo(RoomType.SUITE);
        assertThat(capturedRoom.getCapacity()).isEqualTo(4);
        assertThat(capturedRoom.getPricePerNight()).isEqualByComparingTo(new BigDecimal("250.00"));
        assertThat(capturedRoom.getIsAvailable()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar RoomResponse con los datos de la habitación registrada")
    void shouldReturnRoomResponseWithRegisteredData() {
        // Given
        when(roomRepository.existsByRoomNumber("301")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        // When
        RoomResponse response = roomService.registerRoom(validRequest);

        // Then - Verificar mapeo correcto de entidad a DTO
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getRoomNumber()).isEqualTo(validRequest.getRoomNumber());
        assertThat(response.getRoomType()).isEqualTo(validRequest.getRoomType());
        assertThat(response.getCapacity()).isEqualTo(validRequest.getCapacity());
        assertThat(response.getPricePerNight()).isEqualByComparingTo(validRequest.getPricePerNight());
        assertThat(response.getIsAvailable()).isTrue();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el número de habitación ya existe")
    void shouldThrowExceptionWhenRoomNumberAlreadyExists() {
        // Given - Dado que ya existe una habitación con número "301"
        when(roomRepository.existsByRoomNumber("301")).thenReturn(true);

        // When/Then - Debe lanzar DuplicateRoomNumberException
        assertThatThrownBy(() -> roomService.registerRoom(validRequest))
                .isInstanceOf(DuplicateRoomNumberException.class)
                .hasMessageContaining("Ya existe una habitación registrada con el número '301'");

        // Verificar que NO se intentó guardar
        verify(roomRepository, never()).save(any(Room.class));
    }
}
