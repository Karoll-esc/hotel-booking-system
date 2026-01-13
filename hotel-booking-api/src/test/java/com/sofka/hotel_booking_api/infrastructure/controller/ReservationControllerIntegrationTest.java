package com.sofka.hotel_booking_api.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateGuestRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateReservationRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración E2E para ReservationController.
 * Usa H2 en memoria para probar el flujo completo desde el controlador hasta la base de datos.
 * 
 * Historia 3.1: Crear reserva
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("ReservationController - Tests de Integración E2E")
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    private Room testRoom;
    private CreateGuestRequest validGuestRequest;
    private CreateReservationRequest validReservationRequest;

    @BeforeEach
    void setUp() {
        // Crear habitación de prueba en la base de datos H2
        testRoom = new Room("101", RoomType.STANDARD, 2, new BigDecimal("150.00"));
        testRoom = roomRepository.save(testRoom);

        // Datos del huésped válidos
        validGuestRequest = new CreateGuestRequest(
                "Juan",
                "Pérez",
                "12345678A",
                "juan.perez@email.com",
                "+34 612345678"
        );

        // Request de reserva válida
        validReservationRequest = new CreateReservationRequest(
                validGuestRequest,
                testRoom.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );
    }

    @Test
    @DisplayName("POST /api/reservations - Debe crear reserva exitosamente y retornar 201 Created")
    void shouldCreateReservationSuccessfully() throws Exception {
        // When/Then - Cuando envío POST con datos válidos, debe crear la reserva
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReservationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.reservationNumber").isNotEmpty())
                .andExpect(jsonPath("$.guest.firstName").value("Juan"))
                .andExpect(jsonPath("$.guest.lastName").value("Pérez"))
                .andExpect(jsonPath("$.guest.email").value("juan.perez@email.com"))
                .andExpect(jsonPath("$.room.id").value(testRoom.getId()))
                .andExpect(jsonPath("$.room.roomNumber").value("101"))
                .andExpect(jsonPath("$.checkInDate").value(LocalDate.now().plusDays(1).toString()))
                .andExpect(jsonPath("$.checkOutDate").value(LocalDate.now().plusDays(3).toString()))
                .andExpect(jsonPath("$.numberOfGuests").value(2))
                .andExpect(jsonPath("$.numberOfNights").value(2))
                .andExpect(jsonPath("$.totalAmount").value(300.00)) // 2 noches * 150.00
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/reservations - Debe retornar 400 cuando faltan datos del huésped")
    void shouldReturn400WhenGuestDataIsMissing() throws Exception {
        // Given - Request sin datos del huésped
        CreateReservationRequest invalidRequest = new CreateReservationRequest(
                null,
                testRoom.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );

        // When/Then - Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reservations - Debe retornar 400 cuando check-out es anterior a check-in")
    void shouldReturn400WhenCheckOutIsBeforeCheckIn() throws Exception {
        // Given - Fechas inválidas (check-out antes de check-in)
        CreateReservationRequest invalidRequest = new CreateReservationRequest(
                validGuestRequest,
                testRoom.getId(),
                LocalDate.now().plusDays(5),  // Check-in después
                LocalDate.now().plusDays(3),  // Check-out antes
                2
        );

        // When/Then - Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reservations - Debe retornar 400 cuando número de huéspedes excede capacidad")
    void shouldReturn400WhenGuestsExceedRoomCapacity() throws Exception {
        // Given - Más huéspedes que capacidad de la habitación (capacidad=2)
        CreateReservationRequest invalidRequest = new CreateReservationRequest(
                validGuestRequest,
                testRoom.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                5  // Excede capacidad de 2
        );

        // When/Then - Debe retornar 400 Bad Request
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/reservations - Debe retornar 404 cuando la habitación no existe")
    void shouldReturn404WhenRoomNotFound() throws Exception {
        // Given - ID de habitación inexistente
        CreateReservationRequest invalidRequest = new CreateReservationRequest(
                validGuestRequest,
                99999L,  // ID inexistente
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );

        // When/Then - Debe retornar 404 Not Found
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/reservations - Debe retornar 400 cuando habitación no está disponible en las fechas")
    void shouldReturn400WhenRoomNotAvailableForDates() throws Exception {
        // Given - Primero crear una reserva para ocupar la habitación
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validReservationRequest)))
                .andExpect(status().isCreated());

        // Intentar crear otra reserva en las mismas fechas
        CreateGuestRequest otherGuest = new CreateGuestRequest(
                "María",
                "García",
                "87654321B",
                "maria.garcia@email.com",
                "+34 698765432"
        );

        CreateReservationRequest conflictingRequest = new CreateReservationRequest(
                otherGuest,
                testRoom.getId(),
                LocalDate.now().plusDays(1),  // Mismas fechas
                LocalDate.now().plusDays(3),
                2
        );

        // When/Then - Debe retornar 400 Bad Request (habitación no disponible)
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(conflictingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("no está disponible")));
    }
}
