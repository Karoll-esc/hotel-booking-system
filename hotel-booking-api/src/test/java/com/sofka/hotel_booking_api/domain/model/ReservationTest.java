package com.sofka.hotel_booking_api.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitarios para la entidad Reservation.
 * Enfocados en la lógica de negocio: transiciones de estado, validaciones y cálculos.
 */
@DisplayName("Reservation - Tests de lógica de negocio")
class ReservationTest {

    private Guest guest;
    private Room room;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        // Given - Preparar datos de prueba
        guest = new Guest("Juan", "Pérez", "12345678", "juan@email.com", "+573001234567");
        guest.setId(1L);

        room = new Room("301", RoomType.SUITE, 4, new BigDecimal("250.00"));
        room.setId(1L);

        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        reservation = new Reservation(
                "RES-001",
                guest,
                room,
                checkIn,
                checkOut,
                2,
                new BigDecimal("500.00")
        );
    }

    // ==================== Tests para getNumberOfNights() ====================

    @Test
    @DisplayName("Debe calcular correctamente el número de noches")
    void shouldCalculateNumberOfNights() {
        // Given
        LocalDate checkIn = LocalDate.of(2026, 1, 15);
        LocalDate checkOut = LocalDate.of(2026, 1, 18);
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);

        // When
        long nights = reservation.getNumberOfNights();

        // Then
        assertThat(nights).isEqualTo(3);
    }

    @Test
    @DisplayName("Debe calcular una noche para reserva de un día")
    void shouldCalculateOneNightForOneDayReservation() {
        // Given
        LocalDate checkIn = LocalDate.of(2026, 1, 15);
        LocalDate checkOut = LocalDate.of(2026, 1, 16);
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);

        // When
        long nights = reservation.getNumberOfNights();

        // Then
        assertThat(nights).isEqualTo(1);
    }

    // ==================== Tests para isActive() ====================

    @Test
    @DisplayName("Debe retornar true cuando la reserva está en estado ACTIVE")
    void shouldReturnTrueWhenReservationIsActive() {
        // Given
        reservation.setStatus(ReservationStatus.ACTIVE);

        // When
        boolean isActive = reservation.isActive();

        // Then
        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false cuando la reserva no está en estado ACTIVE")
    void shouldReturnFalseWhenReservationIsNotActive() {
        // Given
        reservation.setStatus(ReservationStatus.PENDING);

        // When
        boolean isActive = reservation.isActive();

        // Then
        assertThat(isActive).isFalse();
    }

    // ==================== Tests para isCancellable() ====================

    @Test
    @DisplayName("Debe retornar true cuando la reserva está en estado PENDING")
    void shouldBeCancellableWhenPending() {
        // Given
        reservation.setStatus(ReservationStatus.PENDING);

        // When
        boolean isCancellable = reservation.isCancellable();

        // Then
        assertThat(isCancellable).isTrue();
    }

    @Test
    @DisplayName("Debe retornar true cuando la reserva está en estado CONFIRMED")
    void shouldBeCancellableWhenConfirmed() {
        // Given
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // When
        boolean isCancellable = reservation.isCancellable();

        // Then
        assertThat(isCancellable).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false cuando la reserva está en estado ACTIVE")
    void shouldNotBeCancellableWhenActive() {
        // Given
        reservation.setStatus(ReservationStatus.ACTIVE);

        // When
        boolean isCancellable = reservation.isCancellable();

        // Then
        assertThat(isCancellable).isFalse();
    }

    @Test
    @DisplayName("Debe retornar false cuando la reserva está en estado COMPLETED")
    void shouldNotBeCancellableWhenCompleted() {
        // Given
        reservation.setStatus(ReservationStatus.COMPLETED);

        // When
        boolean isCancellable = reservation.isCancellable();

        // Then
        assertThat(isCancellable).isFalse();
    }

    // ==================== Tests para confirmPayment() ====================

    @Test
    @DisplayName("Debe confirmar pago exitosamente cuando está en estado PENDING")
    void shouldConfirmPaymentWhenPending() {
        // Given
        reservation.setStatus(ReservationStatus.PENDING);

        // When
        reservation.confirmPayment();

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Debe lanzar excepción al confirmar pago en estado CONFIRMED")
    void shouldThrowExceptionWhenConfirmingPaymentInConfirmedState() {
        // Given
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // When/Then
        assertThatThrownBy(() -> reservation.confirmPayment())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden confirmar reservas en estado PENDING");
    }

    @Test
    @DisplayName("Debe lanzar excepción al confirmar pago en estado CANCELLED")
    void shouldThrowExceptionWhenConfirmingPaymentInCancelledState() {
        // Given
        reservation.setStatus(ReservationStatus.CANCELLED);

        // When/Then
        assertThatThrownBy(() -> reservation.confirmPayment())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden confirmar reservas en estado PENDING");
    }

    // ==================== Tests para checkIn() ====================

    @Test
    @DisplayName("Debe realizar check-in exitosamente cuando está CONFIRMED")
    void shouldCheckInWhenConfirmed() {
        // Given
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // When
        reservation.checkIn();

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(reservation.getCheckInTime()).isNotNull();
        assertThat(reservation.getCheckInTime()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Debe lanzar excepción al hacer check-in en estado PENDING")
    void shouldThrowExceptionWhenCheckingInFromPending() {
        // Given
        reservation.setStatus(ReservationStatus.PENDING);

        // When/Then
        assertThatThrownBy(() -> reservation.checkIn())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede hacer check-in en reservas CONFIRMADAS");
    }

    @Test
    @DisplayName("Debe lanzar excepción al hacer check-in en estado ACTIVE")
    void shouldThrowExceptionWhenCheckingInFromActive() {
        // Given
        reservation.setStatus(ReservationStatus.ACTIVE);

        // When/Then
        assertThatThrownBy(() -> reservation.checkIn())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede hacer check-in en reservas CONFIRMADAS");
    }

    // ==================== Tests para checkOut() ====================

    @Test
    @DisplayName("Debe realizar check-out exitosamente cuando está ACTIVE")
    void shouldCheckOutWhenActive() {
        // Given
        reservation.setStatus(ReservationStatus.ACTIVE);

        // When
        reservation.checkOut();

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        assertThat(reservation.getCheckOutTime()).isNotNull();
        assertThat(reservation.getCheckOutTime()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Debe lanzar excepción al hacer check-out en estado CONFIRMED")
    void shouldThrowExceptionWhenCheckingOutFromConfirmed() {
        // Given
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // When/Then
        assertThatThrownBy(() -> reservation.checkOut())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede hacer check-out en reservas ACTIVAS");
    }

    @Test
    @DisplayName("Debe lanzar excepción al hacer check-out en estado COMPLETED")
    void shouldThrowExceptionWhenCheckingOutFromCompleted() {
        // Given
        reservation.setStatus(ReservationStatus.COMPLETED);

        // When/Then
        assertThatThrownBy(() -> reservation.checkOut())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se puede hacer check-out en reservas ACTIVAS");
    }

    // ==================== Tests para cancel() ====================

    @Test
    @DisplayName("Debe cancelar reserva exitosamente cuando está PENDING")
    void shouldCancelWhenPending() {
        // Given
        reservation.setStatus(ReservationStatus.PENDING);
        String reason = "Cliente canceló por cambio de planes";

        // When
        reservation.cancel(reason);

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservation.getCancelledAt()).isNotNull();
        assertThat(reservation.getCancelledAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(reservation.getCancellationReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("Debe cancelar reserva exitosamente cuando está CONFIRMED")
    void shouldCancelWhenConfirmed() {
        // Given
        reservation.setStatus(ReservationStatus.CONFIRMED);
        String reason = "Cliente canceló por emergencia";

        // When
        reservation.cancel(reason);

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservation.getCancelledAt()).isNotNull();
        assertThat(reservation.getCancellationReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("Debe lanzar excepción al cancelar reserva en estado ACTIVE")
    void shouldThrowExceptionWhenCancellingActive() {
        // Given
        reservation.setStatus(ReservationStatus.ACTIVE);

        // When/Then
        assertThatThrownBy(() -> reservation.cancel("Razón"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Esta reserva no puede ser cancelada");
    }

    @Test
    @DisplayName("Debe lanzar excepción al cancelar reserva en estado COMPLETED")
    void shouldThrowExceptionWhenCancellingCompleted() {
        // Given
        reservation.setStatus(ReservationStatus.COMPLETED);

        // When/Then
        assertThatThrownBy(() -> reservation.cancel("Razón"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Esta reserva no puede ser cancelada");
    }

    @Test
    @DisplayName("Debe lanzar excepción al cancelar reserva ya CANCELLED")
    void shouldThrowExceptionWhenCancellingAlreadyCancelled() {
        // Given
        reservation.setStatus(ReservationStatus.CANCELLED);

        // When/Then
        assertThatThrownBy(() -> reservation.cancel("Razón"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Esta reserva no puede ser cancelada");
    }

    // ==================== Tests para expire() ====================

    @Test
    @DisplayName("Debe expirar reserva exitosamente cuando está PENDING")
    void shouldExpireWhenPending() {
        // Given
        reservation.setStatus(ReservationStatus.PENDING);

        // When
        reservation.expire();

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
    }

    @Test
    @DisplayName("Debe lanzar excepción al expirar reserva en estado CONFIRMED")
    void shouldThrowExceptionWhenExpiringConfirmed() {
        // Given
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // When/Then
        assertThatThrownBy(() -> reservation.expire())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo las reservas PENDING pueden expirar");
    }

    @Test
    @DisplayName("Debe lanzar excepción al expirar reserva en estado ACTIVE")
    void shouldThrowExceptionWhenExpiringActive() {
        // Given
        reservation.setStatus(ReservationStatus.ACTIVE);

        // When/Then
        assertThatThrownBy(() -> reservation.expire())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo las reservas PENDING pueden expirar");
    }

    // ==================== Tests de estado inicial ====================

    @Test
    @DisplayName("Nueva reserva debe iniciar en estado PENDING")
    void newReservationShouldStartAsPending() {
        // When
        Reservation newReservation = new Reservation(
                "RES-002",
                guest,
                room,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                1,
                new BigDecimal("250.00")
        );

        // Then
        assertThat(newReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(newReservation.getCreatedAt()).isNotNull();
    }

    // ==================== Tests de flujo completo ====================

    @Test
    @DisplayName("Debe completar flujo completo: PENDING -> CONFIRMED -> ACTIVE -> COMPLETED")
    void shouldCompleteFullReservationFlow() {
        // Given
        reservation.setStatus(ReservationStatus.PENDING);

        // When/Then - Confirmar pago
        reservation.confirmPayment();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        // When/Then - Check-in
        reservation.checkIn();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.ACTIVE);
        assertThat(reservation.getCheckInTime()).isNotNull();

        // When/Then - Check-out
        reservation.checkOut();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
        assertThat(reservation.getCheckOutTime()).isNotNull();
    }

    @Test
    @DisplayName("Debe completar flujo de cancelación: PENDING -> CANCELLED")
    void shouldCompleteCancellationFlowFromPending() {
        // Given
        reservation.setStatus(ReservationStatus.PENDING);

        // When
        reservation.cancel("Cliente canceló");

        // Then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservation.getCancelledAt()).isNotNull();
        assertThat(reservation.getCancellationReason()).isEqualTo("Cliente canceló");
    }
}
