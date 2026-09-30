package dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import model.Hospede;
import model.ItemServico;
import model.Reserva;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReservaDAOPersistenciaTest {

    private ReservaDAO reservaDAO;
    private HospedeDAO hospedeDAO;
    private List<Integer> reservasCriadas;

    @BeforeEach
    public void setup() {
        reservaDAO = new ReservaDAO();
        hospedeDAO = new HospedeDAO();
        reservasCriadas = new ArrayList<>();
    }

    @AfterEach
    public void cleanup() {
        for (int id : reservasCriadas) {
            try {
                reservaDAO.deletar(id);
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("D1 & D2: Deve cadastrar e consultar reserva com serviços adicionais sob transação e lote")
    public void deveCadastrarEConsultarReservaComServicosEmTransacao() throws Exception {
        Hospede hospede = new Hospede();
        hospede.setNomeCompleto("Teste ACID");
        hospede.setCpf("999.888.777-66");
        hospede.setEmail("acid." + UUID.randomUUID() + "@teste.com");
        hospede.setTelefone("(11) 98888-9999");
        hospede.setCidadeOrigem("Mogi das Cruzes - SP");

        Reserva reserva = new Reserva();
        reserva.setCodigoLocalizador("ACID-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reserva.setDataCheckIn(LocalDate.now().plusDays(10).toString());
        reserva.setDataCheckOut(LocalDate.now().plusDays(15).toString());
        reserva.setQuantidadeHospedes(2);
        reserva.setTipoQuarto("Bangalô Vista Mar & Deck Privativo");
        reserva.setAcomodacaoId(1);
        reserva.setValorDiaria(450.0);
        reserva.setValorTotal(2350.0);
        reserva.setStatus("CONFIRMADA");
        reserva.setFormaPagamento("PIX");
        reserva.setHospede(hospede);

        List<ItemServico> servicos = new ArrayList<>();
        ItemServico s1 = new ItemServico("Café da Manhã", "Buffet", 65.0, 5);
        ItemServico s2 = new ItemServico("Translado", "Executivo", 180.0, 1);
        servicos.add(s1);
        servicos.add(s2);
        reserva.setServicos(servicos);

        int id = reservaDAO.cadastrar(reserva);
        assertTrue(id > 0);
        reservasCriadas.add(id);

        // Testa consulta por ID
        Reserva recuperada = reservaDAO.consultarById(id);
        assertNotNull(recuperada);
        assertNotNull(recuperada.getHospede());
        assertEquals("Teste ACID", recuperada.getHospede().getNomeCompleto());
        assertEquals(2, recuperada.getServicos().size());

        // Testa consultarTodos (D1 - sem N+1)
        List<Reserva> todas = reservaDAO.consultarTodos();
        assertFalse(todas.isEmpty());
        Reserva encontradaNaLista = todas.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
        assertNotNull(encontradaNaLista);
        assertNotNull(encontradaNaLista.getHospede());
        assertEquals(2, encontradaNaLista.getServicos().size());
    }

    @Test
    @DisplayName("D2: Deve deletar atomicamente reserva e serviços vinculados sem deixar registros órfãos")
    public void deveDeletarAtomicamenteReservaEServicos() throws Exception {
        Hospede hospede = new Hospede();
        hospede.setNomeCompleto("Teste Delecao");
        hospede.setCpf("111.222.333-44");
        hospede.setEmail("del." + UUID.randomUUID() + "@teste.com");
        hospede.setTelefone("(11) 97777-6666");

        Reserva reserva = new Reserva();
        reserva.setCodigoLocalizador("DEL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        reserva.setDataCheckIn(LocalDate.now().plusDays(20).toString());
        reserva.setDataCheckOut(LocalDate.now().plusDays(22).toString());
        reserva.setQuantidadeHospedes(1);
        reserva.setTipoQuarto("Suíte Standard Jardim Colonial");
        reserva.setAcomodacaoId(4);
        reserva.setValorDiaria(250.0);
        reserva.setValorTotal(500.0);
        reserva.setStatus("CONFIRMADA");
        reserva.setFormaPagamento("DINHEIRO");
        reserva.setHospede(hospede);

        List<ItemServico> servicos = new ArrayList<>();
        servicos.add(new ItemServico("Massagem", "Relaxante", 150.0, 1));
        reserva.setServicos(servicos);

        int id = reservaDAO.cadastrar(reserva);
        assertNotNull(reservaDAO.consultarById(id));

        // Executa deleção atômica
        reservaDAO.deletar(id);

        assertNull(reservaDAO.consultarById(id));
        ItemServicoDAO itemDAO = new ItemServicoDAO();
        assertTrue(itemDAO.listarPorReserva(id).isEmpty());
    }

    @Test
    @DisplayName("D2: Deve executar rollback da transação caso ocorra falha ao salvar serviços, não persistindo a reserva")
    public void deveReverterTransacaoComRollbackSeServicoForInvalido() throws Exception {
        String localizador = "ROLLBACK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Hospede hospede = new Hospede();
        hospede.setNomeCompleto("Teste Rollback");
        hospede.setCpf("222.333.444-55");
        hospede.setEmail("rb." + UUID.randomUUID() + "@teste.com");
        hospede.setTelefone("(11) 96666-5555");

        Reserva reserva = new Reserva();
        reserva.setCodigoLocalizador(localizador);
        reserva.setDataCheckIn(LocalDate.now().plusDays(30).toString());
        reserva.setDataCheckOut(LocalDate.now().plusDays(35).toString());
        reserva.setQuantidadeHospedes(2);
        reserva.setTipoQuarto("Bangalô Vista Mar & Deck Privativo");
        reserva.setAcomodacaoId(1);
        reserva.setValorDiaria(450.0);
        reserva.setValorTotal(2250.0);
        reserva.setStatus("CONFIRMADA");
        reserva.setFormaPagamento("PIX");
        reserva.setHospede(hospede);

        // Serviço com nome nulo (viola restrição NOT NULL no banco de dados)
        List<ItemServico> servicos = new ArrayList<>();
        servicos.add(new ItemServico(null, "Serviço com nome nulo para forçar erro", 100.0, 1));
        reserva.setServicos(servicos);

        assertThrows(Exception.class, () -> {
            reservaDAO.cadastrar(reserva);
        });

        // Comprova que o rollback funcionou e nada foi gravado no banco de dados
        List<Reserva> todas = reservaDAO.consultarTodos();
        boolean existe = todas.stream().anyMatch(r -> localizador.equals(r.getCodigoLocalizador()));
        assertFalse(existe, "A reserva não deve existir no banco de dados após o rollback!");
    }
}

