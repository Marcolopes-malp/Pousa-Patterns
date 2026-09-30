package br.com.commandfactory.controller;

import dao.AcomodacaoDAO;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Acomodacao;

/**
 * Comando para carregar a página inicial (Home) com as acomodações disponíveis do catálogo.
 * Elimina consulta inútil de reservas na home e provê dados para a JSP via request (A1 / D1).
 */
public class ConsultaTodosReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            AcomodacaoDAO dao = new AcomodacaoDAO();
            List<Acomodacao> acomodacoes = dao.listarTodas();
            request.setAttribute("acomodacoes", acomodacoes);
            return "index.jsp";
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(ConsultaTodosReservaAction.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Erro ao carregar catálogo de acomodações", e);
            request.setAttribute("msg", "Não foi possível carregar as opções de acomodação no momento.");
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
