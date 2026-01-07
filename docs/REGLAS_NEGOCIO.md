# Reglas de Negocio - Motor de Reservas de Hotel

Este documento centraliza las reglas de negocio del sistema. Cada regla tiene un identificador único que se referencia en las historias de usuario correspondientes.

---

## Índice de Reglas

| ID | Nombre | Historias Relacionadas |
|----|--------|------------------------|
| RN-001 | Política de Cancelación | 7.1 |
| RN-002 | Horarios de Check-in/Check-out | 5.2 |
| RN-003 | Tiempo Límite de Pago | 3.1, 4.1 |
| RN-004 | Validaciones de Reserva | 3.1, 6.1 |
| RN-005 | Capacidad de Habitaciones | 3.1 |
| RN-006 | Validaciones de Habitación | 2.1 |
| RN-007 | Validaciones de Huésped | 3.2 |
| RN-008 | Cálculo de Tarifas | 3.1, 6.1, 6.2 |
| RN-009 | Estados de Reserva | 3.1, 4.1, 7.1 |
| RN-010 | Estados de Habitación | 2.2 |
| RN-011 | Registro de Pagos | 4.1 |
| RN-012 | Bloqueo Temporal de Habitación | 3.1 |
| RN-013 | Roles y Permisos | 1.1, 1.2, 1.3 |
| RN-014 | Gestión de Usuarios | 1.2, 1.4 |

---

## RN-001: Política de Cancelación

Define las penalidades aplicables según el tiempo de anticipación de la cancelación.

### Reglas:
| Anticipación | Penalidad | Reembolso |
|--------------|-----------|-----------|
| 7+ días antes del check-in | 0% | 100% |
| Entre 2 y 7 días antes | 50% | 50% |
| Menos de 2 días antes | 100% | 0% |
| 0 dias (reserva activa) | 100% | 0% |

### Aplica a:
- Historia 7.1: Cancelar reserva existente

### Validaciones:
```
SI días_anticipación >= 7 ENTONCES
  reembolso = 100%
SI días_anticipación >= 2 Y días_anticipación < 7 ENTONCES
  reembolso = 50%
SI días_anticipación < 2 ENTONCES
  reembolso = 0%
SI reserva.estado = "Activa" ENTONCES
  reembolso = 0%
```

---

## RN-002: Horarios de Check-in/Check-out

Define los horarios estándar para entrada y salida de huéspedes.

### Reglas:
- **Hora de Check-in:** 15:00 hrs
- **Hora de Check-out:** 11:00 hrs

### Aplica a:
- Historia 5.2: Ver reservas del día

---

## RN-003: Tiempo Límite de Pago

Define el tiempo máximo para completar el pago de una reserva.

### Reglas:
- Las reservas pendientes expiran en **24 horas** desde su creación
- Al expirar, la reserva cambia a estado "Expirada" y la habitación se libera automáticamente

### Aplica a:
- Historia 3.1: Crear reserva para un huésped
- Historia 4.1: Confirmar pago de reserva

---

## RN-004: Validaciones de Reserva

Define las restricciones para la creación y modificación de reservas.

### Reglas:
- **Estadía mínima:** 1 noche
- **Estadía máxima:** 30 noches por reserva
- **Fecha de salida:** Debe ser posterior a la fecha de entrada
- **Solapamiento:** No se permiten reservas con fechas solapadas para la misma habitación

### Aplica a:
- Historia 3.1: Crear reserva para un huésped
- Historia 6.1: Modificar fechas de reserva existente

---

## RN-005: Capacidad de Habitaciones

Define las reglas de ocupación según la capacidad de cada habitación.

### Reglas:
- El número de huéspedes no puede exceder la capacidad máxima de la habitación
- Niños menores de 3 años no cuentan para el cálculo de capacidad

### Aplica a:
- Historia 3.1: Crear reserva para un huésped

---

## RN-006: Validaciones de Habitación

Define las reglas para el registro de habitaciones.

### Reglas:
- **Número de habitación:** Único y obligatorio
- **Tipo de habitación:** Estándar, Superior o Suite
- **Capacidad:** Entre 1 y 10 personas
- **Precio por noche:** Mayor a 0

### Aplica a:
- Historia 2.1: Registrar habitaciones del hotel

---

## RN-007: Validaciones de Huésped

Define las validaciones para la información del huésped.

### Reglas:
- **Nombre completo:** Obligatorio
- **Documento de identidad:** Obligatorio y único
- **Teléfono:** Obligatorio
- **Correo electrónico:** Obligatorio, formato válido

### Aplica a:
- Historia 3.2: Registrar información completa del huésped

---

## RN-008: Cálculo de Tarifas

Define las reglas para el cálculo del precio de las reservas.

### Reglas:
- **Fórmula:** Precio por noche × Número de noches
- Al modificar fechas o habitación, se recalcula el monto total

### Aplica a:
- Historia 3.1: Crear reserva para un huésped
- Historia 6.1: Modificar fechas de reserva existente
- Historia 6.2: Cambiar habitación asignada

---

## RN-009: Estados de Reserva

Define los estados válidos de una reserva y sus transiciones permitidas.

### Estados:
| Estado | Descripción |
|--------|-------------|
| **Pendiente** | Reserva creada, esperando pago |
| **Pagada** | Pago confirmado, esperando check-in |
| **Activa** | Huésped realizó check-in |
| **Completada** | Huésped realizó check-out |
| **Cancelada** | Reserva anulada |
| **Expirada** | Tiempo de pago excedido |

### Transiciones permitidas:
```
Pendiente → Pagada → Activa → Completada
Pendiente → Cancelada | Expirada
Pagada → Cancelada
```

### Aplica a:
- Historia 3.1: Crear reserva para un huésped
- Historia 4.1: Confirmar pago de reserva
- Historia 7.1: Cancelar reserva existente

---

## RN-010: Estados de Habitación

Define los estados operativos de una habitación.

### Estados:
| Estado | Descripción |
|--------|-------------|
| **Disponible** | Libre para reservar |
| **Reservada** | Asignada a una reserva futura |
| **Bloqueada temporalmente** | Reservada temporalmente mientras se confirma el pago |
| **Ocupada** | Huésped actualmente alojado |

### Reglas:
- El estado se determina automáticamente según las reservas registradas

### Aplica a:
- Historia 2.2: Consultar estado de ocupación de habitaciones

---

## RN-011: Registro de Pagos

Define las reglas para el registro manual de pagos.

### Métodos de Pago:
| Método | Referencia Requerida |
|--------|---------------------|
| Efectivo | No |
| Tarjeta | Sí (número de autorización POS) |
| Transferencia | Sí (número de comprobante) |

### Reglas:
- Todo pago debe registrar: monto, método y fecha/hora
- El pago debe ser del 100% del monto total
- Si no se completa en 24 horas, la reserva expira (RN-003)

### Aplica a:
- Historia 4.1: Confirmar pago de reserva

## RN-012: Bloqueo Temporal de Habitación

### Reglas:
- Al crear una reserva, la habitación se bloquea por **15 minutos**
- El bloqueo se libera automáticamente si:
  - Pasan 15 minutos sin confirmar el pago, O
  - La reserva cambia a estado "Expirada" o "Cancelada"
- Solo se permite un bloqueo activo por habitación y período de fechas

### Aplica a:
- Historia 3.1: Crear reserva para un huésped

---

## RN-013: Roles y Permisos

Define los roles del sistema y sus permisos asociados.

### Roles:
| Rol | Descripción |
|-----|-------------|
| **Administrador** | Acceso completo al sistema |
| **Recepcionista** | Gestión de reservas y operaciones diarias |

### Matriz de Permisos:
| Funcionalidad | Administrador | Recepcionista |
|---------------|---------------|---------------|
| Gestión de habitaciones | ✅ | ❌ |
| Consultar ocupación | ✅ | ✅ |
| Crear/modificar reservas | ✅ | ✅ |
| Confirmar pagos | ✅ | ✅ |
| Check-in/Check-out | ✅ | ✅ |
| Cancelar reservas | ✅ | ✅ |
| Gestión de usuarios | ✅ | ❌ |

### Reglas:
- Todo usuario debe tener exactamente un rol asignado
- Los permisos se validan en cada operación
- El sistema es extensible para futuros roles adicionales

### Aplica a:
- Historia 1.1: Iniciar sesión en el sistema
- Historia 1.2: Crear usuario empleado
- Historia 1.3: Gestionar roles de usuario

---

## RN-014: Gestión de Usuarios

Define las reglas para la administración de cuentas de usuario.

### Reglas:
- **Correo electrónico:** Único y obligatorio (usado como identificador)
- **Contraseña:** Mínimo 8 caracteres
- **Estado de cuenta:** Activo o Inactivo
- Debe existir al menos un usuario administrador activo en el sistema
- Los usuarios desactivados no pueden iniciar sesión pero su historial se conserva

### Estados de Usuario:
| Estado | Descripción |
|--------|-------------|
| **Activo** | Puede iniciar sesión y operar |
| **Inactivo** | No puede iniciar sesión, historial preservado |

### Aplica a:
- Historia 1.2: Crear usuario empleado
- Historia 1.4: Desactivar usuario

---

## Historial de Cambios

| Versión | Fecha | Descripción |
|---------|-------|-------------|
| 1.0 | 2026-01-06 | Documento inicial con reglas de negocio |

---

## Notas Importantes

1. **Modificación de reglas:** Cualquier cambio en las reglas de negocio debe ser reflejado en este documento antes de implementarse.