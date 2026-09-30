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
            service.ReservaService service = new service.ReservaService();
            service.ReservaService.ResultadoCheckIn resultado = service.checkIn(id, usuario);

            if (resultado.isJaRealizado()) {
                request.setAttribute("reserva", resultado.getReserva());
                request.setAttribute("msg", "O check-in desta reserva já foi realizado anteriormente. Seu PIN de acesso e acomodação já estão liberados.");
                request.setAttribute("tipoMsg", "warning");
                return "detalhesReserva.jsp";
            }

            request.setAttribute("reserva", resultado.getReserva());
            ResultadoCalculoPreco calculo = resultado.getCalculo();
            if (calculo != null) {
                request.setAttribute("totalDias", calculo.getNoites());
                request.setAttribute("subtotalDiarias", calculo.getSubtotalDiarias());
                request.setAttribute("valorDesconto", calculo.getValorDescontoEstadia());
                request.setAttribute("totalServicos", calculo.getTotalServicos());
                request.setAttribute("taxaAmbiental", calculo.getTaxaAmbiental());
                request.setAttribute("valorDescontoPix", calculo.getValorDescontoPix());
            }
            request.setAttribute("pinAcesso", resultado.getPin());
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
