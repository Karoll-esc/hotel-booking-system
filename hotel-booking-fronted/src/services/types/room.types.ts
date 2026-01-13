/**
 * Tipos de habitación disponibles en el hotel
 * Usando const object para compatibilidad con verbatimModuleSyntax
 */
export const RoomType = {
  STANDARD: 'STANDARD',
  SUPERIOR: 'SUPERIOR',
  SUITE: 'SUITE'
} as const;

/**
 * Type helper para RoomType
 */
export type RoomType = typeof RoomType[keyof typeof RoomType];

/**
 * Interfaz que representa una habitación del hotel
 */
export interface Room {
  /** ID único de la habitación */
  id: number;
  
  /** Número identificador de la habitación (ej: "101", "201A") */
  roomNumber: string;
  
  /** Tipo de habitación */
  roomType: RoomType;
  
  /** Capacidad máxima de personas */
  capacity: number;
  
  /** Precio por noche en USD */
  pricePerNight: number;
  
  /** Indica si la habitación está disponible para reserva */
  isAvailable: boolean;
}

/**
 * DTO para crear una nueva habitación
 */
export interface CreateRoomRequest {
  roomNumber: string;
  roomType: RoomType;
  capacity: number;
  pricePerNight: number;
}

/**
 * Respuesta de error con detalles de validación por campo
 */
export interface ValidationError {
  status: number;
  error: string;
  validationErrors: Record<string, string>;
  timestamp: string;
}

/**
 * Respuesta de error genérica de la API
 */
export interface ApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
}
