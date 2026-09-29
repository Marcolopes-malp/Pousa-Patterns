package br.com.commandfactory.controller;

import dao.AcomodacaoDAO;
import dao.HospedeDAO;
import dao.ReservaDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Acomodacao;
import model.Hospede;
import model.ItemServico;
import model.Reserva;
import model.ReservaBuilder;
import model.factory.ServicoFactory;

public class CadastraReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Ação permitida exclusivamente via POST.");
                return null;
            }

            HttpSession session = request.getSession();
            String sessionToken = (String) session.getAttribute("csrfToken");
            String requestToken = request.getParameter("csrfToken");
            if (sessionToken == null || !sessionToken.equals(requestToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF inválido ou expirado.");
                return null;
            }

            Hospede hospede = (Hospede) session.getAttribute("usuarioLogado");

            if (hospede == null) {
                String nome = request.getParameter("txtNomeHospede");
                String cpf = request.getParameter("txtCpf");
                String email = request.getParameter("txtEmail");
                String telefone = request.getParameter("txtTelefone");
                String cidade = request.getParameter("txtCidadeOrigem");
                String senha = request.getParameter("txtSenha");

                HospedeDAO hdao = new HospedeDAO();
                Hospede existente = (email != null && !email.trim().isEmpty()) ? hdao.buscarPorEmail(email.trim()) : null;

                if (existente != null) {
                    // S1: Se o e-mail já existe e não há sessão, NÃO autentica. Redireciona para o login com mensagem e preserva o fluxo.
                    String acomodacaoId = request.getParameter("acomodacaoId");
                    String checkInParam = request.getParameter("txtCheckIn");
                    String checkOutParam = request.getParameter("txtCheckOut");
                    String qtdHospedesParam = request.getParameter("txtQtdHospedes");

                    String redirect = "controller.do?btnop=NovaReserva"
                            + (acomodacaoId != null ? "&acomodacaoId=" + java.net.URLEncoder.encode(acomodacaoId, "UTF-8") : "")
                            + (checkInParam != null ? "&txtCheckIn=" + java.net.URLEncoder.encode(checkInParam, "UTF-8") : "")
                            + (checkOutParam != null ? "&txtCheckOut=" + java.net.URLEncoder.encode(checkOutParam, "UTF-8") : "")
                            + (qtdHospedesParam != null ? "&txtQtdHospedes=" + java.net.URLEncoder.encode(qtdHospedesParam, "UTF-8") : "");

                    request.setAttribute("redirect", redirect);
                    request.setAttribute("msg", "E-mail já cadastrado, faça login para continuar sua reserva.");
                    request.setAttribute("tipoMsg", "warning");
                    return "login.jsp";
                }

                if (senha == null || senha.trim().length() < 6) {
                    request.setAttribute("msg", "Para criar seu cadastro, a senha é obrigatória e deve ter pelo menos 6 caracteres.");
                    request.setAttribute("tipoMsg", "danger");
                    return "reserva.jsp";
                }

                hospede = new Hospede(nome, cpf, email, telefone, cidade, senha.trim());
                int hid = hdao.cadastrar(hospede);
                hospede.setId(hid);

                // S5: Rotação de ID de sessão e limpeza de credencial
                request.changeSessionId();
                hospede.setSenha(null);

                session = request.getSession();
                session.setAttribute("usuarioLogado", hospede);
                session.setAttribute("csrfToken", java.util.UUID.randomUUID().toString());
            }

            String checkIn = request.getParameter("txtCheckIn");
            String checkOut = request.getParameter("txtCheckOut");

            if (checkIn == null || checkIn.trim().isEmpty() || checkOut == null || checkOut.trim().isEmpty()) {
                request.setAttribute("msg", "As datas de check-in e check-out são obrigatórias.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            LocalDate dtIn;
            LocalDate dtOut;
            try {
                dtIn = LocalDate.parse(checkIn.trim());
                dtOut = LocalDate.parse(checkOut.trim());
            } catch (Exception e) {
                request.setAttribute("msg", "Formato de data inválido. Utilize o formato AAAA-MM-DD.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            if (!dtOut.isAfter(dtIn)) {
                request.setAttribute("msg", "A data de check-out deve ser estritamente posterior à data de check-in.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            long numDiarias = ChronoUnit.DAYS.between(dtIn, dtOut);
            if (numDiarias <= 0) {
                numDiarias = 1;
            }

            int qtdHospedes = 1;
            try {
                qtdHospedes = Integer.parseInt(request.getParameter("txtQtdHospedes"));
            } catch (Exception ignored) {}
            if (qtdHospedes <= 0) qtdHospedes = 1;

            // S6: Obtenção segura e recálculo da diária no servidor via AcomodacaoDAO
            AcomodacaoDAO acomodacaoDAO = new AcomodacaoDAO();
            Acomodacao acomodacao = null;

            int acomodacaoId = 0;
            try {
                String aidStr = request.getParameter("acomodacaoId");
                if (aidStr != null && !aidStr.trim().isEmpty()) {
                    acomodacaoId = Integer.parseInt(aidStr.trim());
                    acomodacao = acomodacaoDAO.buscarPorId(acomodacaoId);
                }
            } catch (Exception ignored) {}

            String tipoQuarto = request.getParameter("txtTipoQuarto");
            if (acomodacao == null && tipoQuarto != null) {
                acomodacao = acomodacaoDAO.buscarPorNome(tipoQuarto);
            }

            double valorDiaria;
            if (acomodacao != null) {
                valorDiaria = acomodacao.getValorDiaria();
                tipoQuarto = acomodacao.getNome();
            } else {
                valorDiaria = 250.0;
                if (tipoQuarto == null || tipoQuarto.trim().isEmpty()) {
                    tipoQuarto = "Suíte Standard Jardim Colonial";
                }
            }

            // Permite ajuste manual de diária apenas para equipe da recepção
            if (hospede != null && hospede.isRecepcao() && request.getParameter("txtValorDiaria") != null) {
                try {
                    double vdManual = Double.parseDouble(request.getParameter("txtValorDiaria"));
                    if (vdManual > 0) {
                        valorDiaria = vdManual;
                    }
                } catch (Exception ignored) {}
            }

            String formaPagamento = request.getParameter("txtFormaPagamento");
            if (formaPagamento == null || formaPagamento.trim().isEmpty()) {
                formaPagamento = "PIX";
            }
            String observacoes = request.getParameter("txtObservacoes");

            // Coleta de serviços adicionais
            List<ItemServico> servicos = new ArrayList<>();
            double totalServicos = 0.0;

            if (request.getParameter("chkCafe") != null) {
                ItemServico cafe = ServicoFactory.obterFabrica("cafe").criarServico();
                servicos.add(cafe);
                totalServicos += cafe.getSubtotal();
            }
            if (request.getParameter("chkTransfer") != null) {
                ItemServico transfer = ServicoFactory.obterFabrica("transfer").criarServico();
                servicos.add(transfer);
                totalServicos += transfer.getSubtotal();
            }
            if (request.getParameter("chkPasseio") != null) {
                ItemServico passeio = ServicoFactory.obterFabrica("passeio").criarServico();
                servicos.add(passeio);
                totalServicos += passeio.getSubtotal();
            }
            if (request.getParameter("chkSpa") != null) {
                ItemServico spa = ServicoFactory.obterFabrica("spa").criarServico();
                servicos.add(spa);
                totalServicos += spa.getSubtotal();
            }

            // Recalcula o valor total exclusivamente no servidor (diárias oficiais + serviços)
            double valorTotalCalculado = (numDiarias * valorDiaria) + totalServicos;

            ReservaBuilder builder = ReservaBuilder.novo()
                    .comHospede(hospede)
                    .comPeriodo(checkIn, checkOut)
                    .comQuantidadeHospedes(qtdHospedes)
                    .comTipoQuarto(tipoQuarto)
                    .comValorDiaria(valorDiaria)
                    .comValorTotal(valorTotalCalculado)
                    .comFormaPagamento(formaPagamento)
                    .comObservacoes(observacoes)
                    .comStatus("CONFIRMADA");

            for (ItemServico s : servicos) {
                builder.adicionarServico(s);
            }

            Reserva reserva = builder.constroi();

            ReservaDAO dao = new ReservaDAO();
            int idGerado = dao.cadastrar(reserva);
            reserva.setId(idGerado);

            request.setAttribute("reserva", reserva);
            request.setAttribute("msg", "Reserva confirmada com sucesso.");
            request.setAttribute("tipoMsg", "success");

            return "detalhesReserva.jsp";

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("msg", "Não foi possível concluir a reserva: " + e.getMessage());
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
