package com.sofka.hotel_booking_api.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un huésped del hotel.
 * Según RN-007: Validaciones de Huésped
 */
@Entity
@Table(name = "guests")
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotNull(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotNull(message = "El número de documento es obligatorio")
    @Size(min = 5, max = 50, message = "El documento debe tener entre 5 y 50 caracteres")
    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @NotNull(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @NotNull(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "El teléfono debe tener un formato válido")
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructor vacío requerido por JPA
    protected Guest() {
    }

    // Constructor para crear huésped
    public Guest(String firstName, String lastName, String documentNumber, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentNumber = documentNumber;
        this.email = email;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guest guest = (Guest) o;
        return Objects.equals(id, guest.id) && Objects.equals(documentNumber, guest.documentNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, documentNumber);
    }

    @Override
    public String toString() {
        return "Guest{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", documentNumber='" + documentNumber + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
