package br.com.commandfactory.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutClienteAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("usuarioLogado");
            session.invalidate();
        }
        response.sendRedirect("controller.do?btnop=ConsultaTodos");
        return null;
    }
}
