package model.factory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para ServicoFactory (Tarefa B5).
 * Valida que tipos válidos retornam suas respectivas fábricas e que tipos nulos
 * ou desconhecidos lançam IllegalArgumentException em vez de retornar café silenciosamente.
 */
public class ServicoFactoryTest {

    @Test
    @DisplayName("B5: Tipos válidos retornam suas respectivas fábricas concretas")
    public void testTiposValidos() {
        assertInstanceOf(CafeManhaFactory.class, ServicoFactory.obterFabrica("cafe"));
        assertInstanceOf(TransferAeroportoFactory.class, ServicoFactory.obterFabrica("transfer"));
        assertInstanceOf(PasseioBarcoFactory.class, ServicoFactory.obterFabrica("passeio"));
        assertInstanceOf(PasseioBarcoFactory.class, ServicoFactory.obterFabrica("barco"));
        assertInstanceOf(SpaRelaxanteFactory.class, ServicoFactory.obterFabrica("spa"));
        assertInstanceOf(SpaRelaxanteFactory.class, ServicoFactory.obterFabrica("massagem"));
    }

    @Test
    @DisplayName("B5: Tipo de serviço nulo lança IllegalArgumentException")
    public void testTipoNuloLancaExcecao() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ServicoFactory.obterFabrica(null);
        });
        assertTrue(ex.getMessage().contains("não informado"));
    }

    @Test
    @DisplayName("B5: Tipo de serviço em branco lança IllegalArgumentException")
    public void testTipoEmBrancoLancaExcecao() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ServicoFactory.obterFabrica("   ");
        });
        assertTrue(ex.getMessage().contains("não informado"));
    }

    @Test
    @DisplayName("B5: Tipo de serviço desconhecido lança IllegalArgumentException")
    public void testTipoDesconhecidoLancaExcecao() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ServicoFactory.obterFabrica("heliponto");
        });
        assertTrue(ex.getMessage().contains("desconhecido"));
    }
}
