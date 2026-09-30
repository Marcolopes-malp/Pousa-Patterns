package controller;

import br.com.commandfactory.controller.ICommand;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Padrão de Projeto Arquitetural: FRONT CONTROLLER (Java EE MVC).
 * Servlet central que intercepta todas as requisições da aplicação,
 * extrai a ação requisitada e utiliza o padrão GoF FACTORY METHOD via CommandFactory
 * para despachar a execução ao COMMAND correspondente de forma segura e tipada (Tarefas A2 e C1).
 */
public class ManterReserva extends HttpServlet {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ManterReserva.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        try {
            // Garante geração de token CSRF para a sessão
            javax.servlet.http.HttpSession session = request.getSession();
            if (session.getAttribute("csrfToken") == null) {
                session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());
            }

            // Recupera a ação solicitada via botão de formulário ou parâmetro de URL
            String paramAction = request.getParameter("btnop");
            if (paramAction == null || paramAction.trim().isEmpty()) {
                paramAction = request.getParameter("acao");
            }
            if (paramAction == null || paramAction.trim().isEmpty()) {
                paramAction = request.getParameter("action");
            }
            if (paramAction == null || paramAction.trim().isEmpty()) {
                paramAction = "ConsultaTodos"; // Ação padrão
            }

            // S2: Ações de mutação de estado só aceitam POST
            java.util.Set<String> acoesMutantes = java.util.Set.of("Deleta", "ProcessarCheckInAutomatico", "Cadastra", "Atualiza", "LogoutCliente", "CadastraCliente", "LoginCliente");
            if (acoesMutantes.contains(paramAction) && !"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Operação permitida exclusivamente via POST.");
                return;
            }

            // S2: Controle de Acesso para ações restritas à RECEPCAO
            java.util.Set<String> acoesRecepcao = java.util.Set.of("Deleta", "Edita", "Atualiza", "Admin", "CadastroManual");
            if (acoesRecepcao.contains(paramAction)) {
                model.Hospede usuario = (model.Hospede) session.getAttribute("usuarioLogado");
                if (usuario == null) {
                    response.sendRedirect("controller.do?btnop=Login&redirect=" + java.net.URLEncoder.encode("controller.do?btnop=" + paramAction, "UTF-8"));
                    return;
                }
                if (!usuario.isRecepcao()) {
                    request.setAttribute("msg", "Acesso restrito: operação exclusiva da equipe de recepção.");
                    request.setAttribute("tipoMsg", "danger");
                    request.getRequestDispatcher("/WEB-INF/views/resultado.jsp").forward(request, response);
                    return;
                }
            }

            // Padrão GoF FACTORY METHOD (CommandFactory):
            // Obtém o Command correspondente via registro explícito seguro (A2)
            ICommand commandAction = br.com.commandfactory.controller.CommandFactory.criarComando(paramAction);
            if (commandAction == null) {
                LOGGER.log(java.util.logging.Level.WARNING, "Comando não reconhecido: {0}", paramAction);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Comando não encontrado: " + paramAction);
                return;
            }

            // Padrão COMMAND: Executa a ação e obtém a página JSP de destino
            String pageAction = commandAction.executar(request, response);

            // Redireciona internamente para a View (JSP em WEB-INF/views/) se não houve redirecionamento prévio
            if (pageAction != null && !pageAction.trim().isEmpty()) {
                if (!pageAction.startsWith("/WEB-INF/views/") && pageAction.endsWith(".jsp")) {
                    pageAction = "/WEB-INF/views/" + pageAction;
                }
                request.getRequestDispatcher(pageAction).forward(request, response);
            }

        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Erro interno no processamento da requisição", e);
            request.setAttribute("msg", "Ocorreu um erro interno ao processar a requisição. Por favor, tente novamente.");
            request.setAttribute("tipoMsg", "danger");
            request.getRequestDispatcher("/WEB-INF/views/resultado.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Front Controller de Reservas - Pousada";
    }
}
