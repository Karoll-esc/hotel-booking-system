package com.sofka.hotel_booking_api.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entidad que representa una reserva de hotel.
 * Según RN-004: Validaciones de Reserva
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El número de reserva es obligatorio")
    @Column(name = "reservation_number", nullable = false, unique = true, length = 50)
    private String reservationNumber;

    @NotNull(message = "El huésped es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @NotNull(message = "La habitación es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "La fecha de entrada es obligatoria")
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotNull(message = "La fecha de salida es obligatoria")
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @NotNull(message = "El número de huéspedes es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 huésped")
    @Column(name = "number_of_guests", nullable = false)
    private Integer numberOfGuests;

    @NotNull(message = "El monto total es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @NotNull(message = "El estado de la reserva es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    // Constructor vacío requerido por JPA
    protected Reservation() {
    }

    // Constructor para crear reserva
    public Reservation(String reservationNumber, Guest guest, Room room, 
                      LocalDate checkInDate, LocalDate checkOutDate, 
                      Integer numberOfGuests, BigDecimal totalAmount) {
        this.reservationNumber = reservationNumber;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
        this.totalAmount = totalAmount;
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calcula el número de noches de la reserva.
     * @return número de noches
     */
    public long getNumberOfNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    /**
     * Verifica si la reserva está activa (check-in realizado, check-out pendiente).
     * @return true si está activa
     */
    public boolean isActive() {
        return status == ReservationStatus.ACTIVE;
    }

    /**
     * Verifica si la reserva puede ser cancelada.
     * RN-001: Se permiten cancelaciones para PENDING, CONFIRMED y ACTIVE.
     * @return true si puede ser cancelada
     */
    public boolean isCancellable() {
        return status == ReservationStatus.PENDING 
            || status == ReservationStatus.CONFIRMED 
            || status == ReservationStatus.ACTIVE;
    }

    /**
     * Calcula el porcentaje de reembolso según la política de cancelación RN-001.
     * - 7+ días antes del check-in: 100% de reembolso
     * - 2-6 días antes del check-in: 50% de reembolso
     * - Menos de 2 días: 0% de reembolso
     * - Reserva activa (0 días): 0% de reembolso
     * 
     * @return porcentaje de reembolso (0, 50, o 100)
     */
    public int calculateRefundPercentage() {
        // Si la reserva está activa (check-in ya realizado), no hay reembolso
        if (status == ReservationStatus.ACTIVE) {
            return 0;
        }
        
        // Calcular días hasta el check-in
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);
        
        // Aplicar política RN-001
        if (daysUntilCheckIn >= 7) {
            return 100; // Sin penalidad
        } else if (daysUntilCheckIn >= 2) {
            return 50;  // 50% de penalidad
        } else {
            return 0;   // 100% de penalidad
        }
    }

    /**
     * Confirma el pago de la reserva.
     */
    public void confirmPayment() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar reservas en estado PENDING");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    /**
     * Realiza el check-in de la reserva.
     */
    public void checkIn() {
        if (status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Solo se puede hacer check-in en reservas CONFIRMADAS");
        }
        this.status = ReservationStatus.ACTIVE;
        this.checkInTime = LocalDateTime.now();
    }

    /**
     * Realiza el check-out de la reserva.
     */
    public void checkOut() {
        if (status != ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Solo se puede hacer check-out en reservas ACTIVAS");
        }
        this.status = ReservationStatus.COMPLETED;
        this.checkOutTime = LocalDateTime.now();
    }

    /**
     * Cancela la reserva.
     * @param reason motivo de cancelación
     */
    public void cancel(String reason) {
        if (!isCancellable()) {
            throw new IllegalStateException("Esta reserva no puede ser cancelada");
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /**
     * Marca la reserva como expirada.
     */
    public void expire() {
        if (status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Solo las reservas PENDING pueden expirar");
        }
        this.status = ReservationStatus.EXPIRED;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setReservationNumber(String reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(reservationNumber, that.reservationNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reservationNumber);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", reservationNumber='" + reservationNumber + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", numberOfGuests=" + numberOfGuests +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
