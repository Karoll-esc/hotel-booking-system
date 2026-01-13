import { UserGroupIcon, CurrencyDollarIcon, HomeIcon } from '@heroicons/react/24/outline';
import type { Room } from '../../services/types/room.types';
import { ROOM_TYPE_LABELS } from '../../utils/constants';
import { formatPrice } from '../../utils/validation';

interface RoomCardProps {
  room: Room;
}

/**
 * Tarjeta para mostrar información de una habitación
 * 
 * Muestra:
 * - Número de habitación
 * - Tipo y descripción
 * - Capacidad
 * - Precio por noche
 * - Estado de disponibilidad
 */
export function RoomCard({ room }: RoomCardProps) {
  return (
    <div className="bg-white rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 overflow-hidden">
      {/* Header con número y estado */}
      <div className="bg-gradient-to-r from-indigo-500 to-indigo-600 px-4 py-3 flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <HomeIcon className="h-5 w-5 text-white" />
          <h3 className="text-lg font-semibold text-white">
            Habitación {room.roomNumber}
          </h3>
        </div>
        <span
          className={`px-2 py-1 text-xs font-medium rounded-full ${
            room.isAvailable
              ? 'bg-green-100 text-green-800'
              : 'bg-red-100 text-red-800'
          }`}
        >
          {room.isAvailable ? 'Disponible' : 'Ocupada'}
        </span>
      </div>

      {/* Contenido */}
      <div className="p-4 space-y-3">
        {/* Tipo de Habitación */}
        <div>
          <p className="text-sm text-gray-500">Tipo</p>
          <p className="text-base font-medium text-gray-900">
            {ROOM_TYPE_LABELS[room.roomType]}
          </p>
        </div>

        {/* Capacidad */}
        <div className="flex items-center space-x-2 text-gray-600">
          <UserGroupIcon className="h-5 w-5" />
          <span className="text-sm">
            {room.capacity} {room.capacity === 1 ? 'persona' : 'personas'}
          </span>
        </div>

        {/* Precio */}
        <div className="flex items-center space-x-2 text-gray-600">
          <CurrencyDollarIcon className="h-5 w-5" />
          <span className="text-sm">
            <span className="text-lg font-bold text-indigo-600">
              ${formatPrice(room.pricePerNight)}
            </span>
            <span className="text-gray-500"> / noche</span>
          </span>
        </div>
      </div>

      {/* Footer con ID (debug) */}
      <div className="bg-gray-50 px-4 py-2 text-xs text-gray-400">
        ID: {room.id}
      </div>
    </div>
  );
}
