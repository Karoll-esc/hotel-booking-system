import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { setupServer } from 'msw/node';
import { http, HttpResponse } from 'msw';
import { useRooms } from './useRooms';
import type { Room, CreateRoomRequest } from '../services/types/room.types';

// Mock Server Setup
const mockRooms: Room[] = [
  {
    id: 1,
    roomNumber: '101',
    roomType: 'STANDARD',
    capacity: 2,
    pricePerNight: 100.0,
    isAvailable: true,
  },
  {
    id: 2,
    roomNumber: '201',
    roomType: 'SUPERIOR',
    capacity: 3,
    pricePerNight: 150.0,
    isAvailable: false,
  },
];

const server = setupServer(
  http.get('http://localhost:8080/api/rooms', () => {
    return HttpResponse.json(mockRooms);
  }),

  http.post('http://localhost:8080/api/rooms', async ({ request }) => {
    const body = await request.json() as CreateRoomRequest;
    const newRoom: Room = {
      id: 3,
      ...body,
      isAvailable: true,
    };
    return HttpResponse.json(newRoom, { status: 201 });
  })
);

beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

// Test Wrapper
function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
      },
      mutations: {
        retry: false,
      },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
}

describe('useRooms', () => {
  it('debería obtener la lista de habitaciones exitosamente', async () => {
    const { result } = renderHook(() => useRooms(), {
      wrapper: createWrapper(),
    });

    // Estado inicial
    expect(result.current.isLoading).toBe(true);
    expect(result.current.rooms).toEqual([]);

    // Esperar a que cargue
    await waitFor(() => {
      expect(result.current.isLoading).toBe(false);
    });

    // Verificar datos
    expect(result.current.rooms).toHaveLength(2);
    expect(result.current.rooms[0].roomNumber).toBe('101');
    expect(result.current.isError).toBe(false);
  });

  it('debería crear una nueva habitación exitosamente', async () => {
    const { result } = renderHook(() => useRooms(), {
      wrapper: createWrapper(),
    });

    // Esperar carga inicial
    await waitFor(() => expect(result.current.isLoading).toBe(false));

    const newRoomData: CreateRoomRequest = {
      roomNumber: '301',
      roomType: 'SUITE',
      capacity: 4,
      pricePerNight: 250.0,
    };

    // Ejecutar mutation
    result.current.createRoom(newRoomData);

    // Verificar estado de creación
    await waitFor(() => {
      expect(result.current.isCreating).toBe(false);
    });

    // Verificar que se agregó al cache (optimistic update)
    await waitFor(() => {
      expect(result.current.rooms).toHaveLength(3);
    });
  });

  it('debería manejar errores al obtener habitaciones', async () => {
    // Simular error del servidor
    server.use(
      http.get('http://localhost:8080/api/rooms', () => {
        return HttpResponse.json(
          { message: 'Error del servidor' },
          { status: 500 }
        );
      })
    );

    const { result } = renderHook(() => useRooms(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
      expect(result.current.error).toBeDefined();
    });
  });

  it('debería manejar habitación duplicada al crear', async () => {
    // Simular error 409 Conflict
    server.use(
      http.post('http://localhost:8080/api/rooms', () => {
        return HttpResponse.json(
          { message: 'Ya existe una habitación con este número' },
          { status: 409 }
        );
      })
    );

    const { result } = renderHook(() => useRooms(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    const duplicateRoom: CreateRoomRequest = {
      roomNumber: '101',
      roomType: 'STANDARD',
      capacity: 2,
      pricePerNight: 100.0,
    };

    result.current.createRoom(duplicateRoom);

    await waitFor(() => {
      expect(result.current.createError).toBeDefined();
    });
  });
});
