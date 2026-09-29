package br.com.commandfactory.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutClienteAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Logout deve ser acionado exclusivamente via POST.");
            return null;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionToken = (String) session.getAttribute("csrfToken");
            String requestToken = request.getParameter("csrfToken");
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF inválido ou expirado.");
                return null;
            }
            session.removeAttribute("usuarioLogado");
            session.invalidate();
        }
        response.sendRedirect("controller.do?btnop=ConsultaTodos");
        return null;
    }
}
