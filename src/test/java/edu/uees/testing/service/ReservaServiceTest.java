package edu.uees.testing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Laboratorio 1 | Diseño de casos y JUnit 5 — UCOM0310, Semana 7.
 *
 * Pruebas unitarias de las reglas puras de ReservaService:
 *   - puedeCancelar(int): se permite con 2 o más horas de anticipación.
 *   - calcularTotal(String, double): VIP 15 %, ESTUDIANTE 10 %, NORMAL sin descuento;
 *     un total base negativo es inválido.
 *
 * Nota: se usa null para los colaboradores únicamente porque estos dos métodos
 * no consultan dependencias. En el Laboratorio 2 se reemplazará por Stub y Mock.
 */
@DisplayName("ReservaService - reglas de cancelación y descuentos")
class ReservaServiceTest {

    private static final double DELTA = 0.001;

    private final ReservaService servicio = new ReservaService(null, null, null);

    // ------------------------------------------------------------------
    // Regla de cancelación: puedeCancelar(int)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("CP-01 | 5 horas de anticipación permiten cancelar")
    void cincoHorasPermitenCancelar() {
        // Arrange
        int horas = 5;

        // Act
        boolean resultado = servicio.puedeCancelar(horas);

        // Assert
        assertTrue(resultado);
    }

    @Test
    @DisplayName("CP-02 | 2 horas es el límite permitido (frontera >= 2)")
    void dosHorasEsElLimitePermitido() {
        // Arrange
        int horas = 2;

        // Act
        boolean resultado = servicio.puedeCancelar(horas);

        // Assert
        assertTrue(resultado, "Con exactamente 2 horas la cancelación debe permitirse");
    }

    @Test
    @DisplayName("CP-03 | 1 hora no permite cancelar (justo debajo del límite)")
    void unaHoraNoPermiteCancelar() {
        // Arrange
        int horas = 1;

        // Act
        boolean resultado = servicio.puedeCancelar(horas);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("CP-04 | 0 horas no permite cancelación inmediata")
    void ceroHorasNoPermiteCancelar() {
        // Arrange
        int horas = 0;

        // Act
        boolean resultado = servicio.puedeCancelar(horas);

        // Assert
        assertFalse(resultado);
    }

    @Test
    @DisplayName("CP-10 | 3 horas permiten cancelar (justo encima del límite)")
    void tresHorasPermitenCancelar() {
        // Arrange
        int horas = 3;

        // Act
        boolean resultado = servicio.puedeCancelar(horas);

        // Assert
        assertTrue(resultado);
    }

    // ------------------------------------------------------------------
    // Regla de descuentos: calcularTotal(String, double)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("CP-05 | Cliente NORMAL no recibe descuento")
    void normalNoRecibeDescuento() {
        // Arrange
        String tipo = "NORMAL";
        double totalBase = 100;

        // Act
        double total = servicio.calcularTotal(tipo, totalBase);

        // Assert
        assertEquals(100.0, total, DELTA);
    }

    @Test
    @DisplayName("CP-06 | Cliente VIP recibe 15 % de descuento")
    void vipRecibeQuincePorCiento() {
        // Arrange
        String tipo = "VIP";
        double totalBase = 100;

        // Act
        double total = servicio.calcularTotal(tipo, totalBase);

        // Assert
        assertEquals(85.0, total, DELTA);
    }

    @Test
    @DisplayName("CP-07 | Cliente ESTUDIANTE recibe 10 % de descuento")
    void estudianteRecibeDiezPorCiento() {
        // Arrange
        String tipo = "ESTUDIANTE";
        double totalBase = 100;

        // Act
        double total = servicio.calcularTotal(tipo, totalBase);

        // Assert
        assertEquals(90.0, total, DELTA);
    }

    @Test
    @DisplayName("CP-08 | Total base cero es válido y devuelve cero")
    void totalCeroDevuelveCero() {
        // Arrange
        String tipo = "VIP";
        double totalBase = 0;

        // Act
        double total = servicio.calcularTotal(tipo, totalBase);

        // Assert
        assertEquals(0.0, total, DELTA);
    }

    @Test
    @DisplayName("CP-09 | Total base negativo lanza IllegalArgumentException")
    void totalNegativoEsInvalido() {
        // Arrange
        String tipo = "NORMAL";
        double totalBase = -1;

        // Act
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.calcularTotal(tipo, totalBase));

        // Assert
        assertEquals("Total base inválido", ex.getMessage());
    }

    @Test
    @DisplayName("CP-11 | VIP con monto decimal aplica 15 % con precisión")
    void vipConMontoDecimalAplicaDescuentoExacto() {
        // Arrange
        String tipo = "VIP";
        double totalBase = 59.99;

        // Act
        double total = servicio.calcularTotal(tipo, totalBase);

        // Assert
        assertEquals(50.9915, total, DELTA);
    }

    // ------------------------------------------------------------------
    // Ae6 | Casos agregados a partir del análisis de cobertura
    // (código cubierto por JaCoCo pero sin una aserción que lo proteja)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("CP-12 | Tipo 'vip' en minúsculas también recibe 15 %")
    void vipEnMinusculasRecibeDescuento() {
        // Arrange: la regla compara el tipo sin distinguir mayúsculas
        String tipo = "vip";
        double totalBase = 100;

        // Act
        double total = servicio.calcularTotal(tipo, totalBase);

        // Assert
        assertEquals(85.0, total, DELTA);
    }

    @Test
    @DisplayName("CP-13 | Tipo desconocido no recibe descuento")
    void tipoDesconocidoNoRecibeDescuento() {
        // Arrange
        String tipo = "PREMIUM";
        double totalBase = 100;

        // Act
        double total = servicio.calcularTotal(tipo, totalBase);

        // Assert
        assertEquals(100.0, total, DELTA);
    }

    @Test
    @DisplayName("CP-14 | Tipo nulo no lanza excepción y no recibe descuento")
    void tipoNuloNoRecibeDescuento() {
        // Arrange
        String tipo = null;
        double totalBase = 100;

        // Act
        double total = servicio.calcularTotal(tipo, totalBase);

        // Assert
        assertEquals(100.0, total, DELTA);
    }

    @Test
    @DisplayName("CP-15 | Horas negativas no permiten cancelar (comportamiento actual)")
    void horasNegativasNoPermitenCancelar() {
        // Arrange: valor inválido; hoy el servicio lo trata como "sin anticipación"
        int horas = -1;

        // Act
        boolean resultado = servicio.puedeCancelar(horas);

        // Assert
        assertFalse(resultado);
    }
}
