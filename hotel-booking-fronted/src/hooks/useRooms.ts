import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import type { CreateRoomRequest, Room } from '../services/types/room.types';
import { roomService } from '../services/api/roomService';
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from '../utils/constants';

/**
 * Query keys para React Query
 */
const QUERY_KEYS = {
  rooms: ['rooms'] as const,
  room: (id: number) => ['rooms', id] as const,
  roomByNumber: (number: string) => ['rooms', 'number', number] as const,
};

/**
 * Custom hook para gestionar habitaciones con React Query
 * 
 * Proporciona queries y mutations para:
 * - Obtener todas las habitaciones
 * - Obtener habitación por ID
 * - Crear nueva habitación
 * 
 * Incluye manejo automático de:
 * - Loading states
 * - Error handling
 * - Cache invalidation
 * - Notificaciones toast
 */
export function useRooms() {
  const queryClient = useQueryClient();

  /**
   * Query: Obtener todas las habitaciones
   */
  const roomsQuery = useQuery({
    queryKey: QUERY_KEYS.rooms,
    queryFn: () => roomService.getAllRooms(),
    staleTime: 5 * 60 * 1000, // 5 minutos
    gcTime: 10 * 60 * 1000, // 10 minutos (antes cacheTime)
  });

  /**
   * Mutation: Crear nueva habitación
   */
  const createRoomMutation = useMutation({
    mutationFn: (data: CreateRoomRequest) => roomService.createRoom(data),
    onSuccess: (newRoom) => {
      // Invalidar cache de rooms para refrescar la lista
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.rooms });
      
      // Agregar la nueva habitación al cache optimísticamente
      queryClient.setQueryData<Room[]>(QUERY_KEYS.rooms, (old = []) => [
        newRoom,
        ...old,
      ]);

      // Notificación de éxito
      toast.success(SUCCESS_MESSAGES.ROOM_CREATED);
    },
    onError: (error: any) => {
      // Notificación de error
      const message = error.message || ERROR_MESSAGES.UNKNOWN_ERROR;
      toast.error(message);
    },
  });

  return {
    // Queries
    rooms: roomsQuery.data ?? [],
    isLoading: roomsQuery.isLoading,
    isError: roomsQuery.isError,
    error: roomsQuery.error,
    refetch: roomsQuery.refetch,

    // Mutations
    createRoom: createRoomMutation.mutate,
    isCreating: createRoomMutation.isPending,
    createError: createRoomMutation.error,
  };
}

/**
 * Hook para obtener una habitación específica por ID
 */
export function useRoom(id: number) {
  return useQuery({
    queryKey: QUERY_KEYS.room(id),
    queryFn: () => roomService.getRoomById(id),
    enabled: !!id, // Solo ejecuta si hay ID
    staleTime: 5 * 60 * 1000,
  });
}

/**
 * Hook para obtener una habitación por número
 */
export function useRoomByNumber(roomNumber: string) {
  return useQuery({
    queryKey: QUERY_KEYS.roomByNumber(roomNumber),
    queryFn: () => roomService.getRoomByNumber(roomNumber),
    enabled: !!roomNumber && roomNumber.length > 0,
    staleTime: 5 * 60 * 1000,
  });
}
