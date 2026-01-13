package com.sofka.hotel_booking_api.application.service;

import com.sofka.hotel_booking_api.domain.exception.InvalidDateRangeException;
import com.sofka.hotel_booking_api.domain.exception.RoomNotFoundException;
import com.sofka.hotel_booking_api.domain.model.Guest;
import com.sofka.hotel_booking_api.domain.model.Reservation;
import com.sofka.hotel_booking_api.domain.model.ReservationStatus;
import com.sofka.hotel_booking_api.domain.model.Room;
import com.sofka.hotel_booking_api.domain.repository.GuestRepository;
import com.sofka.hotel_booking_api.domain.repository.ReservationRepository;
import com.sofka.hotel_booking_api.domain.repository.RoomRepository;
import com.sofka.hotel_booking_api.infrastructure.dto.CreateReservationRequest;
import com.sofka.hotel_booking_api.infrastructure.dto.ReservationResponse;
import com.sofka.hotel_booking_api.infrastructure.dto.TodayReservationsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * Busca reservas por número de reserva o nombre de huésped.
     * Historia 5.1: Buscar reservas existentes
     * 
     * @param reservationNumber número de reserva (búsqueda exacta, opcional)
     * @param guestName nombre o apellido del huésped (búsqueda parcial, opcional)
     * @return lista de reservas que coinciden con los criterios (puede estar vacía)
     * @throws IllegalArgumentException si no se proporciona ningún criterio de búsqueda
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> searchReservations(String reservationNumber, String guestName) {
        // Validar que al menos un criterio de búsqueda esté presente
        if (reservationNumber == null && guestName == null) {
            throw new IllegalArgumentException(
                "Debe proporcionar al menos un criterio de búsqueda: número de reserva o nombre del huésped"
            );
        }

        // Priorizar búsqueda por número de reserva (búsqueda exacta)
        if (reservationNumber != null) {
            return reservationRepository.findByReservationNumber(reservationNumber)
                    .map(ReservationResponse::fromEntity)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        // Búsqueda por nombre de huésped (parcial, case-insensitive)
        List<Reservation> reservations = reservationRepository.findByGuestNameContainingIgnoreCase(guestName);
        
        return reservations.stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las reservas del día actual (check-ins y check-outs programados).
     * Historia 5.2: Ver reservas del día
     * 
     * @return objeto con listas de check-ins y check-outs para hoy
     */
    @Transactional(readOnly = true)
    public TodayReservationsResponse getTodayReservations() {
        LocalDate today = LocalDate.now();
        
        // Obtener check-ins del día: CONFIRMED (pendientes) y ACTIVE (ya realizados hoy)
        List<ReservationResponse> checkIns = reservationRepository
                .findByCheckInDateAndStatusInOrderByCheckInDateAsc(
                    today, 
                    List.of(ReservationStatus.CONFIRMED, ReservationStatus.ACTIVE)
                )
                .stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
        
        // Obtener check-outs del día (reservas ACTIVE con salida hoy)
        List<ReservationResponse> checkOuts = reservationRepository
                .findByCheckOutDateAndStatusOrderByCheckOutDateAsc(today, ReservationStatus.ACTIVE)
                .stream()
                .map(ReservationResponse::fromEntity)
                .collect(Collectors.toList());
        
        return new TodayReservationsResponse(checkIns, checkOuts);
    }

    /**
     * Realiza el check-in de una reserva.
     * Validación estricta: solo permite check-in en la fecha programada.
     * Historia 4.2: Realizar check-in del huésped
     *
     * @param reservationId ID de la reserva
     * @throws com.sofka.hotel_booking_api.domain.exception.ReservationNotFoundException si la reserva no existe
     * @throws IllegalStateException si la reserva no está en estado CONFIRMED o si la fecha no es hoy o si la habitación está ocupada
     */
    @Transactional
    public void checkIn(Long reservationId) {
        // 1. Buscar la reserva
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new com.sofka.hotel_booking_api.domain.exception.ReservationNotFoundException(
                        "Reservation not found with id: " + reservationId));

        // 2. Validación estricta: check-in solo en la fecha programada
        LocalDate today = LocalDate.now();
        if (!reservation.getCheckInDate().equals(today)) {
            throw new IllegalStateException(
                    "Check-in can only be performed on the check-in date. Expected: " 
                    + reservation.getCheckInDate() + ", but today is: " + today);
        }

        // 3. Verificar que la habitación no esté ocupada por otra reserva activa
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                reservation.getRoom(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate()
        );

        // Filtrar solo reservas ACTIVE que no sean esta misma reserva
        boolean roomOccupied = overlappingReservations.stream()
                .filter(r -> !r.getId().equals(reservationId))
                .anyMatch(r -> r.getStatus() == ReservationStatus.ACTIVE);

        if (roomOccupied) {
            throw new IllegalStateException(
                    "Room is occupied by another active reservation. Cannot perform check-in.");
        }

        // 4. Realizar check-in (valida que esté en estado CONFIRMED)
        reservation.checkIn();

        // 5. Marcar habitación como no disponible
        Room room = reservation.getRoom();
        room.setIsAvailable(false);

        // 6. Guardar cambios
        reservationRepository.save(reservation);
        roomRepository.save(room);
    }
}
