# Matriz de casos — Ae6

Reglas de negocio de `ReservaService` y de la entidad `Reserva`. Cada caso tiene una prueba JUnit 5 con el mismo ID en su `@DisplayName`.
Tipos: Normal · Alternativo · Límite · Inválido · Excepción · Interacción. Los casos marcados con **(Ae6)** se agregaron en esta actividad.

## 1. Cancelación — `puedeCancelar(int horasAnticipacion)`
Regla: se permite cancelar con 2 o más horas de anticipación (`>= 2`).

| ID | Regla | Escenario | Entrada | Esperado | Tipo | Riesgo que protege | Prueba |
|---|---|---|---|---|---|---|---|
| CP-01 | Cancelación | Anticipación habitual | 5 | true | Normal | Rechazar cancelaciones válidas | `cincoHorasPermitenCancelar` |
| CP-02 | Cancelación | Límite permitido | 2 | true | Límite | Usar `>` en lugar de `>=` | `dosHorasEsElLimitePermitido` |
| CP-03 | Cancelación | Debajo del límite | 1 | false | Límite | Límite desplazado a 1 | `unaHoraNoPermiteCancelar` |
| CP-10 | Cancelación | Encima del límite | 3 | true | Límite | Condición `== 2` | `tresHorasPermitenCancelar` |
| CP-04 | Cancelación | Sin anticipación | 0 | false | Límite | Permitir cancelación inmediata | `ceroHorasNoPermiteCancelar` |
| CP-15 | Cancelación | Horas negativas **(Ae6)** | -1 | false (comportamiento actual) | Inválido | Aceptar un dato imposible como válido | `horasNegativasNoPermitenCancelar` |

## 2. Descuentos — `calcularTotal(String tipo, double totalBase)`
Regla: VIP 15 %, ESTUDIANTE 10 %, cualquier otro tipo sin descuento; total negativo es inválido.

| ID | Regla | Escenario | Entrada | Esperado | Tipo | Riesgo que protege | Prueba |
|---|---|---|---|---|---|---|---|
| CP-05 | Descuentos | NORMAL | NORMAL, 100 | 100 | Normal | Descuento indebido | `normalNoRecibeDescuento` |
| CP-06 | Descuentos | VIP | VIP, 100 | 85 | Alternativo | Porcentaje VIP incorrecto | `vipRecibeQuincePorCiento` |
| CP-07 | Descuentos | ESTUDIANTE | ESTUDIANTE, 100 | 90 | Alternativo | Porcentaje ESTUDIANTE incorrecto | `estudianteRecibeDiezPorCiento` |
| CP-11 | Descuentos | VIP con decimales | VIP, 59.99 | 50.9915 (±0.001) | Normal | Errores de redondeo | `vipConMontoDecimalAplicaDescuentoExacto` |
| CP-08 | Descuentos | Total cero | VIP, 0 | 0 | Límite | Rechazar el 0 como inválido | `totalCeroDevuelveCero` |
| CP-09 | Descuentos | Total negativo | NORMAL, -1 | IllegalArgumentException "Total base inválido" | Excepción | Aceptar montos imposibles | `totalNegativoEsInvalido` |
| CP-12 | Descuentos | VIP en minúsculas **(Ae6)** | vip, 100 | 85 | Alternativo | Cambiar `equalsIgnoreCase` por `equals` | `vipEnMinusculasRecibeDescuento` |
| CP-13 | Descuentos | Tipo desconocido **(Ae6)** | PREMIUM, 100 | 100 | Alternativo | Aplicar un descuento por defecto | `tipoDesconocidoNoRecibeDescuento` |
| CP-14 | Descuentos | Tipo nulo **(Ae6)** | null, 100 | 100, sin excepción | Inválido | NullPointerException al refactorizar la comparación | `tipoNuloNoRecibeDescuento` |

## 3. Confirmación — `confirmar(Reserva)` con Stub y Mock
Regla: una reserva disponible se confirma, se guarda y se notifica; si no hay disponibilidad o la reserva es nula, el flujo se detiene.

| ID | Regla | Escenario | Entrada | Esperado | Tipo | Riesgo que protege | Prueba |
|---|---|---|---|---|---|---|---|
| CM-01 | Confirmación | Disponible | R-001 + Stub true | CONFIRMADA; guardar y notificar 1 vez | Normal | Reserva no persistida o cliente sin aviso | `reservaDisponibleSeConfirmaGuardaYNotifica` |
| CM-02 | Confirmación | No disponible | R-002 + Stub false | IllegalStateException; sigue PENDIENTE; nunca guarda ni notifica | Excepción | Confirmar un horario ocupado | `reservaNoDisponibleNoSeGuardaNiNotifica` |
| CM-03 | Confirmación | Reserva nula | null | IllegalArgumentException; ninguna dependencia consultada | Inválido | Llamadas externas con datos inválidos | `reservaNulaNoConsultaDependencias` |
| CM-04 | Confirmación | Orden del flujo | R-003 + Stub true | Consulta → guarda → notifica | Interacción | Notificar una reserva no guardada | `reservaDisponibleSeGuardaAntesDeNotificar` |
| CM-05 | Confirmación | Estado al guardar **(Ae6)** | R-004 + Stub true | El repositorio recibe la reserva ya CONFIRMADA | Interacción | Persistir la reserva como PENDIENTE | `reservaSeGuardaYaConfirmada` |

## 4. Entidad — `Reserva`

| ID | Regla | Escenario | Entrada | Esperado | Tipo | Riesgo que protege | Prueba |
|---|---|---|---|---|---|---|---|
| RD-01 | Reserva | Id nulo | null, NORMAL | IllegalArgumentException "Id obligatorio" | Inválido | Reservas sin identificador | `idNuloEsRechazado` |
| RD-02 | Reserva | Id en blanco | "   ", NORMAL | IllegalArgumentException "Id obligatorio" | Inválido | Aceptar ids vacíos | `idEnBlancoEsRechazado` |
| RD-03 | Reserva | Tipo nulo | R-010, null | Tipo NORMAL, estado PENDIENTE | Alternativo | Tipo nulo en el cálculo de descuentos | `tipoNuloSeAsumeNormal` |
| RD-04 | Reserva | Cancelar | R-011 | Estado CANCELADA | Normal | Transición de estado rota | `cancelarCambiaEstadoACancelada` |

**Total: 24 casos** → Cancelación 6 · Descuentos 9 · Confirmación 5 · Entidad 4.
Por tipo: Normal 5 · Alternativo 5 · Límite 5 · Inválido 5 · Excepción 2 · Interacción 2.
