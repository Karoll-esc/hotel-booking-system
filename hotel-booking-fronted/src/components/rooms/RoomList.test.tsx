import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RoomList } from './RoomList';
import type { Room } from '../../services/types/room.types';

describe('RoomList', () => {
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

  const defaultProps = {
    rooms: mockRooms,
    isLoading: false,
    isError: false,
    error: null,
    onRefresh: vi.fn(),
  };

  it('debería renderizar la lista de habitaciones', () => {
    render(<RoomList {...defaultProps} />);

    expect(screen.getByText(/Habitaciones Registradas/i)).toBeInTheDocument();
    expect(screen.getByText(/2 habitaciones encontradas/i)).toBeInTheDocument();
    expect(screen.getByText(/Habitación 101/i)).toBeInTheDocument();
    expect(screen.getByText(/Habitación 201/i)).toBeInTheDocument();
  });

  it('debería mostrar estado de carga', () => {
    render(<RoomList {...defaultProps} isLoading={true} rooms={[]} />);

    expect(screen.getByText(/Cargando habitaciones/i)).toBeInTheDocument();
  });

  it('debería mostrar estado de error', () => {
    const errorMessage = 'Error de conexión';
    render(
      <RoomList
        {...defaultProps}
        isError={true}
        error={new Error(errorMessage)}
        rooms={[]}
      />
    );

    expect(screen.getByText(/Error al cargar habitaciones/i)).toBeInTheDocument();
    expect(screen.getByText(errorMessage)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Reintentar/i })).toBeInTheDocument();
  });

  it('debería mostrar mensaje cuando no hay habitaciones', () => {
    render(<RoomList {...defaultProps} rooms={[]} />);

    expect(screen.getByText(/No hay habitaciones registradas/i)).toBeInTheDocument();
    expect(
      screen.getByText(/Comienza registrando tu primera habitación/i)
    ).toBeInTheDocument();
  });

  it('debería llamar onRefresh cuando se hace clic en Actualizar', async () => {
    const onRefresh = vi.fn();
    const user = userEvent.setup();

    render(<RoomList {...defaultProps} onRefresh={onRefresh} />);

    const refreshButton = screen.getByRole('button', { name: /Actualizar/i });
    await user.click(refreshButton);

    expect(onRefresh).toHaveBeenCalledTimes(1);
  });

  it('debería llamar onRefresh cuando se hace clic en Reintentar', async () => {
    const onRefresh = vi.fn();
    const user = userEvent.setup();

    render(
      <RoomList
        {...defaultProps}
        isError={true}
        error={new Error('Error')}
        rooms={[]}
        onRefresh={onRefresh}
      />
    );

    const retryButton = screen.getByRole('button', { name: /Reintentar/i });
    await user.click(retryButton);

    expect(onRefresh).toHaveBeenCalledTimes(1);
  });

  it('debería mostrar contador correcto con 1 habitación', () => {
    render(<RoomList {...defaultProps} rooms={[mockRooms[0]]} />);

    expect(screen.getByText(/1 habitación encontrada/i)).toBeInTheDocument();
  });

  it('debería renderizar grid con las habitaciones', () => {
    const { container } = render(<RoomList {...defaultProps} />);

    const grid = container.querySelector('.grid');
    expect(grid).toBeInTheDocument();
    expect(grid?.children).toHaveLength(2);
  });
});
