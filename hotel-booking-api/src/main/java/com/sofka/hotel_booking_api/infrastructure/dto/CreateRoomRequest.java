package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.domain.model.RoomType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO para solicitud de registro de habitación.
 * Según RN-006: Validaciones de Habitación
 */
public class CreateRoomRequest {

    @NotNull(message = "El número de habitación es obligatorio")
    @NotBlank(message = "El número de habitación no puede estar vacío")
    private String roomNumber;

    @NotNull(message = "El tipo de habitación es obligatorio")
    private RoomType roomType;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser al menos 1 persona")
    @Max(value = 10, message = "La capacidad no puede exceder 10 personas")
    private Integer capacity;

    @NotNull(message = "El precio por noche es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
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
