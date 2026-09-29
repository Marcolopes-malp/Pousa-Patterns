package br.com.commandfactory.controller;

import dao.ReservaDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;
import model.ItemServico;
import model.Reserva;

/**
 * REQUISITO OBRIGATÓRIO: Automação de Processo de Negócio.
 * Executa o fluxo automatizado de Check-In e Fechamento Financeiro Inteligente:
 * 1. Validação de período e cálculo automático de noites de hospedagem.
 * 2. Aplicação dinâmica de desconto por tempo de estadia (Fidelidade / Long-Stay):
 *    - A partir de 7 dias: 15% de desconto nas diárias.
 *    - A partir de 4 dias: 10% de desconto nas diárias.
 * 3. Totalização automatizada dos serviços adicionais vinculados (1:N).
 * 4. Aplicação automática de Taxa de Preservação Ambiental da Pousada (3%).
 * 5. Geração de PIN digital de acesso aos quartos (smart-lock).
 * 6. Atualização automática do status da reserva para 'CHECKIN_ATIVO'.
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

            // 1. Cálculo automatizado de diárias
            LocalDate in = LocalDate.parse(reserva.getDataCheckIn());
            LocalDate out = LocalDate.parse(reserva.getDataCheckOut());
            long totalDias = ChronoUnit.DAYS.between(in, out);
            if (totalDias <= 0) {
                totalDias = 1;
            }

            // B1: Aplicação do Padrão STRATEGY para garantir cálculo unificado entre reserva e check-in
            model.strategy.CalculadoraPreco calculadora = new model.strategy.CalculadoraPreco();
            model.strategy.ResultadoCalculoPreco calculo = calculadora.calcular(
                    totalDias,
                    reserva.getValorDiaria(),
                    reserva.getServicos(),
                    reserva.getFormaPagamento()
            );

            // 2. Geração do PIN Smart-Lock com gerador criptograficamente seguro (CSPRNG)
            int pin = util.Seguranca.gerarPinFechaduraSeguro();

            // 3. Atualização de status e observações (preservando o valor total contratado)
            reserva.setStatus("CHECKIN_ATIVO");
            String novaObs = (reserva.getObservacoes() != null ? reserva.getObservacoes() : "")
                    + " [Check-In Automatizado realizado. PIN da fechadura digital: " + pin
                    + " | Desconto estadia: " + (int)(calculo.getPercentualDescontoEstadia() * 100) + "%].";
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
