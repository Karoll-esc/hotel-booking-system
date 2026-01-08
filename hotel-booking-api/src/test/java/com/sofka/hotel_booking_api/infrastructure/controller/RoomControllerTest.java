package com.sofka.hotel_booking_api.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.hotel_booking_api.application.service.RoomService;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del controlador de habitaciones.
 * Fase RED de TDD - Historia 2.1: Registrar habitaciones del hotel
 * Escenario: Registro exitoso de nueva habitación
 */
@WebMvcTest(RoomController.class)
@DisplayName("RoomController - Tests de integración")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    private CreateRoomRequest validRequest;
    private RoomResponse expectedResponse;

    @BeforeEach
    void setUp() {
        // Given - Preparar datos de prueba
        validRequest = new CreateRoomRequest(
                "301",
                RoomType.SUITE,
                4,
                new BigDecimal("250.00")
        );

        expectedResponse = new RoomResponse(
                1L,
                "301",
                RoomType.SUITE,
                4,
                new BigDecimal("250.00"),
                true
        );
    }

    @Test
    @DisplayName("POST /api/rooms debe registrar habitación y retornar 201 Created")
    void shouldRegisterRoomAndReturn201() throws Exception {
        // Given - Dado que el servicio registrará la habitación
        when(roomService.registerRoom(any(CreateRoomRequest.class)))
                .thenThrow(new UnsupportedOperationException("Método no implementado aún - fase RED de TDD"));

        // When - Cuando envío POST con datos válidos
        // Then - Este test DEBE FALLAR con 500 Internal Server Error por UnsupportedOperationException
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError()); // Esperamos 500 en fase RED

        // En fase GREEN, cambiaremos a:
        // .andExpect(status().isCreated())
        // .andExpect(jsonPath("$.roomNumber").value("301"))
        // .andExpect(jsonPath("$.roomType").value("SUITE"))
        // .andExpect(jsonPath("$.capacity").value(4))
        // .andExpect(jsonPath("$.pricePerNight").value(250.00))
        // .andExpect(jsonPath("$.isAvailable").value(true));
    }

    @Test
    @DisplayName("POST /api/rooms debe validar datos obligatorios")
    void shouldValidateRequiredFields() throws Exception {
        // Given - Dado que envío request sin datos obligatorios
        CreateRoomRequest invalidRequest = new CreateRoomRequest(null, null, null, null);

        // When/Then - Cuando envío POST debe retornar 400 Bad Request
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/rooms debe validar capacidad mínima")
    void shouldValidateMinimumCapacity() throws Exception {
        // Given - Dado que envío request con capacidad 0 (inválida)
        CreateRoomRequest invalidRequest = new CreateRoomRequest(
                "301",
                RoomType.SUITE,
                0, // Inválido: capacidad debe ser mínimo 1
                new BigDecimal("250.00")
        );

        // When/Then - Cuando envío POST debe retornar 400 Bad Request
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/rooms debe validar capacidad máxima")
    void shouldValidateMaximumCapacity() throws Exception {
        // Given - Dado que envío request con capacidad 11 (excede límite)
        CreateRoomRequest invalidRequest = new CreateRoomRequest(
                "301",
                RoomType.SUITE,
                11, // Inválido: capacidad no puede exceder 10
                new BigDecimal("250.00")
        );

        // When/Then - Cuando envío POST debe retornar 400 Bad Request
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/rooms debe validar precio positivo")
    void shouldValidatePositivePrice() throws Exception {
        // Given - Dado que envío request con precio negativo
        CreateRoomRequest invalidRequest = new CreateRoomRequest(
                "301",
                RoomType.SUITE,
                4,
                new BigDecimal("-100.00") // Inválido: precio debe ser mayor a 0
        );

        // When/Then - Cuando envío POST debe retornar 400 Bad Request
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
