package br.com.commandfactory.controller;

import dao.ReservaDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;
import model.Reserva;
import model.ReservaBuilder;

/**
 * Comando para Atualizar os dados de uma Reserva existente.
 */
public class AtualizaReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Ação de atualização permitida exclusivamente via POST.");
                return null;
            }

            HttpSession session = request.getSession();
            String sessionToken = (String) session.getAttribute("csrfToken");
            String requestToken = request.getParameter("csrfToken");
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF inválido ou expirado.");
                return null;
            }

            Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");
            if (usuario == null) {
                response.sendRedirect("controller.do?btnop=Login");
                return null;
            }
            if (!usuario.isRecepcao()) {
                request.setAttribute("msg", "Acesso negado: a atualização de reservas é restrita à equipe de recepção.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            int id = Integer.parseInt(request.getParameter("txtId"));
            int hospedeId = Integer.parseInt(request.getParameter("txtHospedeId"));
            String codigo = request.getParameter("txtCodigo");
            String nome = request.getParameter("txtNomeHospede");
            String cpf = request.getParameter("txtCpf");
            String email = request.getParameter("txtEmail");
            String telefone = request.getParameter("txtTelefone");
            String cidade = request.getParameter("txtCidadeOrigem");

            Hospede hospede = new Hospede(hospedeId, nome, cpf, email, telefone, cidade);

            String checkIn = request.getParameter("txtCheckIn");
            String checkOut = request.getParameter("txtCheckOut");
            int qtdHospedes = Integer.parseInt(request.getParameter("txtQtdHospedes"));
            String tipoQuarto = request.getParameter("txtTipoQuarto");
            double valorDiaria = Double.parseDouble(request.getParameter("txtValorDiaria"));
            double valorTotal = Double.parseDouble(request.getParameter("txtValorTotal"));
            String status = request.getParameter("txtStatus");
            String formaPagamento = request.getParameter("txtFormaPagamento");
            String observacoes = request.getParameter("txtObservacoes");

            Reserva reserva = ReservaBuilder.novo()
                    .comId(id)
                    .comCodigoLocalizador(codigo)
                    .comHospede(hospede)
                    .comPeriodo(checkIn, checkOut)
                    .comQuantidadeHospedes(qtdHospedes)
                    .comTipoQuarto(tipoQuarto)
                    .comValorDiaria(valorDiaria)
                    .comValorTotal(valorTotal)
                    .comStatus(status)
                    .comFormaPagamento(formaPagamento)
                    .comObservacoes(observacoes)
                    .constroi();

            ReservaDAO dao = new ReservaDAO();
            dao.atualizar(reserva);

            request.setAttribute("msg", "Reserva " + reserva.getCodigoLocalizador() + " atualizada com sucesso!");
            request.setAttribute("tipoMsg", "success");
            return "resultado.jsp";
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("msg", "Erro ao atualizar reserva: " + e.getMessage());
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
