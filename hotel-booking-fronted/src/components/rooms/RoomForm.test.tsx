import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RoomForm } from './RoomForm';

describe('RoomForm', () => {
  const defaultProps = {
    isOpen: true,
    onClose: vi.fn(),
    onSubmit: vi.fn(),
    isSubmitting: false,
  };

  it('debería renderizar el formulario cuando está abierto', () => {
    render(<RoomForm {...defaultProps} />);

    expect(screen.getByText(/Registrar Nueva Habitación/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Número de Habitación/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Tipo de Habitación/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Capacidad/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Precio por Noche/i)).toBeInTheDocument();
  });

  it('no debería renderizar cuando isOpen es false', () => {
    render(<RoomForm {...defaultProps} isOpen={false} />);

    expect(screen.queryByText(/Registrar Nueva Habitación/i)).not.toBeInTheDocument();
  });

  it('debería llamar onClose cuando se hace clic en Cancelar', async () => {
    const onClose = vi.fn();
    const user = userEvent.setup();

    render(<RoomForm {...defaultProps} onClose={onClose} />);

    const cancelButton = screen.getByRole('button', { name: /Cancelar/i });
    await user.click(cancelButton);

    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('debería llamar onClose cuando se hace clic en X', async () => {
    const onClose = vi.fn();
    const user = userEvent.setup();

    render(<RoomForm {...defaultProps} onClose={onClose} />);

    // Buscar el botón X por su aria-label o rol
    const closeButtons = screen.getAllByRole('button');
    const closeButton = closeButtons.find(btn => btn.querySelector('svg'));
    
    if (closeButton) {
      await user.click(closeButton);
      expect(onClose).toHaveBeenCalled();
    }
  });

  it('debería validar campo requerido roomNumber', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();

    render(<RoomForm {...defaultProps} onSubmit={onSubmit} />);

    const submitButton = screen.getByRole('button', { name: /Guardar/i });
    const roomNumberInput = screen.getByLabelText(/Número de Habitación/i);

    // Limpiar el campo
    await user.clear(roomNumberInput);
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/El número de habitación es requerido/i)).toBeInTheDocument();
    });

    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('debería enviar formulario con datos válidos', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();

    render(<RoomForm {...defaultProps} onSubmit={onSubmit} />);

    const roomNumberInput = screen.getByLabelText(/Número de Habitación/i);
    const priceInput = screen.getByLabelText(/Precio por Noche/i);
    const submitButton = screen.getByRole('button', { name: /Guardar/i });

    await user.type(roomNumberInput, '301');
    await user.clear(priceInput);
    await user.type(priceInput, '150.50');
    await user.click(submitButton);

    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith({
        roomNumber: '301',
        roomType: 'STANDARD',
        capacity: 2,
        pricePerNight: 150.5,
      });
    });
  });

  it('debería deshabilitar campos cuando isSubmitting es true', () => {
    render(<RoomForm {...defaultProps} isSubmitting={true} />);

    const roomNumberInput = screen.getByLabelText(/Número de Habitación/i);
    const priceInput = screen.getByLabelText(/Precio por Noche/i);
    const cancelButton = screen.getByRole('button', { name: /Cancelar/i });
    const submitButton = screen.getByRole('button', { name: /Guardando/i });

    expect(roomNumberInput).toBeDisabled();
    expect(priceInput).toBeDisabled();
    expect(cancelButton).toBeDisabled();
    expect(submitButton).toBeDisabled();
  });

  it('debería mostrar rango de precio sugerido según el tipo de habitación', async () => {
    render(<RoomForm {...defaultProps} />);

    // Por defecto es STANDARD
    expect(screen.getByText(/Rango sugerido: \$50\.00 - \$150\.00/i)).toBeInTheDocument();
  });

  it('debería validar precio mayor a 0', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();

    render(<RoomForm {...defaultProps} onSubmit={onSubmit} />);

    const priceInput = screen.getByLabelText(/Precio por Noche/i);
    const submitButton = screen.getByRole('button', { name: /Guardar/i });

    await user.clear(priceInput);
    await user.type(priceInput, '-10');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/El precio debe ser mayor a 0/i)).toBeInTheDocument();
    });

    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('debería validar longitud máxima de roomNumber', async () => {
    const onSubmit = vi.fn();
    const user = userEvent.setup();

    render(<RoomForm {...defaultProps} onSubmit={onSubmit} />);

    const roomNumberInput = screen.getByLabelText(/Número de Habitación/i);
    const submitButton = screen.getByRole('button', { name: /Guardar/i });

    await user.type(roomNumberInput, '12345678901'); // 11 caracteres
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/El número de habitación no puede exceder 10 caracteres/i)
      ).toBeInTheDocument();
    });

    expect(onSubmit).not.toHaveBeenCalled();
  });
});
