import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { RoomCard } from './RoomCard';
import type { Room } from '../../services/types/room.types';

describe('RoomCard', () => {
  const mockRoom: Room = {
    id: 1,
    roomNumber: '101',
    roomType: 'STANDARD',
    capacity: 2,
    pricePerNight: 100.5,
    isAvailable: true,
  };

  it('debería renderizar información básica de la habitación', () => {
    render(<RoomCard room={mockRoom} />);

    expect(screen.getByText(/Habitación 101/i)).toBeInTheDocument();
    expect(screen.getByText('Estándar')).toBeInTheDocument();
    expect(screen.getByText(/2 personas/i)).toBeInTheDocument();
    expect(screen.getByText(/\$100\.50/i)).toBeInTheDocument();
  });

  it('debería mostrar estado "Disponible" cuando isAvailable es true', () => {
    render(<RoomCard room={mockRoom} />);

    const badge = screen.getByText('Disponible');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass('bg-green-100', 'text-green-800');
  });

  it('debería mostrar estado "Ocupada" cuando isAvailable es false', () => {
    const occupiedRoom = { ...mockRoom, isAvailable: false };
    render(<RoomCard room={occupiedRoom} />);

    const badge = screen.getByText('Ocupada');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass('bg-red-100', 'text-red-800');
  });

  it('debería renderizar tipo de habitación SUPERIOR correctamente', () => {
    const superiorRoom: Room = {
      ...mockRoom,
      roomType: 'SUPERIOR',
    };
    render(<RoomCard room={superiorRoom} />);

    expect(screen.getByText('Superior')).toBeInTheDocument();
  });

  it('debería renderizar tipo de habitación SUITE correctamente', () => {
    const suiteRoom: Room = {
      ...mockRoom,
      roomType: 'SUITE',
    };
    render(<RoomCard room={suiteRoom} />);

    expect(screen.getByText('Suite')).toBeInTheDocument();
  });

  it('debería mostrar capacidad en singular cuando es 1 persona', () => {
    const singleRoom = { ...mockRoom, capacity: 1 };
    render(<RoomCard room={singleRoom} />);

    expect(screen.getByText(/1 persona/i)).toBeInTheDocument();
  });

  it('debería formatear precio con 2 decimales', () => {
    const roomWithDecimals = { ...mockRoom, pricePerNight: 99.99 };
    render(<RoomCard room={roomWithDecimals} />);

    expect(screen.getByText(/\$99\.99/i)).toBeInTheDocument();
  });

  it('debería mostrar el ID de la habitación en el footer', () => {
    render(<RoomCard room={mockRoom} />);

    expect(screen.getByText(`ID: ${mockRoom.id}`)).toBeInTheDocument();
  });
});
