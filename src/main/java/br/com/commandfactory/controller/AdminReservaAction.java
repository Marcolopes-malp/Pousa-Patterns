package br.com.commandfactory.controller;

import dao.ReservaDAO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;
import model.Reserva;

/**
 * Comando para carregar o Painel de Gestão da Recepção (Admin).
 * Acesso restrito a usuários com perfil 'RECEPCAO'.
 */
public class AdminReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            response.sendRedirect("controller.do?btnop=Login&redirect=" + java.net.URLEncoder.encode("controller.do?btnop=Admin", "UTF-8"));
            return null;
        }

        if (!usuario.isRecepcao()) {
            request.setAttribute("msg", "Acesso restrito: o painel administrativo é exclusivo da equipe de recepção.");
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }

        ReservaDAO dao = new ReservaDAO();
        List<Reserva> lista = dao.consultarTodos();
        request.setAttribute("listaReservas", lista);
        return "admin.jsp";
    }
}
