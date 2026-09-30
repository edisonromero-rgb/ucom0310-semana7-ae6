# test: completar suite, cobertura y evidencia de Ae6

## Objetivo
Proteger las reglas críticas del módulo de reservas (cancelación, descuentos y confirmación) con una suite JUnit 5 reproducible, y cerrar los huecos que la cobertura JaCoCo no mostraba a pesar de estar en 100 %.

## Cambios realizados
- `ReservaServiceTest`: 4 casos nuevos para tipos de cliente en minúsculas, desconocidos y nulos, y para horas negativas (CP-12 a CP-15).
- `ReservaServiceConfirmacionTest`: CM-05 verifica, con el Mock del repositorio, que la reserva ya está CONFIRMADA cuando se guarda.
- `docs/`: matriz de 24 casos, análisis de cobertura y esta descripción de PR.
- No se modificó código productivo.

## Casos de prueba
- Cancelación: 6 casos (normal, límites 1-2-3, cero y negativo).
- Descuentos: 9 casos (NORMAL, VIP, ESTUDIANTE, decimales, cero, negativo, minúsculas, desconocido, nulo).
- Confirmación con Stub y Mock: 5 casos (disponible, no disponible, nula, orden del flujo, estado al guardar).
- Entidad `Reserva`: 4 casos (id nulo, id en blanco, tipo nulo, cancelar).

`DisponibilidadClient` se usa como **Stub** (`when(...).thenReturn(...)`) para decidir el camino del flujo. `ReservaRepository` y `Notificador` se usan como **Mocks** (`verify`, `never`, `InOrder`, `doAnswer`) para comprobar que se guarda y se notifica solo cuando corresponde, en el orden correcto y con la reserva confirmada. `Reserva` se usa real, porque es una entidad simple.

## Cómo verificar
```bash
mvn clean test
```
Resultado esperado: `Tests run: 24, Failures: 0, Errors: 0, Skipped: 0` y `BUILD SUCCESS`.
Luego abrir `target/site/jacoco/index.html`.

## Cobertura
100 % de instrucciones, líneas y ramas en `ReservaService` y `Reserva` (ya lo estaba antes de Ae6). El valor de este PR no está en el porcentaje, sino en tres defectos que ahora se detectan: guardar la reserva antes de confirmarla (CM-05), comparar el tipo con `equals` (CP-12) y una comparación que no tolera `null` (CP-14). Detalle en `docs/02_ANALISIS_COBERTURA_PLANTILLA.md`.

## Limitaciones
- Horas negativas y tipo nulo se prueban como comportamiento actual; la regla de negocio no define si deben rechazarse.
- No hay pruebas de integración con base de datos, servicio de disponibilidad ni correo.

## Autorrevisión
- [x] Compila
- [x] Pruebas en verde (24/24)
- [x] Sin archivos accidentales (`target/` excluido por `.gitignore`)
- [x] Commits descriptivos
- [x] Documentación actualizada

## Uso de IA
Se utilizó Claude (Anthropic) como apoyo para proponer casos de prueba, redactar el código de las pruebas y la documentación. La ejecución de `mvn clean test`, los reportes JaCoCo, los microexperimentos, el historial Git y la revisión final se realizaron y verificaron en mi equipo.
