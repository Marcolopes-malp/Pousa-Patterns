package br.com.commandfactory.controller;

import dao.ReservaDAO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Reserva;

/**
 * Comando para Consultar Todas as Reservas cadastradas.
 */
public class ConsultaTodosReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            ReservaDAO dao = new ReservaDAO();
            List<Reserva> lista = dao.consultarTodos();
            request.setAttribute("listaReservas", lista);
            return "index.jsp";
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("msg", "Erro ao listar reservas: " + e.getMessage());
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
