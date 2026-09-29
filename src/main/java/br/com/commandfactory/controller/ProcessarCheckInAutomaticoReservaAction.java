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

            double subtotalDiarias = totalDias * reserva.getValorDiaria();

            // 2. Regra de Desconto Automatizado (Long-Stay)
            double percentualDesconto = 0.0;
            if (totalDias >= 7) {
                percentualDesconto = 0.15; // 15%
            } else if (totalDias >= 4) {
                percentualDesconto = 0.10; // 10%
            }
            double valorDesconto = subtotalDiarias * percentualDesconto;
            double subtotalComDesconto = subtotalDiarias - valorDesconto;

            // 3. Totalização automatizada dos Serviços Adicionais (1:N)
            double totalServicos = 0.0;
            if (reserva.getServicos() != null) {
                for (ItemServico s : reserva.getServicos()) {
                    totalServicos += s.getSubtotal();
                }
            }

            // 4. Taxa de Preservação Ambiental (3%)
            double taxaAmbiental = (subtotalComDesconto + totalServicos) * 0.03;

            // Total Consolidado
            double novoTotal = subtotalComDesconto + totalServicos + taxaAmbiental;

            // 5. Geração do PIN Smart-Lock
            int pin = (int) (1000 + Math.random() * 9000);

            // 6. Atualização de status e observações
            reserva.setStatus("CHECKIN_ATIVO");
            reserva.setValorTotal(Math.round(novoTotal * 100.0) / 100.0);
            String novaObs = (reserva.getObservacoes() != null ? reserva.getObservacoes() : "")
                    + " [Check-In Automatizado realizado. PIN da fechadura digital: " + pin
                    + " | Desconto aplicado: " + (percentualDesconto * 100) + "%].";
            reserva.setObservacoes(novaObs);

            // Persiste no banco de dados via DAO
            dao.atualizar(reserva);

            request.setAttribute("reserva", reserva);
            request.setAttribute("totalDias", totalDias);
            request.setAttribute("subtotalDiarias", subtotalDiarias);
            request.setAttribute("valorDesconto", valorDesconto);
            request.setAttribute("totalServicos", totalServicos);
            request.setAttribute("taxaAmbiental", taxaAmbiental);
            request.setAttribute("pinAcesso", pin);
            request.setAttribute("msg", "Processo de Check-In Automatizado concluído com sucesso!");
            request.setAttribute("tipoMsg", "success");

            return "detalhesReserva.jsp";
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("msg", "Erro no processamento automatizado: " + e.getMessage());
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
