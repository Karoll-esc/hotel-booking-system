import type { RoomType } from '../services/types/room.types';
import { RoomType as RoomTypeValues } from '../services/types/room.types';

/**
 * Mapeo de tipos de habitación a etiquetas legibles en español
 */
export const ROOM_TYPE_LABELS: Record<RoomType, string> = {
  [RoomTypeValues.STANDARD]: 'Estándar',
  [RoomTypeValues.SUPERIOR]: 'Superior',
  [RoomTypeValues.SUITE]: 'Suite',
};

/**
 * Descripciones detalladas de cada tipo de habitación
 */
export const ROOM_TYPE_DESCRIPTIONS: Record<RoomType, string> = {
  [RoomTypeValues.STANDARD]: 'Habitación básica con comodidades estándar',
  [RoomTypeValues.SUPERIOR]: 'Habitación con amenidades adicionales y mejor vista',
  [RoomTypeValues.SUITE]: 'Suite de lujo con sala de estar separada',
};

/**
 * Opciones de tipo de habitación para selectores
 */
export const ROOM_TYPE_OPTIONS = Object.entries(ROOM_TYPE_LABELS).map(([value, label]) => ({
  value: value as RoomType,
  label,
  description: ROOM_TYPE_DESCRIPTIONS[value as RoomType],
}));

/**
 * Opciones de capacidad (1-10 personas)
 * Según RN-006: capacidad entre 1 y 10
 */
export const CAPACITY_OPTIONS = Array.from({ length: 10 }, (_, i) => ({
  value: i + 1,
  label: `${i + 1} ${i + 1 === 1 ? 'persona' : 'personas'}`,
}));

/**
 * Rangos de precio sugeridos por tipo de habitación
 */
export const SUGGESTED_PRICE_RANGES: Record<RoomType, { min: number; max: number }> = {
  [RoomTypeValues.STANDARD]: { min: 50, max: 150 },
  [RoomTypeValues.SUPERIOR]: { min: 100, max: 250 },
  [RoomTypeValues.SUITE]: { min: 200, max: 500 },
};

/**
 * Constantes de validación (deben coincidir con el backend)
 */
export const VALIDATION_CONSTANTS = {
  MIN_CAPACITY: 1,
  MAX_CAPACITY: 10,
  MIN_PRICE: 0.01,
  MAX_ROOM_NUMBER_LENGTH: 10,
} as const;

/**
 * Mensajes de error personalizados
 */
export const ERROR_MESSAGES = {
  DUPLICATE_ROOM: 'Ya existe una habitación con este número',
  NETWORK_ERROR: 'Error de conexión. Verifica tu conexión a internet',
  SERVER_ERROR: 'Error del servidor. Intenta nuevamente más tarde',
  VALIDATION_ERROR: 'Por favor corrige los errores en el formulario',
  UNKNOWN_ERROR: 'Ocurrió un error inesperado',
} as const;

/**
 * Mensajes de éxito
 */
export const SUCCESS_MESSAGES = {
  ROOM_CREATED: 'Habitación registrada exitosamente',
  ROOM_UPDATED: 'Habitación actualizada exitosamente',
  ROOM_DELETED: 'Habitación eliminada exitosamente',
} as const;

/**
 * Configuración de la aplicación
 */
export const APP_CONFIG = {
  API_TIMEOUT: 10000, // 10 segundos
  QUERY_STALE_TIME: 5 * 60 * 1000, // 5 minutos
  QUERY_CACHE_TIME: 10 * 60 * 1000, // 10 minutos
} as const;
