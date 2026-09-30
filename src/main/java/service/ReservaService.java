package service;

import dao.HospedeDAO;
import dao.ReservaDAO;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import model.Hospede;
import model.Reserva;
import model.StatusReserva;
import model.strategy.CalculadoraPreco;
import model.strategy.ResultadoCalculoPreco;
import util.Seguranca;

/**
 * Camada de Serviço (Service Layer) para gerenciamento de Reservas.
 * Centraliza e encapsula as regras de negócio de cadastro, check-in e atualização (Tarefa A3).
 */
public class ReservaService {

    private final ReservaDAO reservaDAO;
    private final HospedeDAO hospedeDAO;
    private final CalculadoraPreco calculadoraPreco;

    public ReservaService() {
        this.reservaDAO = new ReservaDAO();
        this.hospedeDAO = new HospedeDAO();
        this.calculadoraPreco = new CalculadoraPreco();
    }

    public ReservaService(ReservaDAO reservaDAO, HospedeDAO hospedeDAO, CalculadoraPreco calculadoraPreco) {
        this.reservaDAO = reservaDAO;
        this.hospedeDAO = hospedeDAO;
        this.calculadoraPreco = calculadoraPreco;
    }

    /**
     * Cadastra uma nova reserva validando disponibilidade de vagas no período.
     */
    public Reserva cadastrar(Reserva reserva) throws SQLException, ClassNotFoundException {
        if (reserva == null) {
            throw new IllegalArgumentException("Dados da reserva não fornecidos.");
        }

        if (reserva.getAcomodacaoId() > 0 && reserva.getAcomodacao() != null) {
            int sobrepostas = reservaDAO.contarReservasSobrepostas(
                    reserva.getAcomodacaoId(),
                    reserva.getDataCheckIn(),
                    reserva.getDataCheckOut(),
                    0
            );
            if (sobrepostas >= reserva.getAcomodacao().getVagasRestantes()) {
                throw new IllegalStateException("A acomodação '" + reserva.getAcomodacao().getNome() +
                        "' não possui vagas suficientes para o período de " +
                        reserva.getDataCheckIn() + " a " + reserva.getDataCheckOut() + ".");
            }
        }

        int id = reservaDAO.cadastrar(reserva);
        reserva.setId(id);
        return reserva;
    }

    /**
     * Executa a regra de negócio do Check-In com validação de status e geração de PIN.
     */
    public ResultadoCheckIn checkIn(int reservaId, Hospede usuarioLogado) throws SQLException, ClassNotFoundException {
        if (usuarioLogado == null) {
            throw new SecurityException("Usuário deve estar autenticado para realizar check-in.");
        }

        Reserva reserva = reservaDAO.consultarById(reservaId);
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva #" + reservaId + " não encontrada para processar check-in.");
        }

        // Controle de Acesso: apenas recepção ou o próprio titular
        if (!usuarioLogado.isRecepcao() && (reserva.getHospede() == null || reserva.getHospede().getId() != usuarioLogado.getId())) {
            throw new SecurityException("Acesso não autorizado: você só pode realizar o check-in de suas próprias reservas.");
        }

        StatusReserva statusAtual = StatusReserva.fromString(reserva.getStatus());

        // Se o check-in já estiver ativo, retorna aviso sem alterar PIN
        if (statusAtual == StatusReserva.CHECKIN_ATIVO) {
            return new ResultadoCheckIn(true, reserva, null, 0);
        }

        if (statusAtual == StatusReserva.CANCELADA) {
            throw new IllegalStateException("Não é possível realizar check-in: a reserva #" + reservaId + " está cancelada.");
        }

        if (statusAtual == StatusReserva.FINALIZADA) {
            throw new IllegalStateException("Não é possível realizar check-in: a reserva #" + reservaId + " já foi finalizada.");
        }

        if (!statusAtual.podeFazerCheckIn()) {
            throw new IllegalStateException("Check-in não permitido para reservas com status '" + statusAtual.getDescricao() + "'.");
        }

        // Validação temporal
        LocalDate hoje = LocalDate.now();
        LocalDate in = LocalDate.parse(reserva.getDataCheckIn());
        LocalDate out = LocalDate.parse(reserva.getDataCheckOut());

        if (!usuarioLogado.isRecepcao() && hoje.isBefore(in.minusDays(1))) {
            throw new IllegalStateException("O check-in antecipado online fica disponível a partir da véspera da data de entrada (" + in.minusDays(1) + ").");
        }

        if (hoje.isAfter(out)) {
            throw new IllegalStateException("Não é possível realizar check-in: o período desta reserva encerrou em " + out + ".");
        }

        long totalDias = ChronoUnit.DAYS.between(in, out);
        if (totalDias <= 0) totalDias = 1;

        ResultadoCalculoPreco calculo = calculadoraPreco.calcular(
                totalDias,
                reserva.getValorDiaria(),
                reserva.getServicos(),
                reserva.getFormaPagamento()
        );

        int pin = Seguranca.gerarPinFechaduraSeguro();
        reserva.setStatus(StatusReserva.CHECKIN_ATIVO.name());

        String obsOriginal = (reserva.getObservacoes() != null) ? reserva.getObservacoes() : "";
        obsOriginal = obsOriginal.replaceAll("\\[Check-In.*?\\]", "").trim();

        String pinTag = "[Check-In realizado. PIN Smart-Lock: " + pin + "]";
        String novaObs = obsOriginal.isEmpty() ? pinTag : obsOriginal + " " + pinTag;
        if (novaObs.length() > 480) {
            novaObs = novaObs.substring(0, 480);
        }
        reserva.setObservacoes(novaObs);

        reservaDAO.atualizar(reserva);

        return new ResultadoCheckIn(false, reserva, calculo, pin);
    }

    /**
     * Atualiza os dados de uma reserva existente e opcionalmente valida e atualiza o hóspede.
     */
    public void atualizar(Reserva reserva, Hospede hospedeAtualizado) throws SQLException, ClassNotFoundException {
        if (reserva == null) {
            throw new IllegalArgumentException("Reserva não fornecida para atualização.");
        }

        if (hospedeAtualizado != null && hospedeAtualizado.getId() > 0) {
            String email = hospedeAtualizado.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                Hospede outro = hospedeDAO.buscarPorEmail(email.trim());
                if (outro != null && outro.getId() != hospedeAtualizado.getId()) {
                    throw new IllegalArgumentException("O e-mail '" + email.trim() + "' já está cadastrado para outro hóspede.");
                }
            }
            hospedeDAO.atualizar(hospedeAtualizado);
        }

        // Validação de disponibilidade de vagas caso o status não seja cancelada
        if (reserva.getAcomodacaoId() > 0 && !"CANCELADA".equalsIgnoreCase(reserva.getStatus()) && reserva.getAcomodacao() != null) {
            int sobrepostas = reservaDAO.contarReservasSobrepostas(
                    reserva.getAcomodacaoId(),
                    reserva.getDataCheckIn(),
                    reserva.getDataCheckOut(),
                    reserva.getId()
            );
            if (sobrepostas >= reserva.getAcomodacao().getVagasRestantes()) {
                throw new IllegalStateException("Desculpe, a acomodação '" + reserva.getAcomodacao().getNome() +
                        "' não possui vagas suficientes para o período informado.");
            }
        }

        reservaDAO.atualizar(reserva);
    }

    public Reserva consultarPorId(int id) throws SQLException, ClassNotFoundException {
        return reservaDAO.consultarById(id);
    }

    public List<Reserva> listarTodas() throws SQLException, ClassNotFoundException {
        return reservaDAO.consultarTodos();
    }

    public List<Reserva> listarPorHospede(int hospedeId) throws SQLException, ClassNotFoundException {
        return reservaDAO.listarPorHospede(hospedeId);
    }

    public void deletar(int id) throws SQLException, ClassNotFoundException {
        reservaDAO.deletar(id);
    }

    /**
     * DTO de resultado do processamento de Check-in.
     */
    public static class ResultadoCheckIn {
        private final boolean jaRealizado;
        private final Reserva reserva;
        private final ResultadoCalculoPreco calculo;
        private final int pin;

        public ResultadoCheckIn(boolean jaRealizado, Reserva reserva, ResultadoCalculoPreco calculo, int pin) {
            this.jaRealizado = jaRealizado;
            this.reserva = reserva;
            this.calculo = calculo;
            this.pin = pin;
        }

        public boolean isJaRealizado() {
            return jaRealizado;
        }

        public Reserva getReserva() {
            return reserva;
        }

        public ResultadoCalculoPreco getCalculo() {
            return calculo;
        }

        public int getPin() {
            return pin;
        }
    }
}
