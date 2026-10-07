package br.com.commandfactory.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;
import model.Reserva;
import service.ReservaService;

public class MinhasReservasAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            response.sendRedirect("controller.do?btnop=Login&redirect=" + java.net.URLEncoder.encode("controller.do?btnop=MinhasReservas", "UTF-8"));
            return null;
        }

        ReservaService reservaService = new ReservaService();
        List<Reserva> lista = reservaService.listarPorHospede(usuario.getId());
        request.setAttribute("listaMinhasReservas", lista);

        return "minhasReservas.jsp";
    }
}
