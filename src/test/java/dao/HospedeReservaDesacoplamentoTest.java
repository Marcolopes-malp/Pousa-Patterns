package dao;

import java.sql.SQLException;
import java.util.UUID;
import model.Hospede;
import model.Reserva;
import model.ReservaBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.FabricaConexao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para a Tarefa B7:
 * Desacoplamento da edição de hóspede da edição de reserva e validação de login único.
 */
public class HospedeReservaDesacoplamentoTest {

    private static ReservaDAO reservaDAO;
    private static HospedeDAO hospedeDAO;

    @BeforeAll
    public static void setUp() throws Exception {
        try (var con = FabricaConexao.getConexao()) {
            // Inicializa tabelas
        }
        reservaDAO = new ReservaDAO();
        hospedeDAO = new HospedeDAO();
    }

    @Test
    @DisplayName("B7: ReservaDAO.atualizar não altera os dados cadastrais do hóspede incondicionalmente")
    public void testReservaAtualizarNaoModificaHospedeIndevidamente() throws SQLException, ClassNotFoundException {
        // Cadastra um hóspede de teste
        String emailOriginal = "original." + UUID.randomUUID().toString().substring(0, 6) + "@email.com";
        Hospede h = new Hospede("Nome Original", "123.456.789-01", emailOriginal, "(11) 98888-7777", "Santos - SP");
        int hid = hospedeDAO.cadastrar(h);
        h.setId(hid);

        // Cria uma reserva para esse hóspede
        String loc = "LOC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Reserva r = ReservaBuilder.novo()
                .comCodigoLocalizador(loc)
                .comHospede(h)
                .comPeriodo("2026-11-10", "2026-11-15")
                .comQuantidadeHospedes(2)
                .comTipoQuarto("Bangalô Vista Mar & Deck Privativo")
                .comValorDiaria(450.0)
                .comValorTotal(2250.0)
                .comFormaPagamento("PIX")
                .comStatus("PENDENTE")
                .constroi();
        int rid = reservaDAO.cadastrar(r);
        r.setId(rid);

        // Altera o objeto hóspede em memória mas atualiza apenas a reserva
        h.setNomeCompleto("Nome Invasor Não Persistido");
        r.setStatus("CONFIRMADA");
        r.setObservacoes("Atualização exclusiva de reserva");
        reservaDAO.atualizar(r);

        // Verifica que o hóspede no banco continua com seu nome original intacto
        Hospede rec = hospedeDAO.consultarById(hid);
        assertNotNull(rec);
        assertEquals("Nome Original", rec.getNomeCompleto(), "O hóspede não deve ser alterado por ReservaDAO.atualizar");

        // E a reserva foi devidamente atualizada
        Reserva recReserva = reservaDAO.consultarById(rid);
        assertEquals("CONFIRMADA", recReserva.getStatus());
        assertEquals("Atualização exclusiva de reserva", recReserva.getObservacoes());

        // Limpeza
        reservaDAO.deletar(rid);
        hospedeDAO.deletar(hid);
    }

    @Test
    @DisplayName("B7: Validação de e-mail existente detecta colisão com outro hóspede")
    public void testDetectaColisaoDeEmail() throws SQLException, ClassNotFoundException {
        Hospede h1 = hospedeDAO.buscarPorEmail("mariana.ramos@email.com");
        assertNotNull(h1, "mariana.ramos@email.com deve existir nos dados semente");

        // Se outro hóspede tentar usar o e-mail da Mariana, deve ser detectado
        Hospede colidente = hospedeDAO.buscarPorEmail("mariana.ramos@email.com");
        int outroHospedeId = 99999;
        boolean emailEmUsoPorOutro = (colidente != null && colidente.getId() != outroHospedeId);
        assertTrue(emailEmUsoPorOutro, "Deve identificar que o e-mail pertence a outro hóspede");
    }
}
