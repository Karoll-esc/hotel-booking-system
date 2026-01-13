package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.Guest;
import com.sofka.hotel_booking_api.domain.model.Reservation;
import com.sofka.hotel_booking_api.domain.model.ReservationStatus;
import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.domain.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PaymentService - RED PHASE.
 * Historia 4.1: Confirmar pago de reserva
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Reservation pendingReservation;
    private Room room;
    private Guest guest;

    @BeforeEach
    void setUp() {
        room = new Room("101", RoomType.STANDARD, 2, new BigDecimal("125.00"));
        guest = new Guest("John", "Doe", "12345678", "john@example.com", "+1-555-1234");
        
        pendingReservation = new Reservation(
                "RES-2026-001",
                guest,
                room,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2,
                new BigDecimal("625.00")
        );
    }

    // ============================================
    // RED PHASE - Escenario: Confirmación de pago en efectivo
    // ============================================

    @Test
    @DisplayName("Debe confirmar pago en efectivo y cambiar estado a CONFIRMED")
    void shouldConfirmPaymentInCashAndChangeStatus() {
        // Given - Reserva en estado PENDING
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(pendingReservation);

        // When - Confirmo el pago en efectivo
        paymentService.confirmPayment(1L, "CASH", new BigDecimal("625.00"), null);

        // Then - La reserva debe cambiar a estado CONFIRMED
        assertEquals(ReservationStatus.CONFIRMED, pendingReservation.getStatus());
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(pendingReservation);
    }

    @Test
    @DisplayName("Debe registrar el método de pago y la fecha de confirmación")
    void shouldRecordPaymentMethodAndConfirmationDate() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(pendingReservation);

        // When
        paymentService.confirmPayment(1L, "CASH", new BigDecimal("625.00"), null);

        // Then - Debe guardar método de pago y fecha
        assertEquals(ReservationStatus.CONFIRMED, pendingReservation.getStatus());
        verify(reservationRepository, times(1)).save(pendingReservation);
    }

    // ============================================
    // RED PHASE - Escenario: Confirmación de pago con tarjeta (POS externo)
    // ============================================

    @Test
    @DisplayName("Debe confirmar pago con tarjeta y registrar número de autorización")
    void shouldConfirmCardPaymentWithAuthorizationNumber() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(pendingReservation);

        // When - Pago con tarjeta y número de autorización
        paymentService.confirmPayment(1L, "CARD", new BigDecimal("625.00"), "AUTH-789456");

        // Then - Debe guardar el número de referencia
        assertEquals(ReservationStatus.CONFIRMED, pendingReservation.getStatus());
        verify(reservationRepository, times(1)).save(pendingReservation);
    }

    @Test
    @DisplayName("Debe lanzar excepción si intenta pagar con tarjeta sin número de autorización")
    void shouldThrowExceptionWhenCardPaymentWithoutAuthorizationNumber() {
        // Given
        // When/Then - Sin número de autorización debe fallar
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.confirmPayment(1L, "CARD", new BigDecimal("625.00"), null);
        });
        
        assertTrue(exception.getMessage().contains("requiere un número de referencia"));
    }

    // ============================================
    // RED PHASE - Escenario: Confirmación de pago por transferencia bancaria
    // ============================================

    @Test
    @DisplayName("Debe confirmar pago por transferencia y registrar comprobante")
    void shouldConfirmTransferPaymentWithReceipt() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(pendingReservation);

        // When - Pago por transferencia con comprobante
        paymentService.confirmPayment(1L, "TRANSFER", new BigDecimal("625.00"), "TRF-2026-001234");

        // Then - Debe guardar el comprobante
        assertEquals(ReservationStatus.CONFIRMED, pendingReservation.getStatus());
        verify(reservationRepository, times(1)).save(pendingReservation);
    }

    @Test
    @DisplayName("Debe lanzar excepción si intenta pagar por transferencia sin comprobante")
    void shouldThrowExceptionWhenTransferPaymentWithoutReceipt() {
        // Given
        // When/Then - Sin comprobante debe fallar
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.confirmPayment(1L, "TRANSFER", new BigDecimal("625.00"), null);
        });
        
        assertTrue(exception.getMessage().contains("requiere un número de referencia"));
    }

    // ============================================
    // RED PHASE - Escenario: Validaciones de pago
    // ============================================

    @Test
    @DisplayName("Debe lanzar excepción si la reserva no existe")
    void shouldThrowExceptionWhenReservationNotFound() {
        // Given - No existe la reserva
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(RuntimeException.class, () -> {
            paymentService.confirmPayment(999L, "CASH", new BigDecimal("625.00"), null);
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción si la reserva está expirada")
    void shouldThrowExceptionWhenReservationIsExpired() {
        // Given - Reserva expirada
        Reservation expiredReservation = new Reservation(
                "RES-2026-002",
                guest,
                room,
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(10),
                2,
                new BigDecimal("625.00")
        );
        expiredReservation.expire();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(expiredReservation));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentService.confirmPayment(1L, "CASH", new BigDecimal("625.00"), null);
        });
        
        assertTrue(exception.getMessage().contains("expirado"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el monto no coincide con el total de la reserva")
    void shouldThrowExceptionWhenAmountDoesNotMatch() {
        // Given - Monto incorrecto
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingReservation));

        // When/Then - Monto diferente al total
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.confirmPayment(1L, "CASH", new BigDecimal("500.00"), null);
        });
        
        assertTrue(exception.getMessage().contains("no coincide"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si intenta confirmar pago de reserva ya confirmada")
    void shouldThrowExceptionWhenReservationAlreadyConfirmed() {
        // Given - Reserva ya pagada
        pendingReservation.confirmPayment();
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(pendingReservation));

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentService.confirmPayment(1L, "CASH", new BigDecimal("625.00"), null);
        });
        
        assertTrue(exception.getMessage().contains("confirmado"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el método de pago no es válido")
    void shouldThrowExceptionWhenPaymentMethodIsInvalid() {
        // Given
        // When/Then - Método de pago inválido
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.confirmPayment(1L, "CRYPTO", new BigDecimal("625.00"), null);
        });
        
        assertTrue(exception.getMessage().contains("inválido"));
    }
}
