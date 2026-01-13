package com.sofka.hotel_booking_api.infrastructure.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para confirmar el pago de una reserva.
 * Historia 4.1: Confirmar pago de reserva
 */
public record ConfirmPaymentRequest(
        @NotBlank(message = "El método de pago es obligatorio")
        String paymentMethod,
        
        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        BigDecimal amount,
        
        String reference // Opcional para CASH, obligatorio para CARD/TRANSFER (validado en servicio)
) {
}
