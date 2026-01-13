package com.sofka.hotel_booking_api.infrastructure.controller;

import com.sofka.hotel_booking_api.application.service.PaymentService;
import com.sofka.hotel_booking_api.application.service.ReservationService;
import com.sofka.hotel_booking_api.infrastructure.dto.ConfirmPaymentRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateReservationRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.ReservationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestionar reservas del hotel.
 * Historia 3.1: Crear reserva
 * Historia 4.1: Confirmar pago de reserva
 */
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;

    public ReservationController(ReservationService reservationService, PaymentService paymentService) {
        this.reservationService = reservationService;
        this.paymentService = paymentService;
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

    /**
     * Endpoint para confirmar el pago de una reserva.
     * POST /api/reservations/{id}/confirm-payment
     * Historia 4.1: Confirmar pago de reserva
     *
     * @param id ID de la reserva
     * @param request datos del pago (método, monto, referencia)
     * @return 200 OK cuando el pago se confirma exitosamente
     */
    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<Void> confirmPayment(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmPaymentRequest request) {
        paymentService.confirmPayment(
                id, 
                request.paymentMethod(), 
                request.amount(), 
                request.reference()
        );
        return ResponseEntity.ok().build();
    }
}
