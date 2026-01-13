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
        // When - Confirmo el pago en efectivo
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "CASH", new BigDecimal("625.00"), null);
        });

        // Then - La reserva debe cambiar a estado CONFIRMED
        // verify will be added in GREEN phase
    }

    @Test
    @DisplayName("Debe registrar el método de pago y la fecha de confirmación")
    void shouldRecordPaymentMethodAndConfirmationDate() {
        // Given
        // When
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "CASH", new BigDecimal("625.00"), null);
        });

        // Then - Debe guardar método de pago y fecha
        // verify will be added in GREEN phase
    }

    // ============================================
    // RED PHASE - Escenario: Confirmación de pago con tarjeta (POS externo)
    // ============================================

    @Test
    @DisplayName("Debe confirmar pago con tarjeta y registrar número de autorización")
    void shouldConfirmCardPaymentWithAuthorizationNumber() {
        // Given
        // When - Pago con tarjeta y número de autorización
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "CARD", new BigDecimal("625.00"), "AUTH-789456");
        });

        // Then - Debe guardar el número de referencia
        // verify will be added in GREEN phase
    }

    @Test
    @DisplayName("Debe lanzar excepción si intenta pagar con tarjeta sin número de autorización")
    void shouldThrowExceptionWhenCardPaymentWithoutAuthorizationNumber() {
        // Given
        // When/Then - Sin número de autorización debe fallar
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "CARD", new BigDecimal("625.00"), null);
        });
    }

    // ============================================
    // RED PHASE - Escenario: Confirmación de pago por transferencia bancaria
    // ============================================

    @Test
    @DisplayName("Debe confirmar pago por transferencia y registrar comprobante")
    void shouldConfirmTransferPaymentWithReceipt() {
        // Given
        // When - Pago por transferencia con comprobante
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "TRANSFER", new BigDecimal("625.00"), "TRF-2026-001234");
        });

        // Then - Debe guardar el comprobante
        // verify will be added in GREEN phase
    }

    @Test
    @DisplayName("Debe lanzar excepción si intenta pagar por transferencia sin comprobante")
    void shouldThrowExceptionWhenTransferPaymentWithoutReceipt() {
        // Given
        // When/Then - Sin comprobante debe fallar
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "TRANSFER", new BigDecimal("625.00"), null);
        });
    }

    // ============================================
    // RED PHASE - Escenario: Validaciones de pago
    // ============================================

    @Test
    @DisplayName("Debe lanzar excepción si la reserva no existe")
    void shouldThrowExceptionWhenReservationNotFound() {
        // Given - No existe la reserva
        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(999L, "CASH", new BigDecimal("625.00"), null);
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción si la reserva está expirada")
    void shouldThrowExceptionWhenReservationIsExpired() {
        // Given - Reserva expirada
        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "CASH", new BigDecimal("625.00"), null);
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción si el monto no coincide con el total de la reserva")
    void shouldThrowExceptionWhenAmountDoesNotMatch() {
        // Given - Monto incorrecto
        // When/Then - Monto diferente al total
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "CASH", new BigDecimal("500.00"), null);
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción si intenta confirmar pago de reserva ya confirmada")
    void shouldThrowExceptionWhenReservationAlreadyConfirmed() {
        // Given - Reserva ya pagada
        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "CASH", new BigDecimal("625.00"), null);
        });
    }

    @Test
    @DisplayName("Debe lanzar excepción si el método de pago no es válido")
    void shouldThrowExceptionWhenPaymentMethodIsInvalid() {
        // Given
        // When/Then - Método de pago inválido
        assertThrows(UnsupportedOperationException.class, () -> {
            paymentService.confirmPayment(1L, "CRYPTO", new BigDecimal("625.00"), null);
        });
    }
}
