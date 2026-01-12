package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.exception.InvalidDateRangeException;
import com.sofka.hotel_booking_api.domain.exception.RoomNotFoundException;
import com.sofka.hotel_booking_api.domain.model.*;
import com.sofka.hotel_booking_api.domain.repository.GuestRepository;
import com.sofka.hotel_booking_api.domain.repository.ReservationRepository;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateGuestRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateReservationRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.ReservationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests del servicio de reservas.
 * Fase RED de TDD - Historia 3.1: Crear reserva para un huésped
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService - Tests unitarios")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private GuestService guestService;

    @InjectMocks
    private ReservationService reservationService;

    private Room availableRoom;
    private Guest guest;
    private CreateGuestRequest guestRequest;
    private CreateReservationRequest validReservationRequest;

    @BeforeEach
    void setUp() {
        // Given - Preparar datos de prueba
        availableRoom = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        availableRoom.setId(1L);

        guest = new Guest("Juan", "Pérez", "12345678", "juan@email.com", "+57 300 1234567");
        guest.setId(1L);

        guestRequest = new CreateGuestRequest(
                "Juan",
                "Pérez",
                "12345678",
                "juan@email.com",
                "+57 300 1234567"
        );

        validReservationRequest = new CreateReservationRequest(
                guestRequest,
                1L,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2
        );
    }

    // ============================================
    // RED PHASE - Escenario: Creación exitosa de reserva
    // ============================================

    @Test
    @DisplayName("Debe crear reserva exitosamente cuando la habitación está disponible")
    void shouldCreateReservationSuccessfully() {
        // Given - Dado que la habitación está disponible
        when(roomRepository.findById(1L)).thenReturn(Optional.of(availableRoom));
        when(guestService.registerOrUpdateGuest(any(CreateGuestRequest.class))).thenReturn(guest);
        when(reservationRepository.findOverlappingReservations(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        
        Reservation savedReservation = new Reservation(
                "RES-2026-001",
                guest,
                availableRoom,
                validReservationRequest.checkInDate(),
                validReservationRequest.checkOutDate(),
                validReservationRequest.numberOfGuests(),
                new BigDecimal("1250.00")
        );
        savedReservation.setId(1L);
        
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // When - Cuando creo la reserva
        ReservationResponse response = reservationService.createReservation(validReservationRequest);

        // Then - Entonces la reserva se crea exitosamente
        assertNotNull(response);
        assertNotNull(response.reservationNumber());
        assertEquals(ReservationStatus.PENDING, response.status());
        assertEquals(guest.getFullName(), response.guest().fullName());
        assertEquals(availableRoom.getRoomNumber(), response.room().getRoomNumber());
        assertEquals(5L, response.numberOfNights());
        assertEquals(new BigDecimal("1250.00"), response.totalAmount());
        
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    // ============================================
    // RED PHASE - Escenario: Intento de reserva sobre fechas no disponibles
    // ============================================

    @Test
    @DisplayName("Debe lanzar excepción cuando la habitación no está disponible en las fechas solicitadas")
    void shouldThrowExceptionWhenRoomNotAvailableForDates() {
        // Given - Dado que la habitación ya tiene una reserva en esas fechas
        when(roomRepository.findById(1L)).thenReturn(Optional.of(availableRoom));
        
        Reservation existingReservation = new Reservation(
                "RES-2026-002",
                guest,
                availableRoom,
                validReservationRequest.checkInDate(),
                validReservationRequest.checkOutDate(),
                2,
                new BigDecimal("1250.00")
        );
        
        when(reservationRepository.findOverlappingReservations(any(), any(), any()))
                .thenReturn(Collections.singletonList(existingReservation));

        // When/Then - Cuando intento crear la reserva debe lanzar excepción
        assertThrows(IllegalStateException.class, () -> {
            reservationService.createReservation(validReservationRequest);
        });
        
        verify(reservationRepository, never()).save(any());
    }

    // ============================================
    // RED PHASE - Escenario: Intento de reserva con capacidad excedida
    // ============================================

    @Test
    @DisplayName("Debe lanzar excepción cuando el número de huéspedes excede la capacidad de la habitación")
    void shouldThrowExceptionWhenGuestsExceedRoomCapacity() {
        // Given - Dado que intento reservar para más personas de la capacidad
        CreateReservationRequest requestExceedingCapacity = new CreateReservationRequest(
                guestRequest,
                1L,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                6 // Excede la capacidad de 4 personas
        );
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(availableRoom));

        // When/Then - Cuando intento crear la reserva debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(requestExceedingCapacity);
        });
        
        verify(reservationRepository, never()).save(any());
    }

    // ============================================
    // RED PHASE - Escenario: Intento de reserva con estadía mayor a 30 noches
    // ============================================

    @Test
    @DisplayName("Debe lanzar excepción cuando la estadía excede 30 noches")
    void shouldThrowExceptionWhenStayExceeds30Nights() {
        // Given - Dado que intento reservar por 45 noches
        CreateReservationRequest longStayRequest = new CreateReservationRequest(
                guestRequest,
                1L,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(50), // 45 noches
                2
        );
        
        when(roomRepository.findById(1L)).thenReturn(Optional.of(availableRoom));

        // When/Then - Cuando intento crear la reserva debe lanzar excepción
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(longStayRequest);
        });
        
        verify(reservationRepository, never()).save(any());
    }

    // ============================================
    // RED PHASE - Escenario: Intento de reserva con fecha de entrada en el pasado
    // ============================================

    @Test
    @DisplayName("Debe lanzar excepción cuando la fecha de entrada es en el pasado")
    void shouldThrowExceptionWhenCheckInDateIsInPast() {
        // Given - Dado que intento reservar con fecha pasada
        CreateReservationRequest pastDateRequest = new CreateReservationRequest(
                guestRequest,
                1L,
                LocalDate.now().minusDays(5), // Fecha pasada
                LocalDate.now().plusDays(5),
                2
        );

        // When/Then - Cuando intento crear la reserva debe lanzar excepción
        assertThrows(InvalidDateRangeException.class, () -> {
            reservationService.createReservation(pastDateRequest);
        });
        
        verify(reservationRepository, never()).save(any());
    }

    // ============================================
    // RED PHASE - Validaciones adicionales
    // ============================================

    @Test
    @DisplayName("Debe lanzar excepción cuando la fecha de salida es anterior a la fecha de entrada")
    void shouldThrowExceptionWhenCheckOutBeforeCheckIn() {
        // Given - Dado que intento reservar con fechas invertidas
        CreateReservationRequest invalidDatesRequest = new CreateReservationRequest(
                guestRequest,
                1L,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(5), // Salida antes de entrada
                2
        );

        // When/Then - Cuando intento crear la reserva debe lanzar excepción
        assertThrows(InvalidDateRangeException.class, () -> {
            reservationService.createReservation(invalidDatesRequest);
        });
        
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la habitación no existe")
    void shouldThrowExceptionWhenRoomDoesNotExist() {
        // Given - Dado que la habitación no existe
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        
        CreateReservationRequest requestWithInvalidRoom = new CreateReservationRequest(
                guestRequest,
                999L,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2
        );

        // When/Then - Cuando intento crear la reserva debe lanzar excepción
        assertThrows(RoomNotFoundException.class, () -> {
            reservationService.createReservation(requestWithInvalidRoom);
        });
        
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe calcular correctamente el monto total de la reserva")
    void shouldCalculateTotalAmountCorrectly() {
        // Given - Dado que creo una reserva válida
        when(roomRepository.findById(1L)).thenReturn(Optional.of(availableRoom));
        when(guestService.registerOrUpdateGuest(any(CreateGuestRequest.class))).thenReturn(guest);
        when(reservationRepository.findOverlappingReservations(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        
        Reservation savedReservation = new Reservation(
                "RES-2026-001",
                guest,
                availableRoom,
                validReservationRequest.checkInDate(),
                validReservationRequest.checkOutDate(),
                validReservationRequest.numberOfGuests(),
                new BigDecimal("1250.00") // 5 noches * 250 USD/noche
        );
        savedReservation.setId(1L);
        
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        // When - Cuando creo la reserva
        ReservationResponse response = reservationService.createReservation(validReservationRequest);

        // Then - Entonces el monto total debe ser correcto
        BigDecimal expectedTotal = availableRoom.getPricePerNight()
                .multiply(BigDecimal.valueOf(5)); // 5 noches
        assertEquals(expectedTotal, response.totalAmount());
    }
}
