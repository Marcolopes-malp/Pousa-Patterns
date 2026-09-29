package br.com.commandfactory.controller;

import dao.ReservaDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;
import model.Reserva;

/**
 * Comando para carregar os dados de uma Reserva e abrir a tela de edição.
 * Exclusivo para perfis com permissão de RECEPCAO.
 */
public class EditaReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            HttpSession session = request.getSession();
            Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");

            if (usuario == null) {
                response.sendRedirect("controller.do?btnop=Login");
                return null;
            }

            if (!usuario.isRecepcao()) {
                request.setAttribute("msg", "Acesso negado: a edição de reservas é restrita à equipe de recepção.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            int id = Integer.parseInt(request.getParameter("id"));
            ReservaDAO dao = new ReservaDAO();
            Reserva reserva = dao.consultarById(id);

            if (reserva != null) {
                request.setAttribute("reserva", reserva);
                request.setAttribute("acomodacoes", new dao.AcomodacaoDAO().listarTodas());
                return "formEditar.jsp";
            } else {
                request.setAttribute("msg", "Reserva não encontrada para edição.");
                request.setAttribute("tipoMsg", "warning");
                return "resultado.jsp";
            }
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(EditaReservaAction.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Erro ao preparar edição da reserva", e);
            request.setAttribute("msg", "Não foi possível carregar os dados para edição no momento.");
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
