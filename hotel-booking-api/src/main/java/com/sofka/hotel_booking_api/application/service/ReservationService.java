package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.exception.InvalidDateRangeException;
import com.sofka.hotel_booking_api.domain.exception.RoomNotFoundException;
import com.sofka.hotel_booking_api.domain.model.Guest;
import com.sofka.hotel_booking_api.domain.model.Reservation;
import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.repository.GuestRepository;
import com.sofka.hotel_booking_api.domain.repository.ReservationRepository;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateReservationRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.ReservationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar las reservas del hotel.
 * Historia 3.1: Crear reserva para un huésped
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final GuestService guestService;

    public ReservationService(ReservationRepository reservationRepository,
                            RoomRepository roomRepository,
                            GuestRepository guestRepository,
                            GuestService guestService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.guestService = guestService;
    }

    /**
     * Crea una nueva reserva.
     * 
     * @param request datos de la reserva
     * @return la reserva creada
     */
    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        // 1. Validar fechas
        validateDates(request.checkInDate(), request.checkOutDate());

        // 2. Validar duración de estadía (máximo 30 noches)
        long numberOfNights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        if (numberOfNights > 30) {
            throw new IllegalArgumentException("La estadía máxima es de 30 noches");
        }

        // 3. Buscar la habitación
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new RoomNotFoundException(request.roomId()));

        // 4. Validar capacidad de la habitación
        if (request.numberOfGuests() > room.getCapacity()) {
            throw new IllegalArgumentException(
                    String.format("La habitación tiene capacidad para %d personas, se solicitaron %d",
                            room.getCapacity(), request.numberOfGuests()));
        }

        // 5. Verificar disponibilidad (no hay reservas solapadas)
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                room, request.checkInDate(), request.checkOutDate()
        );
        
        if (!overlappingReservations.isEmpty()) {
            throw new IllegalStateException(
                    String.format("La habitación %s no está disponible para las fechas solicitadas",
                            room.getRoomNumber()));
        }

        // 6. Registrar o actualizar huésped
        Guest guest = guestService.registerOrUpdateGuest(request.guest());

        // 7. Calcular monto total
        BigDecimal totalAmount = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(numberOfNights));

        // 8. Generar número de reserva único
        String reservationNumber = generateReservationNumber();

        // 9. Crear la reserva
        Reservation reservation = new Reservation(
                reservationNumber,
                guest,
                room,
                request.checkInDate(),
                request.checkOutDate(),
                request.numberOfGuests(),
                totalAmount
        );

        // 10. Guardar la reserva
        Reservation savedReservation = reservationRepository.save(reservation);

        // 11. Retornar la respuesta
        return ReservationResponse.fromEntity(savedReservation);
    }

    /**
     * Valida que las fechas de la reserva sean válidas.
     * RN-004: Validaciones de Reserva
     */
    private void validateDates(LocalDate checkInDate, LocalDate checkOutDate) {
        // Validar que la fecha de entrada no sea en el pasado
        if (checkInDate.isBefore(LocalDate.now())) {
            throw new InvalidDateRangeException("La fecha de entrada no puede ser en el pasado");
        }

        // Validar que la fecha de entrada sea antes de la fecha de salida
        if (checkInDate.isAfter(checkOutDate) || checkInDate.isEqual(checkOutDate)) {
            throw new InvalidDateRangeException("La fecha de entrada debe ser anterior a la fecha de salida");
        }
    }

    /**
     * Genera un número de reserva único.
     * Formato: RES-YYYY-XXXXXX
     */
    private String generateReservationNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String uniqueId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return String.format("RES-%s-%s", year, uniqueId);
    }
}
