package com.sofka.hotel_booking_api.infrastructure.exception;

import com.sofka.hotel_booking_api.domain.exception.DuplicateRoomNumberException;
import com.sofka.hotel_booking_api.domain.exception.InvalidDateRangeException;
import com.sofka.hotel_booking_api.domain.exception.ReservationNotFoundException;
import com.sofka.hotel_booking_api.domain.exception.RoomNotFoundException;
import com.sofka.hotel_booking_api.infrastructure.constants.ValidationMessages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la API REST.
 * 
 * <p>Intercepta excepciones lanzadas por los controladores y las convierte en 
 * respuestas HTTP apropiadas con formato consistente.</p>
 * 
 * <p>Tipos de excepciones manejadas:</p>
 * <ul>
 *   <li>{@link DuplicateRoomNumberException} → 409 CONFLICT</li>
 *   <li>{@link RoomNotFoundException} → 404 NOT FOUND</li>
 *   <li>{@link InvalidDateRangeException} → 400 BAD REQUEST</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 BAD REQUEST</li>
 * </ul>
 * 
 * @author Sistema Hotel Booking
 * @version 1.0
 * @since 2026-01-07
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones cuando se intenta registrar una habitación con número duplicado.
     * 
     * @param ex la excepción de número duplicado
     * @return respuesta HTTP 409 CONFLICT con detalles del error
     */
    @ExceptionHandler(DuplicateRoomNumberException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRoomNumber(DuplicateRoomNumberException ex) {
        ErrorResponse error = buildErrorResponse(
            HttpStatus.CONFLICT,
            ValidationMessages.DUPLICATE_ROOM_NUMBER_TITLE,
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones cuando no se encuentra una habitación por ID.
     * 
     * @param ex la excepción de habitación no encontrada
     * @return respuesta HTTP 404 NOT FOUND con detalles del error
     */
    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoomNotFound(RoomNotFoundException ex) {
        ErrorResponse error = buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "Habitación no encontrada",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja excepciones cuando se proporciona un rango de fechas inválido.
     * 
     * @param ex la excepción de rango de fechas inválido
     * @return respuesta HTTP 400 BAD REQUEST con detalles del error
     */
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRange(InvalidDateRangeException ex) {
        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Rango de fechas inválido",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones cuando no se encuentra una reserva por ID.
     * 
     * @param ex la excepción de reserva no encontrada
     * @return respuesta HTTP 404 NOT FOUND con detalles del error
     */
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFound(ReservationNotFoundException ex) {
        ErrorResponse error = buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "Reserva no encontrada",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Maneja excepciones de estado inválido (ej: intentar confirmar pago de reserva ya confirmada).
     * 
     * @param ex la excepción de estado ilegal
     * @return respuesta HTTP 400 BAD REQUEST con detalles del error
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Operación no permitida",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja excepciones de argumentos inválidos (ej: monto incorrecto, método de pago inválido).
     * 
     * @param ex la excepción de argumento ilegal
     * @return respuesta HTTP 400 BAD REQUEST con detalles del error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Datos inválidos",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja errores de validación de Bean Validation (@Valid).
     * Extrae todos los errores de validación y los agrupa por campo.
     * 
     * @param ex la excepción de validación
     * @return respuesta HTTP 400 BAD REQUEST con detalles de validación por campo
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = extractValidationErrors(ex);
        
        ValidationErrorResponse errorResponse = buildValidationErrorResponse(
            HttpStatus.BAD_REQUEST,
            ValidationMessages.VALIDATION_ERROR_TITLE,
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Extrae los errores de validación de la excepción.
     * 
     * @param ex la excepción de validación
     * @return mapa con nombre de campo y mensaje de error
     */
    private Map<String, String> extractValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
    
    /**
     * Construye una respuesta de error simple.
     * 
     * @param status el estado HTTP
     * @param error el título del error
     * @param message el mensaje descriptivo
     * @return objeto ErrorResponse
     */
    private ErrorResponse buildErrorResponse(HttpStatus status, String error, String message) {
        return new ErrorResponse(
            status.value(),
            error,
            message,
            LocalDateTime.now()
        );
    }
    
    /**
     * Construye una respuesta de error de validación.
     * 
     * @param status el estado HTTP
     * @param error el título del error
     * @param validationErrors mapa de errores por campo
     * @return objeto ValidationErrorResponse
     */
    private ValidationErrorResponse buildValidationErrorResponse(
            HttpStatus status, String error, Map<String, String> validationErrors) {
        return new ValidationErrorResponse(
            status.value(),
            error,
            validationErrors,
            LocalDateTime.now()
        );
    }

    /**
     * DTO para respuestas de error simples
     */
    public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
    ) {}

    /**
     * DTO para respuestas de error de validación
     */
    public record ValidationErrorResponse(
        int status,
        String error,
        Map<String, String> validationErrors,
        LocalDateTime timestamp
    ) {}
}
