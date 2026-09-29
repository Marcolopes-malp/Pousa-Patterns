package dao;

import java.sql.SQLException;
import java.util.UUID;
import model.Acomodacao;
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
 * Testes unitários para validação de vínculo de acomodação e controle de disponibilidade sobreposta (Tarefa B6).
 */
public class AcomodacaoDisponibilidadeTest {

    private static ReservaDAO reservaDAO;
    private static AcomodacaoDAO acomodacaoDAO;

    @BeforeAll
    public static void setUp() throws Exception {
        try (java.sql.Connection con = FabricaConexao.getConexao()) {
            // Garante inicialização das tabelas e seeds
        }
        reservaDAO = new ReservaDAO();
        acomodacaoDAO = new AcomodacaoDAO();
    }

    @Test
    @DisplayName("B6: Detecta sobreposição quando intervalo coincide com reserva confirmada existente")
    public void testDetectaSobreposicaoDentroDoPeriodo() throws SQLException, ClassNotFoundException {
        // Reserva 1 existente: acomodacao_id = 1, checkIn = 2026-10-10, checkOut = 2026-10-15
        int sobreposicoes = reservaDAO.contarReservasSobrepostas(1, "2026-10-12", "2026-10-14", 0);
        assertTrue(sobreposicoes >= 1, "Deve identificar pelo menos 1 sobreposição no meio da estadia");
    }

    @Test
    @DisplayName("B6: Não detecta sobreposição quando check-in coincide com check-out anterior (turnover de quarto)")
    public void testNaoDetectaSobreposicaoNoMesmoDiaDeCheckOut() throws SQLException, ClassNotFoundException {
        // Reserva 1 existente: checkOut = 2026-10-15. Novo hóspede entra em 2026-10-15 e sai em 2026-10-18
        int sobreposicoes = reservaDAO.contarReservasSobrepostas(1, "2026-10-15", "2026-10-18", 0);
        assertEquals(0, sobreposicoes, "Check-in no mesmo dia do check-out não deve ser considerado conflito");

        // Novo hóspede sai em 2026-10-10 antes do check-in da Reserva 1
        int sobreposicoesAntes = reservaDAO.contarReservasSobrepostas(1, "2026-10-05", "2026-10-10", 0);
        assertEquals(0, sobreposicoesAntes, "Check-out no mesmo dia do check-in não deve ser considerado conflito");
    }

    @Test
    @DisplayName("B6: Ignora a própria reserva durante edição para não acusar falso conflito consigo mesma")
    public void testIgnoraPropriaReservaNaEdicao() throws SQLException, ClassNotFoundException {
        // Ao editar a reserva 1 (id=1) no mesmo período, ela não deve conflitar consigo mesma
        int sobreposicoes = reservaDAO.contarReservasSobrepostas(1, "2026-10-10", "2026-10-15", 1);
        assertEquals(0, sobreposicoes, "A própria reserva sendo editada deve ser desconsiderada da contagem");
    }

    @Test
    @DisplayName("B6: Persiste e carrega acomodacaoId e objeto Acomodacao corretamente no ReservaDAO")
    public void testPersistenciaECarregamentoAcomodacao() throws SQLException, ClassNotFoundException {
        Acomodacao acom = acomodacaoDAO.buscarPorId(2);
        assertNotNull(acom);

        String localizador = "TEST-B6-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Hospede hospede = new Hospede("Teste B6", "111.222.333-44", "teste.b6@email.com", "(11) 99999-0000", "São Paulo - SP");

        Reserva reserva = ReservaBuilder.novo()
                .comCodigoLocalizador(localizador)
                .comHospede(hospede)
                .comAcomodacao(acom)
                .comPeriodo("2026-11-20", "2026-11-25")
                .comQuantidadeHospedes(2)
                .comValorDiaria(acom.getValorDiaria())
                .comValorTotal(acom.getValorDiaria() * 5)
                .comFormaPagamento("PIX")
                .comStatus("CONFIRMADA")
                .constroi();

        int id = reservaDAO.cadastrar(reserva);
        assertTrue(id > 0);

        Reserva recuperada = reservaDAO.consultarById(id);
        assertNotNull(recuperada);
        assertEquals(2, recuperada.getAcomodacaoId());
        assertNotNull(recuperada.getAcomodacao());
        assertEquals("Suíte Master com Hidro & Lareira", recuperada.getAcomodacao().getNome());

        // Limpeza
        reservaDAO.deletar(id);
    }
}
