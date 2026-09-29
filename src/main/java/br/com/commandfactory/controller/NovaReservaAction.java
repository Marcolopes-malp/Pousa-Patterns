package br.com.commandfactory.controller;

import dao.AcomodacaoDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Acomodacao;

public class NovaReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int id = 1;
        try {
            id = Integer.parseInt(request.getParameter("acomodacaoId"));
        } catch (Exception ignored) {}

        AcomodacaoDAO dao = new AcomodacaoDAO();
        Acomodacao acomodacao = dao.buscarPorId(id);
        request.setAttribute("acomodacao", acomodacao);

        return "reserva.jsp";
    }
}
