package edu.uees.testing.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas añadidas a partir del reporte JaCoCo (Laboratorio 2, paso 11).
 *
 * El reporte mostró que las validaciones del constructor de Reserva y la
 * operación cancelar() no eran ejecutadas por ninguna prueba. Son reglas del
 * dominio: sin estas pruebas, podrían eliminarse sin que la suite lo detecte.
 * Reserva es una entidad simple, por eso se prueba directamente, sin mocks.
 */
@DisplayName("Reserva - validaciones y estados del dominio")
class ReservaTest {

    @Test
    @DisplayName("RD-01 | Id nulo es rechazado")
    void idNuloEsRechazado() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Reserva(null, "NORMAL"));

        assertEquals("Id obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("RD-02 | Id en blanco es rechazado")
    void idEnBlancoEsRechazado() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Reserva("   ", "NORMAL"));

        assertEquals("Id obligatorio", ex.getMessage());
    }

    @Test
    @DisplayName("RD-03 | Tipo nulo se asume NORMAL y la reserva inicia PENDIENTE")
    void tipoNuloSeAsumeNormal() {
        // Act
        Reserva reserva = new Reserva("R-010", null);

        // Assert
        assertEquals("R-010", reserva.getId());
        assertEquals("NORMAL", reserva.getTipo());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    @DisplayName("RD-04 | cancelar() cambia el estado a CANCELADA")
    void cancelarCambiaEstadoACancelada() {
        // Arrange
        Reserva reserva = new Reserva("R-011", "VIP");

        // Act
        reserva.cancelar();

        // Assert
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }
}
