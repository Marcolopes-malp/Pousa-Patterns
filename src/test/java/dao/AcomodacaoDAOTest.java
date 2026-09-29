package dao;

import java.util.Optional;
import model.Acomodacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para AcomodacaoDAO (Tarefa B5).
 * Valida que buscarPorId retorna null ou Optional vazio para IDs inexistentes,
 * em vez de retornar silenciosamente a primeira acomodação do catálogo.
 */
public class AcomodacaoDAOTest {

    private AcomodacaoDAO dao;

    @BeforeEach
    public void setUp() {
        dao = new AcomodacaoDAO();
    }

    @Test
    @DisplayName("B5: ID existente retorna a acomodação correspondente")
    public void testBuscarPorIdExistente() {
        Acomodacao ac = dao.buscarPorId(1);
        assertNotNull(ac);
        assertEquals(1, ac.getId());
        assertEquals("Bangalô Vista Mar & Deck Privativo", ac.getNome());

        Optional<Acomodacao> opt = dao.buscarPorIdOptional(1);
        assertTrue(opt.isPresent());
        assertEquals("Bangalô Vista Mar & Deck Privativo", opt.get().getNome());
    }

    @Test
    @DisplayName("B5: ID inexistente retorna null e Optional vazio (sem valor padrão silencioso)")
    public void testBuscarPorIdInexistenteRetornaNullEOptionalVazio() {
        Acomodacao ac = dao.buscarPorId(9999);
        assertNull(ac, "Deve retornar null para ID inexistente, não a primeira acomodação");

        Optional<Acomodacao> opt = dao.buscarPorIdOptional(9999);
        assertFalse(opt.isPresent(), "Optional deve estar vazio para ID inexistente");
    }
}
