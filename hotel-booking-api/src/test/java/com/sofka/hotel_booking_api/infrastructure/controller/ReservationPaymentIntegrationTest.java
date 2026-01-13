package com.sofka.hotel_booking_api.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.hotel_booking_api.domain.model.Guest;
import com.sofka.hotel_booking_api.domain.model.Reservation;
import com.sofka.hotel_booking_api.domain.model.ReservationStatus;
import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.domain.repository.GuestRepository;
import com.sofka.hotel_booking_api.domain.repository.ReservationRepository;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.ConfirmPaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración E2E para confirmación de pagos.
 * Usa H2 en memoria para probar el flujo completo.
 * 
 * Historia 4.1: Confirmar pago de reserva
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ReservationController - Tests de Confirmación de Pago E2E")
class ReservationPaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Room testRoom;
    private Guest testGuest;
    private Reservation pendingReservation;

    @BeforeEach
    void setUp() {
        // Crear habitación de prueba
        testRoom = new Room("201", RoomType.SUPERIOR, 3, new BigDecimal("200.00"));
        testRoom = roomRepository.save(testRoom);

        // Crear huésped de prueba
        testGuest = new Guest(
                "Carlos",
                "Rodríguez",
                "11223344C",
                "carlos.rodriguez@email.com",
                "+34 611223344"
        );
        testGuest = guestRepository.save(testGuest);

        // Crear reserva en estado PENDING (2 noches * $200 = $400.00)
        pendingReservation = new Reservation(
                "RES-TEST-001",
                testGuest,
                testRoom,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2,
                new BigDecimal("400.00")
        );
        pendingReservation = reservationRepository.save(pendingReservation);
    }

    @Test
    @DisplayName("POST /api/reservations/{id}/confirm-payment - Debe confirmar pago con CASH exitosamente")
    void shouldConfirmPaymentWithCashSuccessfully() throws Exception {
        // Given - Pago en efectivo con monto correcto
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                "CASH",
                new BigDecimal("400.00"),
                null  // CASH no requiere referencia
        );

        // When/Then - Debe confirmar el pago y retornar 200 OK
        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());

        // Verificar que el estado cambió a CONFIRMED
        Reservation updatedReservation = reservationRepository.findById(pendingReservation.getId()).orElseThrow();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("POST /api/reservations/{id}/confirm-payment - Debe confirmar pago con CARD exitosamente")
    void shouldConfirmPaymentWithCardSuccessfully() throws Exception {
        // Given - Pago con tarjeta con monto y referencia correctos
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                "CARD",
                new BigDecimal("400.00"),
                "AUTH-123456789"  // CARD requiere referencia
        );

        // When/Then - Debe confirmar el pago y retornar 200 OK
        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());

        // Verificar que el estado cambió a CONFIRMED
        Reservation updatedReservation = reservationRepository.findById(pendingReservation.getId()).orElseThrow();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("POST /api/reservations/{id}/confirm-payment - Debe confirmar pago con TRANSFER exitosamente")
    void shouldConfirmPaymentWithTransferSuccessfully() throws Exception {
        // Given - Pago por transferencia con monto y referencia correctos
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                "TRANSFER",
                new BigDecimal("400.00"),
                "TRF-2026011300001"  // TRANSFER requiere referencia
        );

        // When/Then - Debe confirmar el pago y retornar 200 OK
        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());

        // Verificar que el estado cambió a CONFIRMED
        Reservation updatedReservation = reservationRepository.findById(pendingReservation.getId()).orElseThrow();
        assertThat(updatedReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("POST /api/reservations/{id}/confirm-payment - Debe retornar 400 cuando monto no coincide")
    void shouldReturn400WhenAmountDoesNotMatch() throws Exception {
        // Given - Monto incorrecto (debería ser 400.00)
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                "CASH",
                new BigDecimal("350.00"),  // Monto incorrecto
                null
        );

        // When/Then - Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());

        // Verificar que el estado NO cambió (sigue PENDING)
        Reservation unchangedReservation = reservationRepository.findById(pendingReservation.getId()).orElseThrow();
        assertThat(unchangedReservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("POST /api/reservations/{id}/confirm-payment - Debe retornar 400 cuando CARD no tiene referencia")
    void shouldReturn400WhenCardPaymentWithoutReference() throws Exception {
        // Given - Pago con tarjeta SIN referencia
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                "CARD",
                new BigDecimal("400.00"),
                null  // CARD requiere referencia
        );

        // When/Then - Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reservations/{id}/confirm-payment - Debe retornar 400 cuando método de pago es inválido")
    void shouldReturn400WhenPaymentMethodIsInvalid() throws Exception {
        // Given - Método de pago inválido
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                "BITCOIN",  // Método inválido
                new BigDecimal("400.00"),
                "REF-123"
        );

        // When/Then - Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reservations/{id}/confirm-payment - Debe retornar 404 cuando reserva no existe")
    void shouldReturn404WhenReservationNotFound() throws Exception {
        // Given - ID de reserva inexistente
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                "CASH",
                new BigDecimal("400.00"),
                null
        );

        // When/Then - Debe retornar 404 Not Found
        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/reservations/{id}/confirm-payment - Debe retornar 400 cuando reserva ya está confirmada")
    void shouldReturn400WhenReservationAlreadyConfirmed() throws Exception {
        // Given - Confirmar pago primero
        ConfirmPaymentRequest paymentRequest = new ConfirmPaymentRequest(
                "CASH",
                new BigDecimal("400.00"),
                null
        );

        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());

        // When/Then - Intentar confirmar nuevamente debe fallar
        mockMvc.perform(post("/api/reservations/{id}/confirm-payment", pendingReservation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }
}
