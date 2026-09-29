package br.com.commandfactory.controller;

import dao.AcomodacaoDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Acomodacao;

/**
 * Controller Action que inicializa a tela de reserva para uma acomodação selecionada.
 * Valida o parâmetro acomodacaoId e rejeita identificadores ausentes, malformados ou inexistentes (B5).
 */
public class NovaReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String aidParam = request.getParameter("acomodacaoId");
        if (aidParam == null || aidParam.trim().isEmpty()) {
            request.setAttribute("msg", "Acomodação não informada para iniciar a reserva.");
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }

        int id;
        try {
            id = Integer.parseInt(aidParam.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("msg", "Identificador de acomodação inválido: " + aidParam);
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }

        AcomodacaoDAO dao = new AcomodacaoDAO();
        Acomodacao acomodacao = dao.buscarPorId(id);
        if (acomodacao == null) {
            request.setAttribute("msg", "Acomodação #" + id + " não encontrada no catálogo.");
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }

        request.setAttribute("acomodacao", acomodacao);
        return "reserva.jsp";
    }
}
