package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.exception.ReservationNotFoundException;
import com.sofka.hotel_booking_api.domain.model.Reservation;
import com.sofka.hotel_booking_api.domain.model.ReservationStatus;
import com.sofka.hotel_booking_api.domain.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

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
        // 1. Validar que el método de pago sea válido
        validatePaymentMethod(paymentMethod);
        
        // 2. Validar que se proporcione referencia para CARD y TRANSFER
        validateReference(paymentMethod, reference);
        
        // 3. Buscar la reserva
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));
        
        // 4. Validar que la reserva esté en estado PENDING
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            if (reservation.getStatus() == ReservationStatus.EXPIRED) {
                throw new IllegalStateException("La reserva ha expirado. No se puede confirmar el pago");
            }
            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                throw new IllegalStateException("La reserva ya tiene el pago confirmado");
            }
            throw new IllegalStateException(
                    String.format("No se puede confirmar el pago. Estado actual: %s", reservation.getStatus()));
        }
        
        // 5. Validar que el monto coincida con el total de la reserva
        if (amount.compareTo(reservation.getTotalAmount()) != 0) {
            throw new IllegalArgumentException(
                    String.format("El monto pagado (%s) no coincide con el total de la reserva (%s)",
                            amount, reservation.getTotalAmount()));
        }
        
        // 6. Confirmar el pago
        reservation.confirmPayment();
        
        // 7. Guardar la reserva actualizada
        reservationRepository.save(reservation);
        
        // Nota: En una implementación real, aquí se guardaría también:
        // - El método de pago
        // - La referencia/número de autorización
        // - La fecha y hora de confirmación
        // Esto se haría en una entidad Payment separada según RN-011
    }

    /**
     * Valida que el método de pago sea válido.
     */
    private void validatePaymentMethod(String paymentMethod) {
        List<String> validMethods = Arrays.asList("CASH", "CARD", "TRANSFER");
        if (!validMethods.contains(paymentMethod)) {
            throw new IllegalArgumentException(
                    String.format("Método de pago inválido: %s. Métodos válidos: CASH, CARD, TRANSFER", 
                            paymentMethod));
        }
    }

    /**
     * Valida que se proporcione referencia para CARD y TRANSFER.
     */
    private void validateReference(String paymentMethod, String reference) {
        if (("CARD".equals(paymentMethod) || "TRANSFER".equals(paymentMethod)) 
                && (reference == null || reference.trim().isEmpty())) {
            throw new IllegalArgumentException(
                    String.format("El método de pago %s requiere un número de referencia/autorización", 
                            paymentMethod));
        }
    }
}
