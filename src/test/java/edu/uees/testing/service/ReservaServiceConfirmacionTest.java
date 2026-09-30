package edu.uees.testing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.uees.testing.availability.DisponibilidadClient;
import edu.uees.testing.domain.EstadoReserva;
import edu.uees.testing.domain.Reserva;
import edu.uees.testing.notification.Notificador;
import edu.uees.testing.repository.ReservaRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * Laboratorio 2 | Stub, Mock, JaCoCo y Git — UCOM0310, Semana 7.
 *
 * Pruebas de ReservaService.confirmar(Reserva), que depende de tres colaboradores.
 * Ninguna prueba usa una base de datos, un servicio externo ni un servidor de correo.
 *
 * Rol de cada doble de prueba:
 *   - disponibilidad (STUB): solo se le programa una respuesta con
 *     when(...).thenReturn(...) para decidir qué camino recorre el servicio.
 *   - repository y notificador (MOCKS): no devuelven nada útil; se usan para
 *     comprobar con verify(...) / never() que la interacción ocurrió o no ocurrió.
 */
@DisplayName("ReservaService.confirmar - dobles de prueba con Mockito")
class ReservaServiceConfirmacionTest {

    private DisponibilidadClient disponibilidad; // Stub: controla la respuesta
    private ReservaRepository repository;        // Mock: verifica el guardado
    private Notificador notificador;             // Mock: verifica la notificación
    private ReservaService servicio;

    @BeforeEach
    void prepararServicioConDobles() {
        disponibilidad = mock(DisponibilidadClient.class);
        repository = mock(ReservaRepository.class);
        notificador = mock(Notificador.class);
        servicio = new ReservaService(disponibilidad, repository, notificador);
    }

    @Test
    @DisplayName("CM-01 | Reserva disponible: se confirma, se guarda y se notifica")
    void reservaDisponibleSeConfirmaGuardaYNotifica() {
        // Arrange (Stub): el horario está disponible
        when(disponibilidad.estaDisponible(any())).thenReturn(true);
        Reserva reserva = new Reserva("R-001", "NORMAL");

        // Act
        servicio.confirmar(reserva);

        // Assert: estado + interacciones con los Mocks
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        verify(repository).guardar(reserva);
        verify(notificador).enviarConfirmacion(reserva);
    }

    @Test
    @DisplayName("CM-02 | Sin disponibilidad: lanza excepción, no guarda ni notifica")
    void reservaNoDisponibleNoSeGuardaNiNotifica() {
        // Arrange (Stub): el horario NO está disponible
        when(disponibilidad.estaDisponible(any())).thenReturn(false);
        Reserva reserva = new Reserva("R-002", "NORMAL");

        // Act
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> servicio.confirmar(reserva));

        // Assert
        assertEquals("Horario no disponible", ex.getMessage());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado(),
                "Una reserva rechazada no debe quedar confirmada");
        verify(repository, never()).guardar(any());
        verify(notificador, never()).enviarConfirmacion(any());
    }

    @Test
    @DisplayName("CM-03 | Reserva nula: se detiene antes de consultar cualquier dependencia")
    void reservaNulaNoConsultaDependencias() {
        // Arrange: no se programa el Stub; no debería llegar a usarse

        // Act
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.confirmar(null));

        // Assert
        assertEquals("Reserva obligatoria", ex.getMessage());
        verify(disponibilidad, never()).estaDisponible(any());
        verify(repository, never()).guardar(any());
        verify(notificador, never()).enviarConfirmacion(any());
    }

    @Test
    @DisplayName("CM-04 | Reserva disponible: consulta, guarda y recién después notifica")
    void reservaDisponibleSeGuardaAntesDeNotificar() {
        // Arrange
        when(disponibilidad.estaDisponible(any())).thenReturn(true);
        Reserva reserva = new Reserva("R-003", "VIP");

        // Act
        servicio.confirmar(reserva);

        // Assert: el orden importa; notificar una reserva no guardada sería un defecto
        InOrder orden = inOrder(disponibilidad, repository, notificador);
        orden.verify(disponibilidad).estaDisponible(reserva);
        orden.verify(repository).guardar(reserva);
        orden.verify(notificador).enviarConfirmacion(reserva);
    }

    @Test
    @DisplayName("CM-05 | La reserva ya está CONFIRMADA en el momento de guardarse")
    void reservaSeGuardaYaConfirmada() {
        // Arrange: el Mock del repositorio registra el estado que recibe al guardar
        when(disponibilidad.estaDisponible(any())).thenReturn(true);
        List<EstadoReserva> estadoAlGuardar = new ArrayList<>();
        doAnswer(invocacion -> {
            Reserva guardada = invocacion.getArgument(0);
            estadoAlGuardar.add(guardada.getEstado());
            return null;
        }).when(repository).guardar(any());
        Reserva reserva = new Reserva("R-004", "ESTUDIANTE");

        // Act
        servicio.confirmar(reserva);

        // Assert: se persistió una sola vez y ya confirmada, no PENDIENTE
        assertEquals(List.of(EstadoReserva.CONFIRMADA), estadoAlGuardar);
    }
}
