package br.com.commandfactory.controller;

import dao.ReservaDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;
import model.Reserva;
import model.StatusReserva;
import model.strategy.CalculadoraPreco;
import model.strategy.ResultadoCalculoPreco;
import util.Seguranca;

/**
 * REQUISITO OBRIGATÓRIO: Automação de Processo de Negócio.
 * Executa o fluxo automatizado de Check-In e Fechamento Financeiro:
 * 1. Validação de segurança, autenticação e controle de acesso (S2).
 * 2. Validação rigorosa de máquina de estados da Reserva (B2):
 *    - Rejeita check-in de reservas CANCELADA, FINALIZADA e PENDENTE.
 *    - Idempotência: caso a reserva já esteja em CHECKIN_ATIVO, devolve aviso sem alterar dados.
 * 3. Validação temporal de datas (disponível a partir da véspera do check-in até o check-out).
 * 4. Preserva o valor monetário contratado sem reajustes retroativos (B1).
 * 5. Geração de PIN criptograficamente seguro (CSPRNG) e atualização de status para 'CHECKIN_ATIVO'.
 */
public class ProcessarCheckInAutomaticoReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Check-in deve ser acionado exclusivamente via POST.");
                return null;
            }

            HttpSession session = request.getSession();
            String sessionToken = (String) session.getAttribute("csrfToken");
            String requestToken = request.getParameter("csrfToken");
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF inválido ou expirado.");
                return null;
            }

            Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                response.sendRedirect("controller.do?btnop=Login");
                return null;
            }

            int id = Integer.parseInt(request.getParameter("id"));
            ReservaDAO dao = new ReservaDAO();
            Reserva reserva = dao.consultarById(id);

            if (reserva == null) {
                request.setAttribute("msg", "Reserva #" + id + " não encontrada para processar check-in.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            // S2: Controle de Acesso - apenas recepção ou o próprio titular podem fazer check-in
            if (!usuario.isRecepcao() && (reserva.getHospede() == null || reserva.getHospede().getId() != usuario.getId())) {
                request.setAttribute("msg", "Acesso não autorizado: você só pode realizar o check-in de suas próprias reservas.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            // B2: Validação de Estado (Ciclo de Vida da Reserva via StatusReserva)
            StatusReserva statusAtual = StatusReserva.fromString(reserva.getStatus());

            if (statusAtual == StatusReserva.CHECKIN_ATIVO) {
                request.setAttribute("reserva", reserva);
                request.setAttribute("msg", "O check-in desta reserva já foi realizado anteriormente. Seu PIN de acesso e acomodação já estão liberados.");
                request.setAttribute("tipoMsg", "warning");
                return "detalhesReserva.jsp";
            }

            if (statusAtual == StatusReserva.CANCELADA) {
                request.setAttribute("msg", "Não é possível realizar check-in: a reserva #" + id + " está cancelada.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            if (statusAtual == StatusReserva.FINALIZADA) {
                request.setAttribute("msg", "Não é possível realizar check-in: a reserva #" + id + " já foi finalizada.");
                request.setAttribute("tipoMsg", "warning");
                return "resultado.jsp";
            }

            if (!statusAtual.podeFazerCheckIn()) {
                request.setAttribute("msg", "Check-in não permitido: reserva com status '" + statusAtual.getDescricao() + "'.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            // B2: Validação Temporal de Datas
            LocalDate hoje = LocalDate.now();
            LocalDate in = LocalDate.parse(reserva.getDataCheckIn());
            LocalDate out = LocalDate.parse(reserva.getDataCheckOut());

            // Clientes podem antecipar check-in online a partir da véspera da entrada
            if (!usuario.isRecepcao() && hoje.isBefore(in.minusDays(1))) {
                request.setAttribute("msg", "O check-in antecipado online fica disponível a partir da véspera da data de entrada (" + in.minusDays(1) + ").");
                request.setAttribute("tipoMsg", "warning");
                return "resultado.jsp";
            }

            if (hoje.isAfter(out)) {
                request.setAttribute("msg", "Não é possível realizar check-in: o período desta reserva encerrou em " + out + ".");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            long totalDias = ChronoUnit.DAYS.between(in, out);
            if (totalDias <= 0) {
                totalDias = 1;
            }

            // B1: Cálculos para exibição de detalhamento na view
            CalculadoraPreco calculadora = new CalculadoraPreco();
            ResultadoCalculoPreco calculo = calculadora.calcular(
                    totalDias,
                    reserva.getValorDiaria(),
                    reserva.getServicos(),
                    reserva.getFormaPagamento()
            );

            // B2 / S7: Geração do PIN Smart-Lock com gerador criptograficamente seguro (CSPRNG)
            int pin = Seguranca.gerarPinFechaduraSeguro();

            // B2: Atualização segura de status e observações (evita concatenação repetida que estoura VARCHAR)
            reserva.setStatus(StatusReserva.CHECKIN_ATIVO.name());

            String obsOriginal = (reserva.getObservacoes() != null) ? reserva.getObservacoes() : "";
            obsOriginal = obsOriginal.replaceAll("\\[Check-In.*?\\]", "").trim();

            String pinTag = "[Check-In realizado. PIN Smart-Lock: " + pin + "]";
            String novaObs = obsOriginal.isEmpty() ? pinTag : obsOriginal + " " + pinTag;
            if (novaObs.length() > 480) {
                novaObs = novaObs.substring(0, 480);
            }
            reserva.setObservacoes(novaObs);

            // Persiste no banco de dados via DAO
            dao.atualizar(reserva);

            request.setAttribute("reserva", reserva);
            request.setAttribute("totalDias", calculo.getNoites());
            request.setAttribute("subtotalDiarias", calculo.getSubtotalDiarias());
            request.setAttribute("valorDesconto", calculo.getValorDescontoEstadia());
            request.setAttribute("totalServicos", calculo.getTotalServicos());
            request.setAttribute("taxaAmbiental", calculo.getTaxaAmbiental());
            request.setAttribute("valorDescontoPix", calculo.getValorDescontoPix());
            request.setAttribute("pinAcesso", pin);
            request.setAttribute("msg", "Processo de Check-In Automatizado concluído com sucesso!");
            request.setAttribute("tipoMsg", "success");

            return "detalhesReserva.jsp";
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(ProcessarCheckInAutomaticoReservaAction.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Erro ao processar check-in automatizado", e);
            String msg = (e instanceof IllegalArgumentException)
                    ? e.getMessage()
                    : "Não foi possível concluir o check-in online no momento. Por favor, tente novamente ou procure a recepção.";
            request.setAttribute("msg", msg);
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
