package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.domain.model.RoomType;
import com.sofka.hotel_booking_api.infrastructure.constants.ValidationMessages;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO para solicitud de registro de habitación.
 * Encapsula los datos necesarios para crear una nueva habitación en el sistema.
 * 
 * <p>Validaciones aplicadas según RN-006:</p>
 * <ul>
 *   <li>Número de habitación: obligatorio y no vacío</li>
 *   <li>Tipo de habitación: obligatorio (STANDARD, SUPERIOR, SUITE)</li>
 *   <li>Capacidad: obligatoria, entre 1 y 10 personas</li>
 *   <li>Precio por noche: obligatorio, mayor a 0</li>
 * </ul>
 * 
 * @author Sistema Hotel Booking
 * @version 1.0
 * @since 2026-01-07
 * @see ValidationMessages
 */
public class CreateRoomRequest {

    @NotNull(message = ValidationMessages.ROOM_NUMBER_REQUIRED)
    @NotBlank(message = ValidationMessages.ROOM_NUMBER_NOT_BLANK)
    private String roomNumber;

    @NotNull(message = ValidationMessages.ROOM_TYPE_REQUIRED)
    private RoomType roomType;

    @NotNull(message = ValidationMessages.CAPACITY_REQUIRED)
    @Min(value = ValidationMessages.MIN_CAPACITY, message = ValidationMessages.CAPACITY_MIN)
    @Max(value = ValidationMessages.MAX_CAPACITY, message = ValidationMessages.CAPACITY_MAX)
    private Integer capacity;

    @NotNull(message = ValidationMessages.PRICE_REQUIRED)
    @DecimalMin(value = ValidationMessages.MIN_PRICE, message = ValidationMessages.PRICE_MIN)
    private BigDecimal pricePerNight;

    // Constructor vacío
    public CreateRoomRequest() {
    }

    // Constructor con todos los campos
    public CreateRoomRequest(String roomNumber, RoomType roomType, Integer capacity, BigDecimal pricePerNight) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    // Getters y Setters
    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}
