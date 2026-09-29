package br.com.commandfactory.controller;

import dao.ReservaDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Reserva;

/**
 * Comando para Consultar uma Reserva por ID com todos os detalhes e serviços.
 */
public class ConsultaByIdReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            ReservaDAO dao = new ReservaDAO();
            Reserva reserva = dao.consultarById(id);

            if (reserva != null) {
                request.setAttribute("reserva", reserva);
                return "detalhesReserva.jsp";
            } else {
                request.setAttribute("msg", "Reserva não encontrada para o ID: " + id);
                request.setAttribute("tipoMsg", "warning");
                return "resultado.jsp";
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("msg", "Erro ao consultar reserva por ID: " + e.getMessage());
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
