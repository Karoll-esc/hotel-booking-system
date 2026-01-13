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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

        // When/Then - Cuando intento crear la reserva debe lanzar excepción
        // La validación de duración ocurre antes de buscar la habitación
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

    // ============================================
    // RED PHASE - Historia 5.1: Buscar reservas
    // ============================================

    @Test
    @DisplayName("Debe buscar reserva por número de reserva exacto")
    void shouldSearchByReservationNumberExactMatch() {
        // Given - Dado que existe una reserva con número "RES-2026-001234"
        Reservation reservation = new Reservation(
                "RES-2026-001234",
                guest,
                availableRoom,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2,
                new BigDecimal("1250.00")
        );
        reservation.setId(1L);

        when(reservationRepository.findByReservationNumber("RES-2026-001234"))
                .thenReturn(Optional.of(reservation));

        // When - Cuando busco por número de reserva
        List<ReservationResponse> results = reservationService.searchReservations("RES-2026-001234", null);

        // Then - Entonces encuentro la reserva correspondiente
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("RES-2026-001234", results.get(0).reservationNumber());
        verify(reservationRepository).findByReservationNumber("RES-2026-001234");
    }

    @Test
    @DisplayName("Debe buscar reservas por nombre de huésped (partial match)")
    void shouldSearchByGuestNamePartialMatch() {
        // Given - Dado que existen múltiples reservas para "Juan Pérez"
        Reservation reservation1 = new Reservation(
                "RES-2026-001",
                guest,
                availableRoom,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2,
                new BigDecimal("1250.00")
        );
        reservation1.setId(1L);

        Reservation reservation2 = new Reservation(
                "RES-2026-002",
                guest,
                availableRoom,
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(20),
                2,
                new BigDecimal("1250.00")
        );
        reservation2.setId(2L);

        when(reservationRepository.findByGuestNameContainingIgnoreCase("Juan"))
                .thenReturn(Arrays.asList(reservation1, reservation2));

        // When - Cuando busco por nombre "Juan"
        List<ReservationResponse> results = reservationService.searchReservations(null, "Juan");

        // Then - Entonces veo todas las reservas asociadas
        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("Juan", results.get(0).guest().firstName());
        assertEquals("Juan", results.get(1).guest().firstName());
        verify(reservationRepository).findByGuestNameContainingIgnoreCase("Juan");
    }

    @Test
    @DisplayName("Debe buscar por apellido con case-insensitive")
    void shouldSearchByLastNameCaseInsensitive() {
        // Given - Dado que busco por apellido "pérez" (minúscula)
        Reservation reservation = new Reservation(
                "RES-2026-003",
                guest,
                availableRoom,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2,
                new BigDecimal("1250.00")
        );
        reservation.setId(1L);

        when(reservationRepository.findByGuestNameContainingIgnoreCase("pérez"))
                .thenReturn(Collections.singletonList(reservation));

        // When - Cuando busco por "pérez"
        List<ReservationResponse> results = reservationService.searchReservations(null, "pérez");

        // Then - Entonces encuentro reservas con "Pérez"
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Pérez", results.get(0).guest().lastName());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no hay resultados")
    void shouldReturnEmptyListWhenNoResults() {
        // Given - Dado que busco por un criterio que no existe
        when(reservationRepository.findByReservationNumber("RES-9999-999999"))
                .thenReturn(Optional.empty());

        // When - Cuando busco
        List<ReservationResponse> results = reservationService.searchReservations("RES-9999-999999", null);

        // Then - Entonces recibo una lista vacía
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando búsqueda por nombre no encuentra resultados")
    void shouldReturnEmptyListWhenGuestNameNotFound() {
        // Given - Dado que no existen reservas para el nombre buscado
        when(reservationRepository.findByGuestNameContainingIgnoreCase("NoExiste"))
                .thenReturn(Collections.emptyList());

        // When - Cuando busco
        List<ReservationResponse> results = reservationService.searchReservations(null, "NoExiste");

        // Then - Entonces recibo una lista vacía
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando no se proporciona ningún criterio de búsqueda")
    void shouldThrowExceptionWhenNoCriteriaProvided() {
        // Given - No hay criterios de búsqueda

        // When & Then - Entonces lanza IllegalArgumentException
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> reservationService.searchReservations(null, null)
        );

        assertEquals("Debe proporcionar al menos un criterio de búsqueda: número de reserva o nombre del huésped",
                exception.getMessage());
        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("Debe buscar primero por número de reserva cuando ambos criterios están presentes")
    void shouldPrioritizeReservationNumberWhenBothCriteriaProvided() {
        // Given - Dado que proporciono ambos criterios
        Reservation reservation = new Reservation(
                "RES-2026-001234",
                guest,
                availableRoom,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2,
                new BigDecimal("1250.00")
        );
        reservation.setId(1L);

        when(reservationRepository.findByReservationNumber("RES-2026-001234"))
                .thenReturn(Optional.of(reservation));

        // When - Cuando busco con ambos criterios
        List<ReservationResponse> results = reservationService.searchReservations("RES-2026-001234", "Juan");

        // Then - Entonces solo busca por número de reserva (prioridad)
        assertNotNull(results);
        assertEquals(1, results.size());
        verify(reservationRepository).findByReservationNumber("RES-2026-001234");
        verify(reservationRepository, never()).findByGuestNameContainingIgnoreCase(any());
    }

    @Test
    @DisplayName("Debe ordenar resultados por fecha de check-in cuando busca por nombre")
    void shouldOrderResultsByCheckInDateWhenSearchingByName() {
        // Given - Dado que existen reservas con diferentes fechas de check-in
        Reservation futureReservation = new Reservation(
                "RES-2026-002",
                guest,
                availableRoom,
                LocalDate.now().plusDays(15), // Más adelante
                LocalDate.now().plusDays(20),
                2,
                new BigDecimal("1250.00")
        );
        futureReservation.setId(2L);

        Reservation soonReservation = new Reservation(
                "RES-2026-001",
                guest,
                availableRoom,
                LocalDate.now().plusDays(5), // Más pronto
                LocalDate.now().plusDays(10),
                2,
                new BigDecimal("1250.00")
        );
        soonReservation.setId(1L);

        // Repositorio retorna ordenado por checkInDate ASC
        when(reservationRepository.findByGuestNameContainingIgnoreCase("Juan"))
                .thenReturn(Arrays.asList(soonReservation, futureReservation));

        // When - Cuando busco por nombre
        List<ReservationResponse> results = reservationService.searchReservations(null, "Juan");

        // Then - Entonces están ordenadas por fecha de llegada (la más pronta primero)
        assertNotNull(results);
        assertEquals(2, results.size());
        assertTrue(results.get(0).checkInDate().isBefore(results.get(1).checkInDate()));
    }
}
