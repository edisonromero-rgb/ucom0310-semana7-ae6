# Análisis de cobertura — Ae6

Reporte analizado: `target/site/jacoco/index.html` (JaCoCo 0.8.12), generado con `mvn clean test` en la rama `ae6/suite-pruebas`.

## Resultado observado

| Momento | Pruebas | ReservaService (líneas · ramas) | Reserva (líneas · ramas) | Total (instrucciones · ramas) |
|---|---|---|---|---|
| Laboratorio 1 | 11 | 13/21 (62 %) · 8/12 (66 %) | 0/14 · 0/6 (0 %) | 33 % · 44 % |
| Laboratorio 2 (Stub y Mock) | 15 | 21/21 · 12/12 (100 %) | 9/14 (64 %) · 3/6 (50 %) | 87 % · 83 % |
| Laboratorio 2 (+ ReservaTest) | 19 | 21/21 · 12/12 (100 %) | 14/14 · 6/6 (100 %) | 100 % · 100 % |
| **Ae6 (final)** | **24** | **21/21 · 12/12 (100 %)** | **14/14 · 6/6 (100 %)** | **100 % · 100 %** |

- Métodos con menor cobertura en el punto de partida: `confirmar()` (0 % con la suite del Laboratorio 1) y, dentro de `Reserva`, el constructor (3 de 6 ramas), `getId()`, `getTipo()` y `cancelar()`. Ambos huecos se cerraron en el Laboratorio 2.
- En Ae6 la cobertura ya era de 100 %, así que el análisis se centró en lo que el porcentaje no muestra: comportamientos ejecutados pero sin una aserción que los proteja.

## Huecos relevantes

1. **Estado de la reserva al guardarse (`confirmar`).** Las líneas `reserva.confirmar()` y `repository.guardar(reserva)` están en verde, pero ninguna prueba comprobaba en qué estado llega la reserva al repositorio. Si alguien invierte esas dos líneas, se guardaría una reserva PENDIENTE y la suite del Laboratorio 2 seguiría en verde, porque CM-01 revisa el estado solo al final. Se verificó con un microexperimento: 0 fallos con la suite anterior.
2. **Comparación del tipo de cliente (`calcularTotal`).** La rama `"VIP".equalsIgnoreCase(tipo)` aparece cubierta, pero ninguna prueba enviaba el tipo en minúsculas. JaCoCo cuenta la rama como ejecutada tanto si se usa `equalsIgnoreCase` como `equals`: la cobertura no distingue entre ambas implementaciones.
3. **Entradas fuera del contrato.** No había casos para un tipo desconocido, un tipo nulo ni horas negativas. El código los resuelve por la rama por defecto, que ya estaba cubierta por NORMAL, así que el porcentaje no reflejaba la ausencia de estos escenarios.

## Decisiones

- **Pruebas añadidas a partir del análisis:** CM-05 (`reservaSeGuardaYaConfirmada`), que usa el Mock del repositorio para registrar el estado recibido al guardar; CP-12 (`vipEnMinusculasRecibeDescuento`); CP-13 (tipo desconocido); CP-14 (tipo nulo, sin excepción) y CP-15 (horas negativas).
- **Riesgo que protegen y verificación:** se introdujeron tres defectos temporales y cada uno fue detectado solo por la prueba nueva correspondiente: invertir `confirmar()` y `guardar()` → falla CM-05; `equalsIgnoreCase` → `equals` → falla CP-12; comparar con `tipo.equalsIgnoreCase("VIP")` → NullPointerException en CP-14. Después de cada experimento se restauró el código.
- **Por qué el porcentaje no basta:** la cobertura mide qué código se ejecutó, no qué se verificó. Pasar de 19 a 24 pruebas no cambió ninguna métrica de JaCoCo (100 % antes y después) y, sin embargo, la suite ahora detecta tres defectos que antes pasaban inadvertidos. Una suite puede tener 100 % de cobertura y aun así aceptar código incorrecto.

## Limitaciones

- CP-15 documenta el comportamiento actual (horas negativas → `false`) pero la regla de negocio no define si debería lanzarse una excepción. Se propone modelar la anticipación como Value Object que rechace valores negativos.
- CP-14 caracteriza que un tipo nulo no recibe descuento; falta confirmar con negocio si debería rechazarse.
- Las pruebas son unitarias: no validan la integración real con base de datos, servicio de disponibilidad ni correo.
