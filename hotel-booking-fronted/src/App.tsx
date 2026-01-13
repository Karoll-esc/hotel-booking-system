import { useState } from 'react';
import { PlusIcon } from '@heroicons/react/24/outline';
import { RoomForm } from './components/rooms/RoomForm';
import { RoomList } from './components/rooms/RoomList';
import { useRooms } from './hooks/useRooms';
import type { RoomFormData } from './utils/validation';
import './App.css';

function App() {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const { rooms, isLoading, isError, error, refetch, createRoom, isCreating } = useRooms();

  const handleCreateRoom = (data: RoomFormData) => {
    createRoom(data, {
      onSuccess: () => {
        setIsFormOpen(false);
      },
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow">
        <div className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold tracking-tight text-gray-900">
                Sistema de Gestión Hotelera
              </h1>
              <p className="mt-1 text-sm text-gray-500">
                Historia de Usuario 2.1 - Registro de Habitaciones
              </p>
            </div>
            <button
              onClick={() => setIsFormOpen(true)}
              className="inline-flex items-center rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
            >
              <PlusIcon className="h-5 w-5 mr-2" />
              Nueva Habitación
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <RoomList
          rooms={rooms}
          isLoading={isLoading}
          isError={isError}
          error={error}
          onRefresh={refetch}
        />
      </main>

      {/* Modal de Formulario */}
      <RoomForm
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSubmit={handleCreateRoom}
        isSubmitting={isCreating}
      />
    </div>
  );
}

export default App;

