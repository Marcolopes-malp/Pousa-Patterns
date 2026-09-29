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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");

        try {
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

            // Redireciona internamente para a View (JSP) se não houve redirecionamento prévio
            if (pageAction != null && !pageAction.trim().isEmpty()) {
                request.getRequestDispatcher(pageAction).forward(request, response);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            request.setAttribute("msg", "Erro 404: Comando de ação não encontrado no sistema.");
            request.setAttribute("tipoMsg", "danger");
            request.getRequestDispatcher("resultado.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("msg", "Erro interno no processamento da requisição: " + e.getMessage());
            request.setAttribute("tipoMsg", "danger");
            request.getRequestDispatcher("resultado.jsp").forward(request, response);
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
