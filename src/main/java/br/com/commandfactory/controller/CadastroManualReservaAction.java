package br.com.commandfactory.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;

/**
 * Comando para exibir o formulário de cadastro manual de reservas.
 * Acesso exclusivo da recepção.
 */
public class CadastroManualReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();
        Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            response.sendRedirect("controller.do?btnop=Login&redirect=" + java.net.URLEncoder.encode("controller.do?btnop=CadastroManual", "UTF-8"));
            return null;
        }

        if (!usuario.isRecepcao()) {
            request.setAttribute("msg", "Acesso restrito: o cadastro manual de reservas é exclusivo da equipe de recepção.");
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }

        return "formCadastro.jsp";
    }
}
