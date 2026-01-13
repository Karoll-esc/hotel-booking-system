import { z } from 'zod';
import { RoomType as RoomTypeValues } from '../services/types/room.types';

/**
 * Schema de validación para el formulario de registro de habitaciones
 * Usa Zod para validación en el cliente antes de enviar al servidor
 * 
 * Las validaciones coinciden con las del backend (RN-006):
 * - Número de habitación: obligatorio, no vacío
 * - Tipo: obligatorio, debe ser STANDARD, SUPERIOR o SUITE
 * - Capacidad: obligatoria, entre 1 y 10 personas
 * - Precio: obligatorio, mayor a 0
 */
export const roomFormSchema = z.object({
  roomNumber: z
    .string({ message: 'El número de habitación es obligatorio' })
    .min(1, 'El número de habitación no puede estar vacío')
    .max(10, 'El número de habitación no puede exceder 10 caracteres')
    .trim()
    .refine(
      (val) => val.length > 0,
      'El número de habitación no puede estar vacío'
    ),
  
  roomType: z.enum(
    [RoomTypeValues.STANDARD, RoomTypeValues.SUPERIOR, RoomTypeValues.SUITE],
    { message: 'El tipo de habitación es obligatorio' }
  ),
  
  capacity: z
    .number({ message: 'La capacidad es obligatoria' })
    .int('La capacidad debe ser un número entero')
    .min(1, 'La capacidad debe ser al menos 1 persona')
    .max(10, 'La capacidad no puede exceder 10 personas'),
  
  pricePerNight: z
    .number({ message: 'El precio por noche es obligatorio' })
    .positive('El precio debe ser mayor a 0')
    .multipleOf(0.01, 'El precio debe tener máximo 2 decimales')
    .refine(
      (val) => val > 0,
      'El precio debe ser mayor a 0'
    ),
});

/**
 * Tipo inferido del schema de validación
 * Útil para type-safety en formularios
 */
export type RoomFormData = z.infer<typeof roomFormSchema>;

/**
 * Validación adicional: verifica que el número de habitación no contenga caracteres especiales peligrosos
 */
export const sanitizeRoomNumber = (roomNumber: string): string => {
  return roomNumber.replace(/[<>\"']/g, '').trim();
};

/**
 * Formatea el precio para mostrar con 2 decimales
 */
export const formatPrice = (price: number): string => {
  return price.toFixed(2);
};

/**
 * Valida que el precio tenga máximo 2 decimales
 */
export const validatePriceDecimals = (price: number): boolean => {
  const decimals = (price.toString().split('.')[1] || '').length;
  return decimals <= 2;
};
