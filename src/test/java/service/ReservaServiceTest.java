package service;

import dao.HospedeDAO;
import dao.ReservaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import model.Hospede;
import model.Reserva;
import model.ReservaBuilder;
import model.StatusReserva;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.FabricaConexao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários para a Camada de Serviço ReservaService (Tarefa A3).
 */
public class ReservaServiceTest {

    private static ReservaService service;
    private static ReservaDAO reservaDAO;
    private static HospedeDAO hospedeDAO;

    @BeforeAll
    public static void setUp() throws Exception {
        try (var con = FabricaConexao.getConexao()) {
            // Inicializa tabelas
        }
        service = new ReservaService();
        reservaDAO = new ReservaDAO();
        hospedeDAO = new HospedeDAO();
    }

    @Test
    @DisplayName("A3: Check-in via ReservaService realiza transição para CHECKIN_ATIVO e gera PIN")
    public void testCheckInFluxoSucesso() throws SQLException, ClassNotFoundException {
        String email = "service." + UUID.randomUUID().toString().substring(0, 6) + "@email.com";
        Hospede h = new Hospede("Cliente Service", "111.333.555-77", email, "(11) 98765-1122", "Mogi - SP");
        int hid = hospedeDAO.cadastrar(h);
        h.setId(hid);

        String in = LocalDate.now().toString();
        String out = LocalDate.now().plusDays(3).toString();
        String loc = "LOC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Reserva r = ReservaBuilder.novo()
                .comCodigoLocalizador(loc)
                .comHospede(h)
                .comPeriodo(in, out)
                .comQuantidadeHospedes(2)
                .comTipoQuarto("Bangalô Vista Mar & Deck Privativo")
                .comValorDiaria(450.0)
                .comValorTotal(1350.0)
                .comFormaPagamento("PIX")
                .comStatus(StatusReserva.CONFIRMADA.name())
                .constroi();
        int rid = reservaDAO.cadastrar(r);
        r.setId(rid);

        // Executa check-in pelo titular
        ReservaService.ResultadoCheckIn res = service.checkIn(rid, h);
        assertNotNull(res);
        assertTrue(!res.isJaRealizado());
        assertEquals(StatusReserva.CHECKIN_ATIVO.name(), res.getReserva().getStatus());
        assertTrue(res.getPin() >= 1000 && res.getPin() <= 9999);

        // Segundo check-in idempotente
        ReservaService.ResultadoCheckIn res2 = service.checkIn(rid, h);
        assertTrue(res2.isJaRealizado(), "Segundo check-in deve acusar já realizado sem quebrar");

        // Limpeza
        reservaDAO.deletar(rid);
        hospedeDAO.deletar(hid);
    }

    @Test
    @DisplayName("A3: Check-in rejeita reserva cancelada com IllegalStateException")
    public void testCheckInReservaCancelada() throws SQLException, ClassNotFoundException {
        String email = "service.canc." + UUID.randomUUID().toString().substring(0, 6) + "@email.com";
        Hospede h = new Hospede("Cliente Cancelado", "222.444.666-88", email, "(11) 98765-3344", "São Paulo - SP");
        int hid = hospedeDAO.cadastrar(h);
        h.setId(hid);

        String loc = "LOC-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Reserva r = ReservaBuilder.novo()
                .comCodigoLocalizador(loc)
                .comHospede(h)
                .comPeriodo(LocalDate.now().toString(), LocalDate.now().plusDays(2).toString())
                .comQuantidadeHospedes(1)
                .comTipoQuarto("Suíte Standard Jardim Colonial")
                .comValorDiaria(250.0)
                .comValorTotal(500.0)
                .comFormaPagamento("PIX")
                .comStatus(StatusReserva.CANCELADA.name())
                .constroi();
        int rid = reservaDAO.cadastrar(r);
        r.setId(rid);

        assertThrows(IllegalStateException.class, () -> service.checkIn(rid, h));

        // Limpeza
        reservaDAO.deletar(rid);
        hospedeDAO.deletar(hid);
    }
}
