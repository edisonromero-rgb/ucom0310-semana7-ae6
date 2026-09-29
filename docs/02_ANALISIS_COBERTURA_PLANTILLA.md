# Análisis de cobertura

Reporte analizado: `target/site/jacoco/index.html` (JaCoCo 0.8.12), generado con `mvn clean test`.

## Resultado observado

| Momento | Pruebas | ReservaService (líneas / ramas) | Reserva (líneas / ramas) | Total proyecto (instr. / ramas) |
|---|---|---|---|---|
| Solo Laboratorio 1 | 11 | 13/21 (62 %) · 8/12 (67 %) | 0/14 (0 %) · 0/6 (0 %) | 33 % · 44 % |
| + Stub y Mock (`ReservaServiceConfirmacionTest`) | 15 | 21/21 (100 %) · 12/12 (100 %) | 9/14 (64 %) · 3/6 (50 %) | 87 % · 83 % |
| + Pruebas derivadas del reporte (`ReservaTest`) | 19 | 21/21 (100 %) · 12/12 (100 %) | 14/14 (100 %) · 6/6 (100 %) | 100 % · 100 % |

- Cobertura de líneas: ReservaService 100 %; Reserva pasó de 64 % a 100 %.
- Cobertura de ramas: ReservaService 100 %; Reserva pasó de 50 % a 100 %.
- Clase o método analizado: `ReservaService.confirmar(Reserva)` y la entidad `Reserva`, que el método usa.

## Huecos relevantes

1. **Validaciones de `Reserva` sin ejecutar (hueco de líneas y ramas).** El constructor tenía en amarillo la condición `id == null || id.isBlank()` y en amarillo `tipo == null ? "NORMAL" : tipo`; en rojo, el `throw` de "Id obligatorio" y los métodos `getId()`, `getTipo()` y `cancelar()`. El comportamiento que faltaba no era "una línea", sino tres reglas del dominio: una reserva sin identificador debe rechazarse, un tipo ausente se asume NORMAL y una reserva puede pasar a CANCELADA. Sin pruebas, cualquiera de esas reglas podía borrarse y la suite seguía en verde.
2. **Código cubierto pero mal protegido.** `ReservaService` marca 100 % y aun así tiene comportamientos sin aserción:
   - *Orden de las interacciones.* Las pruebas de la guía verifican que se guarde y se notifique, pero no en qué orden. Si alguien notifica antes de guardar, la cobertura sigue en 100 % y el caso 1 sigue pasando. Se protegió con CM-04 (`InOrder`); el microexperimento lo confirmó (solo CM-04 falla con ese defecto).
   - *Mayúsculas y minúsculas en `calcularTotal`.* La regla usa `equalsIgnoreCase`, pero ninguna prueba envía "vip" en minúsculas. Cambiar a `equals` deja la cobertura en 100 % y las 19 pruebas en verde. Queda como mejora propuesta: `vipEnMinusculasRecibeDescuento()` con entrada ("vip", 100) → 85.

## Decisiones

- **¿Qué prueba nueva se añadió?** `ReservaTest` con 4 casos: id nulo rechazado (RD-01), id en blanco rechazado (RD-02), tipo nulo se asume NORMAL e inicia PENDIENTE (RD-03) y `cancelar()` deja la reserva CANCELADA (RD-04). También se reforzó el caso sin disponibilidad para comprobar que la reserva queda PENDIENTE y se agregó CM-04 para el orden de interacciones. `Reserva` es una entidad simple: se prueba directamente, sin mocks.
- **¿Qué riesgo protege?** Que se acepten reservas sin identificador (datos inválidos persistidos), que una reserva sin tipo quede con un tipo nulo y falle el cálculo de descuentos, que el estado de cancelación deje de funcionar y que se notifique al cliente una reserva que no llegó a guardarse. El microexperimento de quitar `isBlank()` confirmó que RD-02 lo detecta.
- **¿Por qué no basta con el porcentaje?** Porque la cobertura solo dice qué código se ejecutó, no qué se comprobó. `ReservaService` ya estaba en 100 % y aun así el cambio de orden de guardado/notificación y el cambio de `equalsIgnoreCase` a `equals` pasaban inadvertidos. Una prueba que ejecuta una línea sin una aserción relevante sube el porcentaje sin proteger la regla. El porcentaje sirve para encontrar zonas no ejecutadas; la calidad la dan las aserciones y la elección de casos.
