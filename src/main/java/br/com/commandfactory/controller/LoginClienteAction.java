package br.com.commandfactory.controller;

import dao.HospedeDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;

public class LoginClienteAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String email = request.getParameter("txtEmail");
        String senha = request.getParameter("txtSenha");
        String redirect = request.getParameter("redirect");

        HospedeDAO dao = new HospedeDAO();
        Hospede hospede = dao.autenticar(email, senha);

        if (hospede != null) {
            // S5: Rotação de ID de sessão para mitigar Session Fixation
            request.changeSessionId();

            boolean isRecepcao = hospede.isRecepcao();
            // S5: Limpa a credencial do objeto antes de vinculá-lo à sessão HTTP
            hospede.setSenha(null);

            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogado", hospede);
            session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());

            if (util.Seguranca.isRedirectSeguro(redirect)) {
                response.sendRedirect(redirect.trim());
                return null;
            }
            if (isRecepcao) {
                response.sendRedirect("controller.do?btnop=Admin");
            } else {
                response.sendRedirect("controller.do?btnop=MinhasReservas");
            }
            return null;
        }

        request.setAttribute("msg", "E-mail ou senha incorretos. Verifique suas credenciais.");
        request.setAttribute("tipoMsg", "danger");
        return "login.jsp";
    }
}
