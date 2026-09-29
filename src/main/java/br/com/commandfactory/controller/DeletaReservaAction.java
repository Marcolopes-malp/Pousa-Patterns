package br.com.commandfactory.controller;

import dao.ReservaDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;

/**
 * Comando para Deletar uma Reserva por ID.
 * Exclusivo para perfis com permissão de RECEPCAO, acionado apenas via POST e protegido por CSRF.
 */
public class DeletaReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            // S2: Rejeitar requisições GET
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Ação de exclusão permitida apenas via POST.");
                return null;
            }

            // S2: Validação de Token CSRF
            HttpSession session = request.getSession();
            String sessionToken = (String) session.getAttribute("csrfToken");
            String requestToken = request.getParameter("csrfToken");
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF inválido ou expirado.");
                return null;
            }

            // S2: Controle de Acesso - Exclusivo para RECEPCAO
            Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                response.sendRedirect("controller.do?btnop=Login");
                return null;
            }
            if (!usuario.isRecepcao()) {
                request.setAttribute("msg", "Acesso negado: a exclusão de reservas é restrita à equipe de recepção.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            int id = Integer.parseInt(request.getParameter("id"));
            ReservaDAO dao = new ReservaDAO();
            dao.deletar(id);

            request.setAttribute("msg", "Reserva #" + id + " excluída com sucesso.");
            request.setAttribute("tipoMsg", "success");
            return "resultado.jsp";
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(DeletaReservaAction.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Erro ao excluir reserva", e);
            request.setAttribute("msg", "Não foi possível excluir a reserva no momento. Por favor, tente novamente.");
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
