# Motor de Reservas de Hotel - Historias de Usuario

**Usuarios principales:** Recepcionista y Administrador del hotel

---

## Épica 1: Autenticación y Gestión de Usuarios

### Historia 1.1: Iniciar sesión en el sistema
**Como** empleado del hotel (recepcionista o administrador)  
**Quiero** iniciar sesión con mis credenciales  
**Para** acceder a las funcionalidades del sistema según mi rol

**Reglas de negocio:** [RN-013](REGLAS_NEGOCIO.md#rn-013-roles-y-permisos), [RN-014](REGLAS_NEGOCIO.md#rn-014-gestión-de-usuarios)

**Nota técnica:** La autenticación se gestiona mediante Keycloak como proveedor de identidad externo.

#### Escenarios BDD:

```gherkin
Escenario: Inicio de sesión exitoso
  Dado que soy un usuario registrado con rol "Recepcionista"
  Cuando ingreso mi correo electrónico y contraseña correctos
  Entonces accedo al sistema
  Y veo el dashboard correspondiente a mi rol
  Y mi sesión permanece activa durante mi jornada laboral

Escenario: Inicio de sesión con credenciales inválidas
  Dado que intento acceder al sistema
  Cuando ingreso una contraseña incorrecta
  Entonces recibo un mensaje indicando que las credenciales son inválidas
  Y no puedo acceder al sistema
  Y se registra el intento fallido

Escenario: Inicio de sesión con usuario desactivado
  Dado que mi cuenta ha sido desactivada por el administrador
  Cuando intento iniciar sesión con mis credenciales
  Entonces recibo un mensaje indicando que mi cuenta está desactivada
  Y debo contactar al administrador

Escenario: Cierre de sesión
  Dado que tengo una sesión activa en el sistema
  Cuando selecciono la opción de cerrar sesión
  Entonces mi sesión se termina de forma segura
  Y soy redirigido a la página de inicio de sesión
```

### Historia 1.2: Crear usuario empleado
**Como** administrador del hotel  
**Quiero** crear cuentas de usuario para los empleados  
**Para** que puedan acceder al sistema según su rol asignado

**Reglas de negocio:** [RN-013](REGLAS_NEGOCIO.md#rn-013-roles-y-permisos), [RN-014](REGLAS_NEGOCIO.md#rn-014-gestión-de-usuarios)

#### Escenarios BDD:

```gherkin
Escenario: Creación exitosa de usuario recepcionista
  Dado que accedo al módulo de gestión de usuarios
  Cuando creo un nuevo usuario con nombre "Ana López", correo "ana@hotel.com" y rol "Recepcionista"
  Entonces el usuario queda registrado en el sistema
  Y se genera una contraseña temporal
  Y el nuevo usuario puede iniciar sesión

Escenario: Creación exitosa de usuario administrador
  Dado que accedo al módulo de gestión de usuarios
  Cuando creo un nuevo usuario con rol "Administrador"
  Entonces el usuario queda registrado con permisos completos
  Y puede gestionar habitaciones, reportes y otros usuarios

Escenario: Intento de crear usuario con correo duplicado
  Dado que ya existe un usuario con correo "ana@hotel.com"
  Cuando intento crear otro usuario con el mismo correo
  Entonces recibo un mensaje indicando que el correo ya está registrado
  Y el usuario no se crea

Escenario: Intento de crear usuario sin permisos
  Dado que soy un usuario con rol "Recepcionista"
  Cuando intento acceder al módulo de gestión de usuarios
  Entonces recibo un mensaje indicando que no tengo permisos
  Y no puedo ver ni crear usuarios
```

### Historia 1.3: Gestionar roles de usuario
**Como** administrador del hotel  
**Quiero** asignar y modificar roles de los usuarios  
**Para** controlar qué funcionalidades puede utilizar cada empleado

**Reglas de negocio:** [RN-013](REGLAS_NEGOCIO.md#rn-013-roles-y-permisos)

#### Escenarios BDD:

```gherkin
Escenario: Cambio de rol exitoso
  Dado que existe un usuario "Carlos Ruiz" con rol "Recepcionista"
  Cuando cambio su rol a "Administrador"
  Entonces el usuario adquiere los permisos del nuevo rol
  Y en su próximo inicio de sesión ve las opciones de administrador

Escenario: Visualización de permisos por rol
  Dado que accedo a la gestión de roles
  Cuando consulto el rol "Recepcionista"
  Entonces veo la lista de permisos asociados: crear reservas, gestionar pagos, check-in/out
  Y veo qué funcionalidades NO puede acceder: reportes, gestión de usuarios, habitaciones
```

### Historia 1.4: Desactivar usuario
**Como** administrador del hotel  
**Quiero** desactivar cuentas de usuarios que ya no trabajan en el hotel  
**Para** mantener la seguridad sin perder el historial de sus acciones

**Reglas de negocio:** [RN-014](REGLAS_NEGOCIO.md#rn-014-gestión-de-usuarios)

#### Escenarios BDD:

```gherkin
Escenario: Desactivación exitosa de usuario
  Dado que existe un usuario activo "Pedro Gómez"
  Cuando desactivo su cuenta
  Entonces el usuario no puede iniciar sesión
  Y su historial de acciones en reservas permanece visible
  Y puedo reactivar la cuenta en el futuro si es necesario

Escenario: Intento de desactivar al único administrador
  Dado que solo existe un usuario con rol "Administrador"
  Cuando intento desactivar su cuenta
  Entonces recibo un mensaje indicando que debe existir al menos un administrador activo
  Y la cuenta permanece activa

Escenario: Reactivación de usuario
  Dado que existe un usuario desactivado "Pedro Gómez"
  Cuando reactivo su cuenta
  Entonces el usuario puede volver a iniciar sesión
  Y mantiene su rol y permisos anteriores
```

---

## Épica 2: Gestión de Inventario de Habitaciones

### Historia 2.1: Registrar habitaciones del hotel
**Como** administrador del hotel  
**Quiero** registrar las habitaciones disponibles en el sistema  
**Para** poder gestionar el inventario y realizar reservas

**Reglas de negocio:** [RN-006](REGLAS_NEGOCIO.md#rn-006-validaciones-de-habitación)

#### Escenarios BDD:

```gherkin
Escenario: Registro exitoso de nueva habitación
  Dado que accedo al módulo de gestión de habitaciones
  Cuando registro una habitación con número "301", tipo "Suite", capacidad 4 personas y precio 250 USD por noche
  Entonces la habitación queda registrada en el sistema
  Y aparece en el listado de habitaciones disponibles
  Y puedo visualizar todos sus detalles

Escenario: Intento de registro con número duplicado
  Dado que ya existe una habitación registrada con número "301"
  Cuando intento registrar otra habitación con el mismo número "301"
  Entonces recibo un mensaje indicando que el número de habitación ya existe
  Y la nueva habitación no se registra

Escenario: Intento de registro con datos inválidos
  Dado que accedo al módulo de gestión de habitaciones
  Cuando intento registrar una habitación con capacidad 0 o precio negativo
  Entonces recibo un mensaje indicando los campos con valores inválidos
  Y la habitación no se registra

Escenario: Intento de registro con campos obligatorios vacíos
  Dado que accedo al módulo de gestión de habitaciones
  Cuando intento registrar una habitación sin especificar el número o tipo
  Entonces recibo un mensaje indicando los campos obligatorios faltantes
  Y la habitación no se registra
```

### Historia 2.2: Consultar estado de ocupación de habitaciones
**Como** recepcionista  
**Quiero** visualizar el estado de todas las habitaciones en un período específico  
**Para** conocer la disponibilidad y planificar reservas

**Reglas de negocio:** [RN-010](REGLAS_NEGOCIO.md#rn-010-estados-de-habitación)

#### Escenarios BDD:

```gherkin
Escenario: Visualización de disponibilidad en fecha específica
  Dado que existen habitaciones con diferentes estados de ocupación
  Cuando consulto el estado para el rango "15/02/2026" al "20/02/2026"
  Entonces veo un calendario con todas las habitaciones
  Y cada habitación muestra su estado: Disponible, Reservada o Bloqueada
  Y puedo identificar rápidamente cuáles están libres

Escenario: Filtrado por tipo de habitación
  Dado que visualizo el estado de ocupación
  Cuando filtro por habitaciones tipo "Suite"
  Entonces solo veo el estado de las habitaciones de ese tipo
  Y puedo verificar su disponibilidad específica

Escenario: Consulta con rango de fechas inválido
  Dado que accedo al módulo de consulta de ocupación
  Cuando ingreso fecha de inicio posterior a fecha de fin
  Entonces recibo un mensaje indicando que el rango de fechas es inválido
  Y no se muestra ningún resultado

Escenario: Consulta sin habitaciones registradas
  Dado que no existen habitaciones registradas en el sistema
  Cuando consulto el estado de ocupación
  Entonces veo un mensaje indicando que no hay habitaciones registradas
  Y se sugiere registrar habitaciones primero
```

---

## Épica 3: Creación de Reservas por Recepcionista

### Historia 3.1: Crear reserva para un huésped
**Como** recepcionista  
**Quiero** crear una reserva en nombre de un huésped  
**Para** asegurar su alojamiento cuando llama o llega al mostrador

**Reglas de negocio:** [RN-003](REGLAS_NEGOCIO.md#rn-003-tiempo-límite-de-pago), [RN-004](REGLAS_NEGOCIO.md#rn-004-validaciones-de-reserva), [RN-005](REGLAS_NEGOCIO.md#rn-005-capacidad-de-habitaciones), [RN-008](REGLAS_NEGOCIO.md#rn-008-cálculo-de-tarifas), [RN-009](REGLAS_NEGOCIO.md#rn-009-estados-de-reserva), [RN-012](REGLAS_NEGOCIO.md#rn-012-bloqueo-temporal-de-habitación)

#### Escenarios BDD:

```gherkin
Escenario: Creación exitosa de reserva
  Dado que la habitación 301 está disponible del "10/03/2026" al "15/03/2026"
  Cuando creo una reserva para el huésped "Juan Pérez" en esas fechas
  Y registro sus datos de contacto: teléfono y correo electrónico
  Entonces la reserva se crea en estado "Pendiente"
  Y la habitación 301 queda bloqueada para ese período
  Y se genera un número de reserva único
  Y puedo ver el resumen con el monto total calculado

Escenario: Intento de reserva sobre fechas no disponibles
  Dado que la habitación 301 ya tiene una reserva del "10/03/2026" al "15/03/2026"
  Cuando intento crear otra reserva para la misma habitación en fechas que se solapan
  Entonces recibo un mensaje indicando el conflicto de fechas
  Y la reserva no se crea
  Y se me muestran fechas alternativas disponibles para esa habitación

Escenario: Intento de reserva con capacidad excedida
  Dado que la habitación 301 tiene capacidad para 2 personas
  Cuando intento crear una reserva para 4 huéspedes
  Entonces recibo un mensaje indicando que la capacidad es insuficiente
  Y se me sugieren habitaciones con capacidad adecuada

Escenario: Intento de reserva con estadía mayor a 30 noches
  Dado que la habitación 301 está disponible
  Cuando intento crear una reserva por 45 noches
  Entonces recibo un mensaje indicando que la estadía máxima es 30 noches
  Y la reserva no se crea

Escenario: Intento de reserva con fecha de entrada en el pasado
  Dado que la fecha actual es "10/03/2026"
  Cuando intento crear una reserva con fecha de entrada "05/03/2026"
  Entonces recibo un mensaje indicando que no se permiten fechas pasadas
  Y la reserva no se crea
```

### Historia 3.2: Registrar información completa del huésped
**Como** recepcionista  
**Quiero** capturar toda la información necesaria del huésped durante la reserva  
**Para** mantener un registro completo y poder contactarlo

**Reglas de negocio:** [RN-007](REGLAS_NEGOCIO.md#rn-007-validaciones-de-huésped)

#### Escenarios BDD:

```gherkin
Escenario: Registro completo de datos del huésped
  Dado que estoy creando una reserva
  Cuando ingreso nombre "María García", documento "12345678", teléfono "+57 300 1234567" y correo "maria@email.com"
  Y todos los datos son válidos
  Entonces la información se asocia correctamente a la reserva
  Y puedo continuar con el proceso

Escenario: Intento de registro con datos inválidos
  Dado que estoy creando una reserva
  Cuando ingreso un correo electrónico en formato inválido "maria.email.com"
  Entonces recibo un mensaje indicando el formato correcto requerido
  Y no puedo avanzar hasta corregir el dato

Escenario: Registro con huésped existente
  Dado que estoy creando una reserva
  Y existe un huésped con documento "12345678" registrado previamente
  Cuando ingreso el documento "12345678"
  Entonces el sistema autocompleta los datos del huésped existente
  Y puedo confirmar o actualizar la información
```

---

## Épica 4: Confirmación y Pago de Reservas

### Historia 4.1: Confirmar pago de reserva
**Como** recepcionista  
**Quiero** registrar que un huésped ha completado el pago  
**Para** confirmar definitivamente su reserva

**Reglas de negocio:** [RN-003](REGLAS_NEGOCIO.md#rn-003-tiempo-límite-de-pago), [RN-009](REGLAS_NEGOCIO.md#rn-009-estados-de-reserva), [RN-011](REGLAS_NEGOCIO.md#rn-011-registro-de-pagos)

**Nota técnica:** En esta versión, el sistema no procesa pagos electrónicos. El recepcionista registra manualmente la confirmación del pago realizado por medios externos (POS físico, transferencia bancaria o efectivo).

#### Escenarios BDD:

```gherkin
Escenario: Confirmación de pago en efectivo
  Dado que existe una reserva en estado "Pendiente" por 1250 USD
  Cuando selecciono la reserva y registro el pago
  Y selecciono método de pago "Efectivo"
  Y ingreso el monto recibido "1250 USD"
  Entonces la reserva cambia a estado "Pagada"
  Y se registra el método de pago como "Efectivo"
  Y se registra la fecha y hora de confirmación
  Y puedo generar un comprobante de pago

Escenario: Confirmación de pago con tarjeta (POS externo)
  Dado que existe una reserva en estado "Pendiente" por 1500 USD
  Y el huésped pagó usando el POS físico del hotel
  Cuando registro el pago con método "Tarjeta"
  Y ingreso el número de referencia/autorización del POS "AUTH-789456"
  Entonces la reserva cambia a estado "Pagada"
  Y se registra el método de pago como "Tarjeta"
  Y se guarda el número de referencia para conciliación

Escenario: Confirmación de pago por transferencia bancaria
  Dado que existe una reserva en estado "Pendiente" por 2000 USD
  Y el huésped realizó una transferencia bancaria
  Cuando verifico que el depósito aparece en la cuenta del hotel
  Y registro el pago con método "Transferencia"
  Y ingreso el número de comprobante "TRF-2026-001234"
  Entonces la reserva cambia a estado "Pagada"
  Y se registra el método de pago como "Transferencia"
  Y se guarda el número de comprobante

Escenario: Intento de confirmar pago sin ingresar referencia requerida
  Dado que existe una reserva en estado "Pendiente"
  Cuando intento registrar un pago con tarjeta
  Y no ingreso el número de referencia del POS
  Entonces recibo un mensaje indicando que la referencia es obligatoria
  Y el pago no se registra hasta completar el dato

Escenario: Intento de confirmar pago en reserva expirada
  Dado que existe una reserva en estado "Expirada"
  Cuando intento registrar un pago
  Entonces recibo un mensaje indicando que la reserva ha expirado
  Y se sugiere crear una nueva reserva si hay disponibilidad

Escenario: Intento de confirmar pago con monto incorrecto
  Dado que existe una reserva en estado "Pendiente" por 1250 USD
  Cuando intento registrar un pago de 1000 USD
  Entonces recibo un mensaje indicando que el monto no coincide
  Y el pago no se registra hasta corregir el monto
```

---

## Épica 5: Consulta y Búsqueda de Reservas

### Historia 5.1: Buscar reservas existentes
**Como** recepcionista  
**Quiero** buscar reservas por diferentes criterios  
**Para** localizar rápidamente la información que necesito

#### Escenarios BDD:

```gherkin
Escenario: Búsqueda por número de reserva
  Dado que existen múltiples reservas en el sistema
  Cuando busco por número de reserva "RES-2026-001234"
  Entonces encuentro la reserva correspondiente
  Y veo todos sus detalles: huésped, habitación, fechas y estado

Escenario: Búsqueda por nombre de huésped
  Dado que existen múltiples reservas en el sistema
  Cuando busco por nombre "Juan Pérez"
  Entonces veo todas las reservas asociadas a ese nombre
  Y están ordenadas por fecha de llegada
  Y puedo ver el estado de cada una

Escenario: Búsqueda sin resultados
  Dado que busco por un criterio que no existe en el sistema
  Cuando ingreso número de reserva "RES-9999-999999"
  Entonces recibo un mensaje indicando que no se encontraron resultados
  Y se me sugiere verificar los datos de búsqueda
```

### Historia 5.2: Ver reservas del día
**Como** recepcionista  
**Quiero** visualizar todas las llegadas y salidas del día actual  
**Para** preparar las habitaciones y gestionar los check-in/check-out

**Reglas de negocio:** [RN-002](REGLAS_NEGOCIO.md#rn-002-horarios-de-check-incheck-out)

#### Escenarios BDD:

```gherkin
Escenario: Visualización de llegadas del día
  Dado que existen reservas con fecha de entrada para hoy
  Cuando accedo a la vista de "Llegadas de hoy"
  Entonces veo una lista de todas las reservas con entrada programada
  Y cada reserva muestra: nombre del huésped, habitación asignada y hora estimada
  Y puedo marcar cada una como check-in completado

Escenario: Visualización de salidas del día
  Dado que existen reservas con fecha de salida para hoy
  Cuando accedo a la vista de "Salidas de hoy"
  Entonces veo una lista de todas las reservas con salida programada
  Y cada reserva muestra: nombre del huésped, habitación y hora límite de salida
  Y puedo marcar cada una como check-out completado

Escenario: Día sin llegadas ni salidas programadas
  Dado que no existen reservas con entrada o salida para hoy
  Cuando accedo a la vista de reservas del día
  Entonces veo un mensaje indicando que no hay llegadas ni salidas programadas
  Y la vista se muestra vacía
```

---

## Épica 6: Modificación de Reservas

### Historia 6.1: Modificar fechas de reserva existente
**Como** recepcionista  
**Quiero** cambiar las fechas de una reserva existente  
**Para** ajustarla a las necesidades cambiantes del huésped

**Reglas de negocio:** [RN-004](REGLAS_NEGOCIO.md#rn-004-validaciones-de-reserva), [RN-008](REGLAS_NEGOCIO.md#rn-008-cálculo-de-tarifas)

#### Escenarios BDD:

```gherkin
Escenario: Modificación exitosa con misma habitación disponible
  Dado que existe una reserva confirmada en habitación 301 del "10/03/2026" al "15/03/2026"
  Y la habitación 301 está disponible del "12/03/2026" al "17/03/2026"
  Cuando modifico las fechas al nuevo período
  Entonces la reserva se actualiza con las nuevas fechas
  Y se recalcula el monto total
  Y se registra la modificación en el historial de la reserva

Escenario: Modificación requiere cambio de habitación
  Dado que existe una reserva en habitación 301 del "10/03/2026" al "15/03/2026"
  Y la habitación 301 NO está disponible para las nuevas fechas solicitadas
  Cuando intento modificar a fechas donde no hay disponibilidad
  Entonces recibo un mensaje indicando que se requiere cambio de habitación
  Y se me muestran habitaciones alternativas disponibles para esas fechas
  Y puedo reasignar la reserva a una habitación alternativa

Escenario: Intento de modificar reserva ya activa
  Dado que existe una reserva en estado "Activa" (check-in realizado)
  Cuando intento modificar las fechas de entrada
  Entonces recibo un mensaje indicando que no se puede modificar la fecha de entrada
  Y solo puedo extender o acortar la fecha de salida si hay disponibilidad

Escenario: Intento de modificar a fechas que solapan con otra reserva
  Dado que existe una reserva en habitación 301 del "10/03/2026" al "15/03/2026"
  Y existe otra reserva en la misma habitación del "18/03/2026" al "22/03/2026"
  Cuando intento modificar las fechas a "14/03/2026" al "20/03/2026"
  Entonces recibo un mensaje indicando el conflicto con la otra reserva
  Y la modificación no se realiza
```

### Historia 6.2: Cambiar habitación asignada
**Como** recepcionista  
**Quiero** reasignar una reserva a otra habitación  
**Para** resolver situaciones de mantenimiento u ofrecer mejoras al huésped

**Reglas de negocio:** [RN-008](REGLAS_NEGOCIO.md#rn-008-cálculo-de-tarifas)

#### Escenarios BDD:

```gherkin
Escenario: Reasignación exitosa a habitación similar
  Dado que existe una reserva confirmada en habitación 301
  Y la habitación 302 del mismo tipo está disponible para el mismo período
  Cuando reasigno la reserva a la habitación 302
  Entonces la reserva se actualiza con la nueva habitación
  Y la habitación 301 queda nuevamente disponible
  Y la habitación 302 queda reservada
  Y se registra el cambio en el historial

Escenario: Upgrade de habitación con ajuste de precio
  Dado que existe una reserva confirmada en habitación "Estándar" por 100 USD/noche
  Y reasigno a una habitación "Suite" que cuesta 200 USD/noche
  Cuando confirmo el cambio
  Entonces se recalcula el nuevo monto total
  Y se muestra la diferencia a cobrar o reembolsar
  Y puedo procesar el ajuste de pago correspondiente

Escenario: Intento de reasignación sin habitaciones disponibles
  Dado que existe una reserva confirmada en habitación 301
  Y no hay otras habitaciones disponibles para el mismo período
  Cuando intento reasignar la reserva
  Entonces recibo un mensaje indicando que no hay habitaciones alternativas disponibles
  Y la reserva permanece en la habitación original
```

---

## Épica 7: Cancelación de Reservas

### Historia 7.1: Cancelar reserva existente
**Como** recepcionista  
**Quiero** cancelar una reserva a solicitud del huésped  
**Para** liberar la habitación y gestionar el reembolso según política

**Reglas de negocio:** [RN-001](REGLAS_NEGOCIO.md#rn-001-política-de-cancelación), [RN-009](REGLAS_NEGOCIO.md#rn-009-estados-de-reserva)

#### Escenarios BDD:

```gherkin
Escenario: Cancelación sin penalidad
  Dado que existe una reserva confirmada para dentro de 10 días
  Y la política permite cancelación sin cargo hasta 7 días antes
  Cuando cancelo la reserva
  Entonces la reserva cambia a estado "Cancelada"
  Y la habitación queda disponible nuevamente
  Y se calcula reembolso del 100% del monto pagado
  Y se registra la fecha y motivo de cancelación

Escenario: Cancelación con penalidad aplicada
  Dado que existe una reserva confirmada para dentro de 3 días
  Y la política establece penalidad del 50% con menos de 7 días de anticipación
  Cuando cancelo la reserva
  Entonces se calcula automáticamente la penalidad del 50%
  Y se muestra el monto a reembolsar (50% del total)
  Y al confirmar, la reserva cambia a estado "Cancelada"
  Y se registra el monto de penalidad aplicada

Escenario: Cancelación de reserva ya iniciada
  Dado que existe una reserva donde el check-in ya fue realizado
  Cuando intento cancelar la reserva
  Entonces recibo una advertencia sobre cancelación de estadía en curso
  Y debo confirmar explícitamente la acción
  Y se aplica la política de cancelación para estadías iniciadas
```