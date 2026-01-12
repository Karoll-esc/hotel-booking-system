package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.exception.DuplicateRoomNumberException;
import com.sofka.hotel_booking_api.domain.exception.InvalidDateRangeException;
import com.sofka.hotel_booking_api.domain.exception.RoomNotFoundException;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    // ==================== Tests para getAllRooms() ====================

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay habitaciones")
    void shouldReturnEmptyListWhenNoRooms() {
        // Given
        when(roomRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<RoomResponse> rooms = roomService.getAllRooms();

        // Then
        assertThat(rooms).isEmpty();
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar todas las habitaciones registradas")
    void shouldReturnAllRooms() {
        // Given
        Room room1 = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        room1.setId(1L);
        Room room2 = new Room("302", RoomType.SUPERIOR, 2, new BigDecimal("150.00"));
        room2.setId(2L);
        Room room3 = new Room("303", RoomType.STANDARD, 1, new BigDecimal("100.00"));
        room3.setId(3L);

        when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2, room3));

        // When
        List<RoomResponse> rooms = roomService.getAllRooms();

        // Then
        assertThat(rooms).hasSize(3);
        assertThat(rooms.get(0).getRoomNumber()).isEqualTo("301");
        assertThat(rooms.get(1).getRoomNumber()).isEqualTo("302");
        assertThat(rooms.get(2).getRoomNumber()).isEqualTo("303");
        verify(roomRepository, times(1)).findAll();
    }

    // ==================== Tests para getRoomById() ====================

    @Test
    @DisplayName("Debe retornar habitación cuando existe")
    void shouldReturnRoomWhenExists() {
        // Given
        Room room = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        room.setId(1L);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        // When
        RoomResponse response = roomService.getRoomById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRoomNumber()).isEqualTo("301");
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando habitación no existe")
    void shouldThrowExceptionWhenRoomNotFound() {
        // Given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> roomService.getRoomById(999L))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessageContaining("No se encontró la habitación con ID: 999");

        verify(roomRepository, times(1)).findById(999L);
    }

    // ==================== Tests para updateRoom() ====================

    @Test
    @DisplayName("Debe actualizar habitación exitosamente")
    void shouldUpdateRoomSuccessfully() {
        // Given
        Room existingRoom = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        existingRoom.setId(1L);

        CreateRoomRequest updateRequest = new CreateRoomRequest(
                "301A",
                RoomType.DELUXE,
                5,
                new BigDecimal("300.00")
        );

        Room updatedRoom = new Room("301A", RoomType.DELUXE, 5, new BigDecimal("300.00"));
        updatedRoom.setId(1L);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.save(any(Room.class))).thenReturn(updatedRoom);

        // When
        RoomResponse response = roomService.updateRoom(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRoomNumber()).isEqualTo("301A");
        assertThat(response.getRoomType()).isEqualTo(RoomType.DELUXE);
        assertThat(response.getCapacity()).isEqualTo(5);
        assertThat(response.getPricePerNight()).isEqualByComparingTo(new BigDecimal("300.00"));

        verify(roomRepository, times(1)).findById(1L);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar habitación inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentRoom() {
        // Given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        CreateRoomRequest updateRequest = new CreateRoomRequest(
                "301A",
                RoomType.DELUXE,
                5,
                new BigDecimal("300.00")
        );

        // When/Then
        assertThatThrownBy(() -> roomService.updateRoom(999L, updateRequest))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessageContaining("No se encontró la habitación con ID: 999");

        verify(roomRepository, times(1)).findById(999L);
        verify(roomRepository, never()).save(any(Room.class));
    }

    // ==================== Tests para deleteRoom() ====================

    @Test
    @DisplayName("Debe eliminar habitación exitosamente")
    void shouldDeleteRoomSuccessfully() {
        // Given
        when(roomRepository.existsById(1L)).thenReturn(true);
        doNothing().when(roomRepository).deleteById(1L);

        // When
        roomService.deleteRoom(1L);

        // Then
        verify(roomRepository, times(1)).existsById(1L);
        verify(roomRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar habitación inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentRoom() {
        // Given
        when(roomRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> roomService.deleteRoom(999L))
                .isInstanceOf(RoomNotFoundException.class)
                .hasMessageContaining("No se encontró la habitación con ID: 999");

        verify(roomRepository, times(1)).existsById(999L);
        verify(roomRepository, never()).deleteById(anyLong());
    }

    // ==================== Tests para getAvailableRooms() ====================

    @Test
    @DisplayName("Debe retornar habitaciones disponibles sin filtro de tipo")
    void shouldReturnAvailableRoomsWithoutTypeFilter() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Room room1 = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        room1.setId(1L);
        Room room2 = new Room("302", RoomType.SUPERIOR, 2, new BigDecimal("150.00"));
        room2.setId(2L);
        room2.setIsAvailable(false);  // No disponible
        Room room3 = new Room("303", RoomType.STANDARD, 1, new BigDecimal("100.00"));
        room3.setId(3L);

        when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2, room3));

        // When
        List<RoomResponse> availableRooms = roomService.getAvailableRooms(checkIn, checkOut, null);

        // Then
        assertThat(availableRooms).hasSize(2);
        assertThat(availableRooms.get(0).getRoomNumber()).isEqualTo("301");
        assertThat(availableRooms.get(1).getRoomNumber()).isEqualTo("303");
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe retornar habitaciones disponibles filtradas por tipo")
    void shouldReturnAvailableRoomsFilteredByType() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Room room1 = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        room1.setId(1L);
        Room room2 = new Room("302", RoomType.SUPERIOR, 2, new BigDecimal("150.00"));
        room2.setId(2L);
        Room room3 = new Room("303", RoomType.SUITE, 4, new BigDecimal("280.00"));
        room3.setId(3L);

        when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2, room3));

        // When
        List<RoomResponse> availableRooms = roomService.getAvailableRooms(checkIn, checkOut, RoomType.SUITE);

        // Then
        assertThat(availableRooms).hasSize(2);
        assertThat(availableRooms.get(0).getRoomType()).isEqualTo(RoomType.SUITE);
        assertThat(availableRooms.get(1).getRoomType()).isEqualTo(RoomType.SUITE);
        verify(roomRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando fecha de entrada es en el pasado")
    void shouldThrowExceptionWhenCheckInIsInPast() {
        // Given
        LocalDate checkIn = LocalDate.now().minusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        // When/Then
        assertThatThrownBy(() -> roomService.getAvailableRooms(checkIn, checkOut, null))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("La fecha de entrada no puede ser en el pasado");

        verify(roomRepository, never()).findAll();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando fecha de entrada es después de fecha de salida")
    void shouldThrowExceptionWhenCheckInIsAfterCheckOut() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        // When/Then
        assertThatThrownBy(() -> roomService.getAvailableRooms(checkIn, checkOut, null))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("La fecha de entrada debe ser anterior a la fecha de salida");

        verify(roomRepository, never()).findAll();
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando fechas de entrada y salida son iguales")
    void shouldThrowExceptionWhenCheckInEqualsCheckOut() {
        // Given
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        // When/Then
        assertThatThrownBy(() -> roomService.getAvailableRooms(checkIn, checkOut, null))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("La fecha de entrada debe ser anterior a la fecha de salida");

        verify(roomRepository, never()).findAll();
    }
}
