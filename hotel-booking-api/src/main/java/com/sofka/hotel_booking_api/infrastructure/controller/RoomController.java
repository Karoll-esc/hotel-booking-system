package com.sofka.hotel_booking_api.infrastructure.controller;

import com.sofka.hotel_booking_api.application.service.RoomService;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateRoomRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.RoomResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Según Historia 2.1: Registrar habitaciones del hotel - Escenario: Registro exitoso
     *
     * @param request datos de la habitación a registrar
     * @return la habitación registrada con status 201 Created
     */
    @PostMapping
    public ResponseEntity<RoomResponse> registerRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.registerRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
