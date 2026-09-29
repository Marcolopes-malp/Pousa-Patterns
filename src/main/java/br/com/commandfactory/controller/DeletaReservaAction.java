package br.com.commandfactory.controller;

import dao.ReservaDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Comando para Deletar uma Reserva por ID.
 */
public class DeletaReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            ReservaDAO dao = new ReservaDAO();
            dao.deletar(id);

            request.setAttribute("msg", "Reserva #" + id + " excluída com sucesso.");
            request.setAttribute("tipoMsg", "success");
            return "resultado.jsp";
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("msg", "Erro ao excluir reserva: " + e.getMessage());
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
