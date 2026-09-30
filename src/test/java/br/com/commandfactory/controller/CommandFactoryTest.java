package br.com.commandfactory.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para a CommandFactory (Tarefa A2).
 */
public class CommandFactoryTest {

    @Test
    @DisplayName("A2: Cria comandos registrados corretamente (case-insensitive)")
    public void testCriarComandosRegistrados() {
        ICommand cmdConsulta = CommandFactory.criarComando("ConsultaTodos");
        assertNotNull(cmdConsulta);
        assertInstanceOf(ConsultaTodosReservaAction.class, cmdConsulta);

        ICommand cmdCadastra = CommandFactory.criarComando("cadastra");
        assertNotNull(cmdCadastra);
        assertInstanceOf(CadastraReservaAction.class, cmdCadastra);

        ICommand cmdAdmin = CommandFactory.criarComando("ADMIN");
        assertNotNull(cmdAdmin);
        assertInstanceOf(AdminReservaAction.class, cmdAdmin);
    }

    @Test
    @DisplayName("A2: Ação padrão para parâmetro nulo ou vazio é ConsultaTodos")
    public void testAcaoPadrao() {
        ICommand cmdNull = CommandFactory.criarComando(null);
        assertNotNull(cmdNull);
        assertInstanceOf(ConsultaTodosReservaAction.class, cmdNull);

        ICommand cmdVazio = CommandFactory.criarComando("   ");
        assertNotNull(cmdVazio);
        assertInstanceOf(ConsultaTodosReservaAction.class, cmdVazio);
    }

    @Test
    @DisplayName("A2: Retorna null para comando desconhecido (para o Front Controller responder 404)")
    public void testComandoDesconhecidoRetornaNull() {
        ICommand cmdInexistente = CommandFactory.criarComando("acaoInexistenteXYZ");
        assertNull(cmdInexistente, "Comando desconhecido deve retornar null sem lançar exceção reflexiva");
        assertFalse(CommandFactory.existeComando("acaoInexistenteXYZ"));
    }

    @Test
    @DisplayName("A2: existeComando valida existência correta dos nomes")
    public void testExisteComando() {
        assertTrue(CommandFactory.existeComando("minhasreservas"));
        assertTrue(CommandFactory.existeComando("ProcessarCheckInAutomatico"));
        assertFalse(CommandFactory.existeComando("comando_falso"));
        assertFalse(CommandFactory.existeComando(null));
    }
}
