package com.sofka.hotel_booking_api.infrastructure.controller;

import com.sofka.hotel_booking_api.application.service.RoomService;
import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador REST para gestionar habitaciones del hotel.
 */
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * Endpoint para registrar una nueva habitación.
     * POST /api/rooms
     *
     * @param request datos de la habitación a registrar
     * @return la habitación registrada con status 201 Created
     */
    @PostMapping
    public ResponseEntity<RoomResponse> registerRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.registerRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para obtener todas las habitaciones.
     * GET /api/rooms
     *
     * @return lista de todas las habitaciones
     */
    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<RoomResponse> rooms = roomService.getAllRooms();
        return ResponseEntity.ok(rooms);
    }

    /**
     * Endpoint para obtener una habitación por ID.
     * GET /api/rooms/{id}
     *
     * @param id el ID de la habitación
     * @return la habitación encontrada con status 200 OK
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para actualizar una habitación existente.
     * PUT /api/rooms/{id}
     *
     * @param id el ID de la habitación a actualizar
     * @param request los nuevos datos de la habitación
     * @return la habitación actualizada con status 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para eliminar una habitación.
     * DELETE /api/rooms/{id}
     *
     * @param id el ID de la habitación a eliminar
     * @return status 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para consultar habitaciones disponibles en un rango de fechas.
     * GET /api/rooms/available
     * Historia 2.2: Consultar estado de ocupación de habitaciones
     *
     * @param checkIn fecha de entrada (obligatoria)
     * @param checkOut fecha de salida (obligatoria)
     * @param roomType tipo de habitación (opcional)
     * @return lista de habitaciones disponibles
     */
    @GetMapping("/available")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) RoomType roomType) {
        List<RoomResponse> availableRooms = roomService.getAvailableRooms(checkIn, checkOut, roomType);
        return ResponseEntity.ok(availableRooms);
    }
}
