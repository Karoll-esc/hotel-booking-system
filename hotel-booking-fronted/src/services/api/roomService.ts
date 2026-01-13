import axios from 'axios';
import type { Room, CreateRoomRequest, ApiError, ValidationError } from '../types/room.types';

/**
 * URL base de la API
 * Usa variable de entorno o fallback a localhost
 */
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

/**
 * Instancia configurada de axios para la API
 */
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 segundos
});

/**
 * Servicio para interactuar con el API de habitaciones
 */
export const roomService = {
  /**
   * Registra una nueva habitación en el sistema
   * 
   * @param data - Datos de la habitación a crear
   * @returns Promise con la habitación creada
   * @throws Error con mensaje descriptivo en caso de fallo
   * 
   * @example
   * ```ts
   * const room = await roomService.createRoom({
   *   roomNumber: "301",
   *   roomType: RoomType.SUITE,
   *   capacity: 4,
   *   pricePerNight: 250.00
   * });
   * ```
   */
  async createRoom(data: CreateRoomRequest): Promise<Room> {
    try {
      const response = await axiosInstance.post<Room>('/rooms', data);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const apiError = error.response?.data as ApiError | ValidationError;
        
        // Si es un error de validación, extraer todos los mensajes
        if ('validationErrors' in apiError) {
          const errors = Object.values(apiError.validationErrors).join(', ');
          throw new Error(errors);
        }
        
        // Si es un error de negocio (ej: número duplicado)
        throw new Error(apiError?.message || 'Error al registrar habitación');
      }
      
      // Error genérico
      throw new Error('Error de conexión con el servidor');
    }
  },

  /**
   * Obtiene todas las habitaciones registradas
   * 
   * @returns Promise con array de habitaciones
   * @throws Error en caso de fallo en la petición
   */
  async getAllRooms(): Promise<Room[]> {
    try {
      const response = await axiosInstance.get<Room[]>('/rooms');
      return response.data;
    } catch (error) {
      throw new Error('Error al obtener las habitaciones');
    }
  },

  /**
   * Obtiene una habitación específica por su ID
   * 
   * @param id - ID de la habitación
   * @returns Promise con la habitación encontrada
   * @throws Error si no se encuentra o hay error en la petición
   */
  async getRoomById(id: number): Promise<Room> {
    try {
      const response = await axiosInstance.get<Room>(`/rooms/${id}`);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        throw new Error(`No se encontró la habitación con ID ${id}`);
      }
      throw new Error('Error al obtener la habitación');
    }
  },

  /**
   * Busca una habitación por su número
   * 
   * @param roomNumber - Número de la habitación
   * @returns Promise con la habitación encontrada o null
   */
  async getRoomByNumber(roomNumber: string): Promise<Room | null> {
    try {
      const response = await axiosInstance.get<Room>(`/rooms/number/${roomNumber}`);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        return null;
      }
      throw new Error('Error al buscar la habitación');
    }
  },
};
