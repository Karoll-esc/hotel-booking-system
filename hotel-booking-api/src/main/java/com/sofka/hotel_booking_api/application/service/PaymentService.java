package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio para gestionar pagos de reservas.
 * Historia 4.1: Confirmar pago de reserva
 */
@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;

    public PaymentService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Confirma el pago de una reserva.
     * 
     * @param reservationId ID de la reserva
     * @param paymentMethod método de pago (CASH, CARD, TRANSFER)
     * @param amount monto pagado
     * @param reference número de referencia/autorización (requerido para CARD y TRANSFER)
     */
    @Transactional
    public void confirmPayment(Long reservationId, String paymentMethod, BigDecimal amount, String reference) {
        // TODO: Implementar en fase GREEN
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
