package com.sofka.hotel_booking_api.infrastructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sofka.hotel_booking_api.application.service.RoomService;
import com.sofka.hotel_booking_api.domain.exception.DuplicateRoomNumberException;
import com.sofka.hotel_booking_api.domain.exception.RoomNotFoundException;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del controlador de habitaciones.
 * Fase GREEN de TDD - Historia 2.1: Registrar habitaciones del hotel
 * Escenario: Registro exitoso de nueva habitación
 */
@WebMvcTest(controllers = RoomController.class, 
            excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
            })
@Import(com.sofka.hotel_booking_api.infrastructure.exception.GlobalExceptionHandler.class)
@DisplayName("RoomController - Tests de integración")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
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
        // Given - Dado que el servicio registrará la habitación exitosamente
        when(roomService.registerRoom(any(CreateRoomRequest.class)))
                .thenReturn(expectedResponse);

        // When/Then - Cuando envío POST con datos válidos
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("301"))
                .andExpect(jsonPath("$.roomType").value("SUITE"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.pricePerNight").value(250.00))
                .andExpect(jsonPath("$.isAvailable").value(true));
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

    @Test
    @DisplayName("POST /api/rooms debe retornar 409 Conflict cuando el número de habitación ya existe")
    void shouldReturn409WhenRoomNumberAlreadyExists() throws Exception {
        // Given - Dado que el servicio lanza excepción de número duplicado
        when(roomService.registerRoom(any(CreateRoomRequest.class)))
                .thenThrow(new DuplicateRoomNumberException("301"));

        // When/Then - Cuando envío POST debe retornar 409 Conflict
        mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Número de habitación duplicado"))
                .andExpect(jsonPath("$.message").value("Ya existe una habitación registrada con el número '301'"));
    }

    // ============================================
    // RED PHASE - Tests for GET by ID endpoint
    // ============================================

    @Test
    @DisplayName("GET /api/rooms/{id} debe retornar habitación existente con 200 OK")
    void shouldReturnRoomByIdWhenExists() throws Exception {
        // Given - Dado que existe una habitación con ID 1
        Long roomId = 1L;
        when(roomService.getRoomById(roomId))
                .thenReturn(expectedResponse);

        // When/Then - Cuando consulto por ID debe retornar 200 OK
        mockMvc.perform(get("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomNumber").value("301"))
                .andExpect(jsonPath("$.roomType").value("SUITE"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.pricePerNight").value(250.00))
                .andExpect(jsonPath("$.isAvailable").value(true));
    }

    @Test
    @DisplayName("GET /api/rooms/{id} debe retornar 404 cuando habitación no existe")
    void shouldReturn404WhenRoomNotFound() throws Exception {
        // Given - Dado que no existe habitación con ID 999
        Long roomId = 999L;
        when(roomService.getRoomById(roomId))
                .thenThrow(new RoomNotFoundException(roomId));

        // When/Then - Cuando consulto por ID inexistente debe retornar 404
        mockMvc.perform(get("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Habitación no encontrada"))
                .andExpect(jsonPath("$.message").value("No se encontró la habitación con ID: 999"));
    }

    // ============================================
    // RED PHASE - Tests for PUT endpoint
    // ============================================

    @Test
    @DisplayName("PUT /api/rooms/{id} debe actualizar habitación y retornar 200 OK")
    void shouldUpdateRoomAndReturn200() throws Exception {
        // Given - Dado que actualizo una habitación existente
        Long roomId = 1L;
        CreateRoomRequest updateRequest = new CreateRoomRequest(
                "302",
                RoomType.DELUXE,
                3,
                new BigDecimal("200.00")
        );
        RoomResponse updatedResponse = new RoomResponse(
                roomId,
                "302",
                RoomType.DELUXE,
                3,
                new BigDecimal("200.00"),
                true
        );
        when(roomService.updateRoom(any(Long.class), any(CreateRoomRequest.class)))
                .thenReturn(updatedResponse);

        // When/Then - Cuando envío PUT debe actualizar y retornar 200 OK
        mockMvc.perform(put("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roomId))
                .andExpect(jsonPath("$.roomNumber").value("302"))
                .andExpect(jsonPath("$.roomType").value("DELUXE"))
                .andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.pricePerNight").value(200.00));
    }

    @Test
    @DisplayName("PUT /api/rooms/{id} debe retornar 404 cuando habitación no existe")
    void shouldReturn404WhenUpdatingNonExistentRoom() throws Exception {
        // Given - Dado que intento actualizar habitación inexistente
        Long roomId = 999L;
        when(roomService.updateRoom(any(Long.class), any(CreateRoomRequest.class)))
                .thenThrow(new RoomNotFoundException(roomId));

        // When/Then - Cuando envío PUT debe retornar 404
        mockMvc.perform(put("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Habitación no encontrada"));
    }

    @Test
    @DisplayName("PUT /api/rooms/{id} debe validar datos obligatorios")
    void shouldValidateRequiredFieldsOnUpdate() throws Exception {
        // Given - Dado que envío datos inválidos en actualización
        Long roomId = 1L;
        CreateRoomRequest invalidRequest = new CreateRoomRequest(null, null, null, null);

        // When/Then - Cuando envío PUT con datos inválidos debe retornar 400
        mockMvc.perform(put("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/rooms/{id} debe validar capacidad mínima en actualización")
    void shouldValidateMinimumCapacityOnUpdate() throws Exception {
        // Given - Dado que envío request con capacidad 0 en actualización
        Long roomId = 1L;
        CreateRoomRequest invalidRequest = new CreateRoomRequest(
                "302",
                RoomType.DELUXE,
                0, // Inválido
                new BigDecimal("200.00")
        );

        // When/Then - Cuando envío PUT con capacidad inválida debe retornar 400
        mockMvc.perform(put("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/rooms/{id} debe validar precio positivo en actualización")
    void shouldValidatePositivePriceOnUpdate() throws Exception {
        // Given - Dado que envío request con precio negativo en actualización
        Long roomId = 1L;
        CreateRoomRequest invalidRequest = new CreateRoomRequest(
                "302",
                RoomType.DELUXE,
                3,
                new BigDecimal("-100.00") // Inválido
        );

        // When/Then - Cuando envío PUT con precio inválido debe retornar 400
        mockMvc.perform(put("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ============================================
    // RED PHASE - Tests for DELETE endpoint
    // ============================================

    @Test
    @DisplayName("DELETE /api/rooms/{id} debe eliminar habitación y retornar 204 No Content")
    void shouldDeleteRoomAndReturn204() throws Exception {
        // Given - Dado que elimino una habitación existente
        Long roomId = 1L;

        // When/Then - Cuando envío DELETE debe retornar 204 No Content
        mockMvc.perform(delete("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/rooms/{id} debe retornar 404 cuando habitación no existe")
    void shouldReturn404WhenDeletingNonExistentRoom() throws Exception {
        // Given - Dado que intento eliminar habitación inexistente
        Long roomId = 999L;
        doThrow(new RoomNotFoundException(roomId))
                .when(roomService).deleteRoom(roomId);

        // When/Then - Cuando envío DELETE debe retornar 404
        mockMvc.perform(delete("/api/rooms/{id}", roomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Habitación no encontrada"));
    }
}
