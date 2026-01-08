package com.sofka.hotel_booking_api.infrastructure.dto;

import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.model.RoomType;

import java.math.BigDecimal;

/**
 * DTO para la respuesta de habitación.
 */
public class RoomResponse {

    private Long id;
    private String roomNumber;
    private RoomType roomType;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private Boolean isAvailable;

    // Constructor vacío
    public RoomResponse() {
    }

    // Constructor desde entidad Room
    public RoomResponse(Room room) {
        this.id = room.getId();
        this.roomNumber = room.getRoomNumber();
        this.roomType = room.getRoomType();
        this.capacity = room.getCapacity();
        this.pricePerNight = room.getPricePerNight();
        this.isAvailable = room.getIsAvailable();
    }

    // Constructor con todos los campos
    public RoomResponse(Long id, String roomNumber, RoomType roomType, Integer capacity, 
                        BigDecimal pricePerNight, Boolean isAvailable) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.isAvailable = isAvailable;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
