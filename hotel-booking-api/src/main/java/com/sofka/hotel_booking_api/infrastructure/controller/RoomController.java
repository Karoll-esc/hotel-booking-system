package com.sofka.hotel_booking_api.infrastructure.controller;

import com.sofka.hotel_booking_api.application.service.RoomService;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
