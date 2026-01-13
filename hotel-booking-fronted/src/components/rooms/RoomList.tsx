import { ArrowPathIcon, ExclamationTriangleIcon } from '@heroicons/react/24/outline';
import { RoomCard } from './RoomCard';
import type { Room } from '../../services/types/room.types';

interface RoomListProps {
  rooms: Room[];
  isLoading: boolean;
  isError: boolean;
  error: Error | null;
  onRefresh: () => void;
}

/**
 * Lista de habitaciones con estados de carga y error
 * 
 * Maneja:
 * - Estado de carga (loading spinner)
 * - Estado de error (mensaje + botón retry)
 * - Lista vacía (empty state)
 * - Grid responsivo de tarjetas
 */
export function RoomList({ rooms, isLoading, isError, error, onRefresh }: RoomListProps) {
  // Estado: Cargando
  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <ArrowPathIcon className="h-12 w-12 text-indigo-600 animate-spin" />
        <p className="mt-4 text-sm text-gray-600">Cargando habitaciones...</p>
      </div>
    );
  }

  // Estado: Error
  if (isError) {
    return (
      <div className="flex flex-col items-center justify-center py-12 px-4">
        <div className="rounded-full bg-red-100 p-3">
          <ExclamationTriangleIcon className="h-8 w-8 text-red-600" />
        </div>
        <h3 className="mt-4 text-lg font-medium text-gray-900">Error al cargar habitaciones</h3>
        <p className="mt-2 text-sm text-gray-500 text-center max-w-md">
          {error?.message || 'Ocurrió un error inesperado'}
        </p>
        <button
          onClick={onRefresh}
          className="mt-4 inline-flex items-center rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
        >
          <ArrowPathIcon className="h-4 w-4 mr-2" />
          Reintentar
        </button>
      </div>
    );
  }

  // Estado: Lista vacía
  if (rooms.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 px-4">
        <div className="rounded-full bg-gray-100 p-3">
          <svg
            className="h-8 w-8 text-gray-400"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
            />
          </svg>
        </div>
        <h3 className="mt-4 text-lg font-medium text-gray-900">No hay habitaciones registradas</h3>
        <p className="mt-2 text-sm text-gray-500 text-center max-w-md">
          Comienza registrando tu primera habitación haciendo clic en "Nueva Habitación"
        </p>
      </div>
    );
  }

  // Estado: Lista con datos
  return (
    <div>
      {/* Header con contador */}
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h2 className="text-lg font-medium text-gray-900">
            Habitaciones Registradas
          </h2>
          <p className="text-sm text-gray-500">
            {rooms.length} {rooms.length === 1 ? 'habitación' : 'habitaciones'} encontradas
          </p>
        </div>
        <button
          onClick={onRefresh}
          className="inline-flex items-center text-sm text-indigo-600 hover:text-indigo-700"
        >
          <ArrowPathIcon className="h-4 w-4 mr-1" />
          Actualizar
        </button>
      </div>

      {/* Grid de tarjetas */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {rooms.map((room) => (
          <RoomCard key={room.id} room={room} />
        ))}
      </div>
    </div>
  );
}
