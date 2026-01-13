import { Fragment } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Dialog, Transition, Listbox } from '@headlessui/react';
import { XMarkIcon, CheckIcon, ChevronUpDownIcon } from '@heroicons/react/24/outline';
import { roomFormSchema, type RoomFormData, formatPrice } from '../../utils/validation';
import {
  ROOM_TYPE_OPTIONS,
  CAPACITY_OPTIONS,
  SUGGESTED_PRICE_RANGES,
} from '../../utils/constants';

interface RoomFormProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: RoomFormData) => void;
  isSubmitting?: boolean;
}

/**
 * Formulario modal para crear/editar habitaciones
 * 
 * Características:
 * - Validación con Zod
 * - React Hook Form para manejo de estado
 * - Headless UI para componentes accesibles
 * - Sugerencias de precio según tipo de habitación
 */
export function RoomForm({ isOpen, onClose, onSubmit, isSubmitting = false }: RoomFormProps) {
  const {
    register,
    handleSubmit,
    control,
    watch,
    reset,
    formState: { errors },
  } = useForm<RoomFormData>({
    resolver: zodResolver(roomFormSchema),
    defaultValues: {
      roomNumber: '',
      roomType: 'STANDARD',
      capacity: 2,
      pricePerNight: 100,
    },
  });

  const selectedRoomType = watch('roomType');
  const suggestedPrice = SUGGESTED_PRICE_RANGES[selectedRoomType];

  const handleClose = () => {
    reset();
    onClose();
  };

  const handleFormSubmit = (data: RoomFormData) => {
    onSubmit(data);
    reset();
  };

  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-10" onClose={handleClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/25 backdrop-blur-sm" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4 text-center">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all">
                <div className="flex items-center justify-between mb-4">
                  <Dialog.Title
                    as="h3"
                    className="text-lg font-medium leading-6 text-gray-900"
                  >
                    Registrar Nueva Habitación
                  </Dialog.Title>
                  <button
                    type="button"
                    className="text-gray-400 hover:text-gray-500"
                    onClick={handleClose}
                  >
                    <XMarkIcon className="h-6 w-6" />
                  </button>
                </div>

                <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
                  {/* Número de Habitación */}
                  <div>
                    <label htmlFor="roomNumber" className="block text-sm font-medium text-gray-700">
                      Número de Habitación *
                    </label>
                    <input
                      {...register('roomNumber')}
                      type="text"
                      id="roomNumber"
                      className={`mt-1 block w-full rounded-md border ${
                        errors.roomNumber ? 'border-red-300' : 'border-gray-300'
                      } px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm`}
                      placeholder="Ej: 301, A-12"
                      disabled={isSubmitting}
                    />
                    {errors.roomNumber && (
                      <p className="mt-1 text-sm text-red-600">{errors.roomNumber.message}</p>
                    )}
                  </div>

                  {/* Tipo de Habitación */}
                  <div>
                    <label htmlFor="roomType" className="block text-sm font-medium text-gray-700 mb-1">
                      Tipo de Habitación *
                    </label>
                    <Controller
                      name="roomType"
                      control={control}
                      render={({ field }) => (
                        <Listbox value={field.value} onChange={field.onChange} disabled={isSubmitting}>
                          <div className="relative">
                            <Listbox.Button
                              className={`relative w-full cursor-default rounded-md border ${
                                errors.roomType ? 'border-red-300' : 'border-gray-300'
                              } bg-white py-2 pl-3 pr-10 text-left shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:text-sm`}
                            >
                              <span className="block truncate">
                                {ROOM_TYPE_OPTIONS.find((opt) => opt.value === field.value)?.label}
                              </span>
                              <span className="pointer-events-none absolute inset-y-0 right-0 flex items-center pr-2">
                                <ChevronUpDownIcon
                                  className="h-5 w-5 text-gray-400"
                                  aria-hidden="true"
                                />
                              </span>
                            </Listbox.Button>
                            <Transition
                              as={Fragment}
                              leave="transition ease-in duration-100"
                              leaveFrom="opacity-100"
                              leaveTo="opacity-0"
                            >
                              <Listbox.Options className="absolute z-10 mt-1 max-h-60 w-full overflow-auto rounded-md bg-white py-1 text-base shadow-lg ring-1 ring-black/5 focus:outline-none sm:text-sm">
                                {ROOM_TYPE_OPTIONS.map((option) => (
                                  <Listbox.Option
                                    key={option.value}
                                    className={({ active }) =>
                                      `relative cursor-default select-none py-2 pl-10 pr-4 ${
                                        active ? 'bg-indigo-100 text-indigo-900' : 'text-gray-900'
                                      }`
                                    }
                                    value={option.value}
                                  >
                                    {({ selected }) => (
                                      <>
                                        <div>
                                          <span className={`block truncate ${selected ? 'font-medium' : 'font-normal'}`}>
                                            {option.label}
                                          </span>
                                          <span className="block text-xs text-gray-500">
                                            {option.description}
                                          </span>
                                        </div>
                                        {selected && (
                                          <span className="absolute inset-y-0 left-0 flex items-center pl-3 text-indigo-600">
                                            <CheckIcon className="h-5 w-5" aria-hidden="true" />
                                          </span>
                                        )}
                                      </>
                                    )}
                                  </Listbox.Option>
                                ))}
                              </Listbox.Options>
                            </Transition>
                          </div>
                        </Listbox>
                      )}
                    />
                    {errors.roomType && (
                      <p className="mt-1 text-sm text-red-600">{errors.roomType.message}</p>
                    )}
                  </div>

                  {/* Capacidad */}
                  <div>
                    <label htmlFor="capacity" className="block text-sm font-medium text-gray-700">
                      Capacidad (personas) *
                    </label>
                    <Controller
                      name="capacity"
                      control={control}
                      render={({ field }) => (
                        <select
                          {...field}
                          id="capacity"
                          className={`mt-1 block w-full rounded-md border ${
                            errors.capacity ? 'border-red-300' : 'border-gray-300'
                          } px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm`}
                          disabled={isSubmitting}
                          onChange={(e) => field.onChange(Number(e.target.value))}
                        >
                          {CAPACITY_OPTIONS.map((option) => (
                            <option key={option.value} value={option.value}>
                              {option.label}
                            </option>
                          ))}
                        </select>
                      )}
                    />
                    {errors.capacity && (
                      <p className="mt-1 text-sm text-red-600">{errors.capacity.message}</p>
                    )}
                  </div>

                  {/* Precio por Noche */}
                  <div>
                    <label htmlFor="pricePerNight" className="block text-sm font-medium text-gray-700">
                      Precio por Noche (USD) *
                    </label>
                    <input
                      {...register('pricePerNight', { valueAsNumber: true })}
                      type="number"
                      id="pricePerNight"
                      step="0.01"
                      min="0.01"
                      className={`mt-1 block w-full rounded-md border ${
                        errors.pricePerNight ? 'border-red-300' : 'border-gray-300'
                      } px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-indigo-500 sm:text-sm`}
                      placeholder="Ej: 100.00"
                      disabled={isSubmitting}
                    />
                    {errors.pricePerNight && (
                      <p className="mt-1 text-sm text-red-600">{errors.pricePerNight.message}</p>
                    )}
                    <p className="mt-1 text-xs text-gray-500">
                      Rango sugerido: ${formatPrice(suggestedPrice.min)} - ${formatPrice(suggestedPrice.max)}
                    </p>
                  </div>

                  {/* Botones de Acción */}
                  <div className="mt-6 flex justify-end space-x-3">
                    <button
                      type="button"
                      className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
                      onClick={handleClose}
                      disabled={isSubmitting}
                    >
                      Cancelar
                    </button>
                    <button
                      type="submit"
                      className="rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
                      disabled={isSubmitting}
                    >
                      {isSubmitting ? 'Guardando...' : 'Guardar'}
                    </button>
                  </div>
                </form>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
}
