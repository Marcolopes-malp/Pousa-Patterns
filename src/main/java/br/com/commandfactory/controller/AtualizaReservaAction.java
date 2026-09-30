package br.com.commandfactory.controller;

import dao.AcomodacaoDAO;
import dao.ReservaDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Acomodacao;
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

            // B7: Validar unicidade do e-mail para não colidir com outro hóspede
            dao.HospedeDAO hospedeDAO = new dao.HospedeDAO();
            if (email != null && !email.trim().isEmpty()) {
                Hospede outro = hospedeDAO.buscarPorEmail(email.trim());
                if (outro != null && outro.getId() != hospedeId) {
                    request.setAttribute("msg", "O e-mail '" + email.trim() + "' já está cadastrado para outro hóspede.");
                    request.setAttribute("tipoMsg", "danger");
                    return "resultado.jsp";
                }
            }

            Hospede hospede = null;
            if (hospedeId > 0) {
                hospede = hospedeDAO.consultarById(hospedeId);
                if (hospede != null) {
                    hospede.setNomeCompleto(nome);
                    hospede.setCpf(cpf);
                    hospede.setEmail(email != null ? email.trim() : hospede.getEmail());
                    hospede.setTelefone(telefone);
                    hospede.setCidadeOrigem(cidade);
                    hospedeDAO.atualizar(hospede);
                }
            }
            if (hospede == null) {
                hospede = new Hospede(hospedeId, nome, cpf, email, telefone, cidade);
            }

            String checkIn = request.getParameter("txtCheckIn");
            String checkOut = request.getParameter("txtCheckOut");

            try {
                java.time.LocalDate dtIn = java.time.LocalDate.parse(checkIn);
                java.time.LocalDate dtOut = java.time.LocalDate.parse(checkOut);
                if (!dtOut.isAfter(dtIn)) {
                    request.setAttribute("msg", "A data de check-out deve ser estritamente posterior à data de check-in.");
                    request.setAttribute("tipoMsg", "danger");
                    return "resultado.jsp";
                }
            } catch (Exception e) {
                request.setAttribute("msg", "Formato de data inválido.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            int qtdHospedes = Integer.parseInt(request.getParameter("txtQtdHospedes"));
            String tipoQuarto = request.getParameter("txtTipoQuarto");

            int acomodacaoId = 0;
            String aidStr = request.getParameter("acomodacaoId");
            if (aidStr != null && !aidStr.trim().isEmpty()) {
                try {
                    acomodacaoId = Integer.parseInt(aidStr.trim());
                } catch (NumberFormatException ignored) {}
            }

            Acomodacao acomodacao = null;
            AcomodacaoDAO acomodacaoDAO = new AcomodacaoDAO();
            if (acomodacaoId > 0) {
                acomodacao = acomodacaoDAO.buscarPorId(acomodacaoId);
            }
            if (acomodacao == null && tipoQuarto != null && !tipoQuarto.trim().isEmpty()) {
                acomodacao = acomodacaoDAO.buscarPorNome(tipoQuarto);
            }
            if (acomodacao != null) {
                tipoQuarto = acomodacao.getNome();
                acomodacaoId = acomodacao.getId();
            }

            double valorDiaria = Double.parseDouble(request.getParameter("txtValorDiaria"));
            double valorTotal = Double.parseDouble(request.getParameter("txtValorTotal"));
            if (valorTotal <= 0) {
                java.time.LocalDate dtIn = java.time.LocalDate.parse(checkIn);
                java.time.LocalDate dtOut = java.time.LocalDate.parse(checkOut);
                long dias = java.time.temporal.ChronoUnit.DAYS.between(dtIn, dtOut);
                valorTotal = (dias > 0 ? dias : 1) * valorDiaria;
            }
            String status = request.getParameter("txtStatus");
            String formaPagamento = request.getParameter("txtFormaPagamento");
            String observacoes = request.getParameter("txtObservacoes");

            // B6: Se a reserva não estiver cancelada, valida a disponibilidade de vagas (ignorando a própria reserva sendo editada)
            ReservaDAO dao = new ReservaDAO();
            if (acomodacao != null && !"CANCELADA".equalsIgnoreCase(status)) {
                int sobrepostas = dao.contarReservasSobrepostas(acomodacao.getId(), checkIn, checkOut, id);
                if (sobrepostas >= acomodacao.getVagasRestantes()) {
                    request.setAttribute("msg", "Desculpe, a acomodação '" + acomodacao.getNome() + "' não possui vagas suficientes para o período de " + checkIn + " a " + checkOut + ".");
                    request.setAttribute("tipoMsg", "warning");
                    return "resultado.jsp";
                }
            }

            ReservaBuilder builder = ReservaBuilder.novo()
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
                    .comObservacoes(observacoes);

            if (acomodacao != null) {
                builder.comAcomodacao(acomodacao);
            } else if (acomodacaoId > 0) {
                builder.comAcomodacaoId(acomodacaoId);
            }

            Reserva reserva = builder.constroi();
            dao.atualizar(reserva);

            request.setAttribute("msg", "Reserva " + reserva.getCodigoLocalizador() + " atualizada com sucesso!");
            request.setAttribute("tipoMsg", "success");
            return "resultado.jsp";
        } catch (Exception e) {
            java.util.logging.Logger.getLogger(AtualizaReservaAction.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Erro ao atualizar dados da reserva", e);
            String msg = (e instanceof IllegalArgumentException)
                    ? e.getMessage()
                    : "Não foi possível atualizar os dados da reserva no momento. Por favor, tente novamente.";
            request.setAttribute("msg", msg);
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
