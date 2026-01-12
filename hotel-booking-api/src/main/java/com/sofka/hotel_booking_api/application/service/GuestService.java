package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.model.Guest;
import com.sofka.hotel_booking_api.domain.repository.GuestRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateGuestRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para gestionar los huéspedes del hotel.
 * Historia 3.2: Registrar información completa del huésped
 */
@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    /**
     * Registra un nuevo huésped o actualiza uno existente si ya existe con el mismo documento.
     * 
     * @param request datos del huésped
     * @return el huésped registrado o actualizado
     */
    @Transactional
    public Guest registerOrUpdateGuest(CreateGuestRequest request) {
        // 1. Buscar si ya existe un huésped con ese documento
        return guestRepository.findByDocumentNumber(request.documentNumber())
                .map(existingGuest -> {
                    // Actualizar datos del huésped existente
                    existingGuest.setFirstName(request.firstName());
                    existingGuest.setLastName(request.lastName());
                    existingGuest.setEmail(request.email());
                    existingGuest.setPhone(request.phone());
                    return guestRepository.save(existingGuest);
                })
                .orElseGet(() -> {
                    // Crear nuevo huésped
                    Guest newGuest = new Guest(
                            request.firstName(),
                            request.lastName(),
                            request.documentNumber(),
                            request.email(),
                            request.phone()
                    );
                    return guestRepository.save(newGuest);
                });
    }
}
