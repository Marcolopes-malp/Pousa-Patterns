package br.com.commandfactory.controller;

import dao.HospedeDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;

public class CadastraClienteAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String nome = request.getParameter("txtNome");
            String cpf = request.getParameter("txtCpf");
            String email = request.getParameter("txtEmail");
            String telefone = request.getParameter("txtTelefone");
            String cidade = request.getParameter("txtCidade");
            String senha = request.getParameter("txtSenha");
            String redirect = request.getParameter("redirect");

            HospedeDAO dao = new HospedeDAO();
            Hospede existente = dao.buscarPorEmail(email);
            if (existente != null) {
                request.setAttribute("msg", "Este e-mail já está cadastrado. Faça login.");
                request.setAttribute("tipoMsg", "warning");
                return "login.jsp";
            }

            Hospede novo = new Hospede(nome, cpf, email, telefone, cidade, senha);
            int id = dao.cadastrar(novo);
            novo.setId(id);

            // S5: Rotação de ID de sessão e limpeza de credencial
            request.changeSessionId();
            novo.setSenha(null);

            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogado", novo);
            session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());

            if (util.Seguranca.isRedirectSeguro(redirect)) {
                response.sendRedirect(redirect.trim());
                return null;
            }
            response.sendRedirect("controller.do?btnop=MinhasReservas");
            return null;

        } catch (Exception e) {
            java.util.logging.Logger.getLogger(CadastraClienteAction.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Erro ao realizar cadastro de cliente", e);
            String msg = (e instanceof IllegalArgumentException)
                    ? e.getMessage()
                    : "Não foi possível concluir o cadastro no momento. Por favor, tente novamente.";
            request.setAttribute("msg", msg);
            request.setAttribute("tipoMsg", "danger");
            return "cadastro.jsp";
        }
    }
}
