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
 * extrai a ação requisitada e utiliza o padrão FACTORY METHOD via Reflection
 * para delegar ao COMMAND correspondente.
 * Conforme ensinado na Aula 07 do Prof. Wolley.
 */
@WebServlet(name = "ManterReserva", urlPatterns = {"/ManterReserva", "/controller.do"})
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
            java.util.Set<String> acoesMutantes = java.util.Set.of("Deleta", "ProcessarCheckInAutomatico", "Cadastra", "Atualiza", "LogoutCliente");
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

            // Padrão FACTORY METHOD com Reflection dinâmica:
            // Constrói o nome qualificado da classe Action correspondente
            String nomeDaClasse = "br.com.commandfactory.controller." + paramAction + "ReservaAction";
            Class<?> classAction = null;
            try {
                classAction = Class.forName(nomeDaClasse);
            } catch (ClassNotFoundException e) {
                // Tenta sem o sufixo Reserva (ex: ProcessarCheckInAutomaticoAction)
                nomeDaClasse = "br.com.commandfactory.controller." + paramAction + "Action";
                classAction = Class.forName(nomeDaClasse);
            }
            ICommand commandAction = (ICommand) classAction.getDeclaredConstructor().newInstance();

            // Padrão COMMAND: Executa a ação e obtém a página JSP de destino
            String pageAction = commandAction.executar(request, response);

            // Redireciona internamente para a View (JSP em WEB-INF/views/) se não houve redirecionamento prévio
            if (pageAction != null && !pageAction.trim().isEmpty()) {
                if (!pageAction.startsWith("/WEB-INF/views/") && pageAction.endsWith(".jsp")) {
                    pageAction = "/WEB-INF/views/" + pageAction;
                }
                request.getRequestDispatcher(pageAction).forward(request, response);
            }

        } catch (ClassNotFoundException e) {
            LOGGER.log(java.util.logging.Level.WARNING, "Comando de ação não encontrado", e);
            request.setAttribute("msg", "Ação solicitada não foi encontrada no sistema.");
            request.setAttribute("tipoMsg", "warning");
            request.getRequestDispatcher("/WEB-INF/views/resultado.jsp").forward(request, response);
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
