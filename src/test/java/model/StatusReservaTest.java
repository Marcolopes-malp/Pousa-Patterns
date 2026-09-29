package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para o ciclo de vida e máquina de estados da Reserva (Tarefa B2).
 */
public class StatusReservaTest {

    @Test
    @DisplayName("B2: Apenas reservas CONFIRMADA podem realizar check-in")
    public void testApenasConfirmadaPodeFazerCheckIn() {
        assertTrue(StatusReserva.CONFIRMADA.podeFazerCheckIn(), "Status CONFIRMADA deve permitir check-in");
        assertFalse(StatusReserva.PENDENTE.podeFazerCheckIn(), "Status PENDENTE não deve permitir check-in");
        assertFalse(StatusReserva.CHECKIN_ATIVO.podeFazerCheckIn(), "Status CHECKIN_ATIVO não deve permitir repetição de check-in");
        assertFalse(StatusReserva.CANCELADA.podeFazerCheckIn(), "Status CANCELADA não deve permitir check-in");
        assertFalse(StatusReserva.FINALIZADA.podeFazerCheckIn(), "Status FINALIZADA não deve permitir check-in");
    }

    @Test
    @DisplayName("B2: Validação de transição de cancelamento")
    public void testRegrasCancelamento() {
        assertTrue(StatusReserva.PENDENTE.podeCancelar(), "PENDENTE pode ser cancelada");
        assertTrue(StatusReserva.CONFIRMADA.podeCancelar(), "CONFIRMADA pode ser cancelada");
        assertFalse(StatusReserva.CHECKIN_ATIVO.podeCancelar(), "CHECKIN_ATIVO não pode ser cancelada diretamente");
        assertFalse(StatusReserva.FINALIZADA.podeCancelar(), "FINALIZADA não pode ser cancelada");
        assertFalse(StatusReserva.CANCELADA.podeCancelar(), "Já cancelada");
    }

    @Test
    @DisplayName("B2: Validação de finalização (check-out)")
    public void testRegrasFinalizacao() {
        assertTrue(StatusReserva.CHECKIN_ATIVO.podeFinalizar(), "CHECKIN_ATIVO pode ser finalizada");
        assertFalse(StatusReserva.CONFIRMADA.podeFinalizar(), "CONFIRMADA ainda não entrou");
        assertFalse(StatusReserva.PENDENTE.podeFinalizar(), "PENDENTE não pode ser finalizada");
        assertFalse(StatusReserva.CANCELADA.podeFinalizar(), "CANCELADA não pode ser finalizada");
    }

    @Test
    @DisplayName("B2: Parse seguro de StatusReserva a partir de string")
    public void testFromStringSeguro() {
        assertEquals(StatusReserva.CONFIRMADA, StatusReserva.fromString("CONFIRMADA"));
        assertEquals(StatusReserva.CONFIRMADA, StatusReserva.fromString("confirmada"));
        assertEquals(StatusReserva.CHECKIN_ATIVO, StatusReserva.fromString("CHECKIN_ATIVO"));
        assertEquals(StatusReserva.CANCELADA, StatusReserva.fromString("CANCELADA"));
        assertEquals(StatusReserva.PENDENTE, StatusReserva.fromString(null));
        assertEquals(StatusReserva.PENDENTE, StatusReserva.fromString("   "));
        assertEquals(StatusReserva.PENDENTE, StatusReserva.fromString("DESCONHECIDO"));
    }
}
