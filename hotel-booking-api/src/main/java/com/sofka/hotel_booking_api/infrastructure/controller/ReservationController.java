package com.sofka.hotel_booking_api.infrastructure.controller;

import com.sofka.hotel_booking_api.application.service.ReservationService;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateReservationRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar reservas del hotel.
 * Historia 3.1: Crear reserva
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Endpoint para crear una nueva reserva.
     * POST /api/reservations
     *
     * @param request datos de la reserva a crear
     * @return la reserva creada con status 201 Created
     */
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
