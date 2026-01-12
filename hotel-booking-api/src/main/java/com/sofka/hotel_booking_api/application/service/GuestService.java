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
        // TODO: Implementar en fase GREEN
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
