package com.sofka.hotel_booking_api.infrastructure.constants;

/**
 * Constantes para mensajes de validación y error de la aplicación.
 * Centralizados para facilitar mantenimiento y consistencia.
 * 
 * @author Sistema Hotel Booking
 * @version 1.0
 * @since 2026-01-07
 */
public final class ValidationMessages {

    // Constructor privado para evitar instanciación
    private ValidationMessages() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no debe ser instanciada");
    }

    // ==================== Room Validation Messages ====================
    
    /**
     * Mensaje cuando el número de habitación es nulo
     */
    public static final String ROOM_NUMBER_REQUIRED = "El número de habitación es obligatorio";
    
    /**
     * Mensaje cuando el número de habitación está vacío
     */
    public static final String ROOM_NUMBER_NOT_BLANK = "El número de habitación no puede estar vacío";
    
    /**
     * Mensaje cuando el tipo de habitación es nulo
     */
    public static final String ROOM_TYPE_REQUIRED = "El tipo de habitación es obligatorio";
    
    /**
     * Mensaje cuando la capacidad es nula
     */
    public static final String CAPACITY_REQUIRED = "La capacidad es obligatoria";
    
    /**
     * Mensaje cuando la capacidad es menor al mínimo
     */
    public static final String CAPACITY_MIN = "La capacidad debe ser al menos 1 persona";
    
    /**
     * Mensaje cuando la capacidad excede el máximo
     */
    public static final String CAPACITY_MAX = "La capacidad no puede exceder 10 personas";
    
    /**
     * Mensaje cuando el precio es nulo
     */
    public static final String PRICE_REQUIRED = "El precio por noche es obligatorio";
    
    /**
     * Mensaje cuando el precio es menor o igual a cero
     */
    public static final String PRICE_MIN = "El precio debe ser mayor a 0";

    // ==================== Error Response Messages ====================
    
    /**
     * Título de error para número de habitación duplicado
     */
    public static final String DUPLICATE_ROOM_NUMBER_TITLE = "Número de habitación duplicado";
    
    /**
     * Título de error para validaciones fallidas
     */
    public static final String VALIDATION_ERROR_TITLE = "Errores de validación";
    
    /**
     * Formato del mensaje de error para habitación duplicada
     */
    public static final String DUPLICATE_ROOM_NUMBER_MESSAGE = "Ya existe una habitación registrada con el número '%s'";

    // ==================== Domain Constraints ====================
    
    /**
     * Capacidad mínima permitida para una habitación (RN-006)
     */
    public static final int MIN_CAPACITY = 1;
    
    /**
     * Capacidad máxima permitida para una habitación (RN-006)
     */
    public static final int MAX_CAPACITY = 10;
    
    /**
     * Precio mínimo permitido para una habitación (RN-006)
     */
    public static final String MIN_PRICE = "0.01";
}
