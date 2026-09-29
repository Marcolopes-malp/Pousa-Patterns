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

            int qtdHospedes;
            String qtdHospedesStr = request.getParameter("txtQtdHospedes");
            try {
                qtdHospedes = Integer.parseInt(qtdHospedesStr != null ? qtdHospedesStr.trim() : "1");
            } catch (NumberFormatException e) {
                request.setAttribute("msg", "Quantidade de hóspedes inválida.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }
            if (qtdHospedes <= 0) {
                request.setAttribute("msg", "A quantidade de hóspedes deve ser de pelo menos 1 pessoa.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            // S6 / B5: Obtenção segura e validação da acomodação sem valores padrão silenciosos
            AcomodacaoDAO acomodacaoDAO = new AcomodacaoDAO();
            Acomodacao acomodacao = null;

            String aidStr = request.getParameter("acomodacaoId");
            if (aidStr != null && !aidStr.trim().isEmpty()) {
                int acomodacaoId;
                try {
                    acomodacaoId = Integer.parseInt(aidStr.trim());
                } catch (NumberFormatException e) {
                    request.setAttribute("msg", "Identificador de acomodação inválido: " + aidStr);
                    request.setAttribute("tipoMsg", "danger");
                    return "resultado.jsp";
                }
                acomodacao = acomodacaoDAO.buscarPorId(acomodacaoId);
                if (acomodacao == null) {
                    request.setAttribute("msg", "Acomodação #" + acomodacaoId + " não encontrada no catálogo.");
                    request.setAttribute("tipoMsg", "danger");
                    return "resultado.jsp";
                }
            }

            String tipoQuarto = request.getParameter("txtTipoQuarto");
            if (acomodacao == null && tipoQuarto != null && !tipoQuarto.trim().isEmpty()) {
                acomodacao = acomodacaoDAO.buscarPorNome(tipoQuarto);
            }

            if (acomodacao == null) {
                request.setAttribute("msg", "Nenhuma acomodação válida foi informada ou encontrada para a reserva.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            // B6: Controle de disponibilidade e sobreposição de datas
            ReservaDAO dao = new ReservaDAO();
            int reservasSobrepostas = dao.contarReservasSobrepostas(acomodacao.getId(), checkIn.trim(), checkOut.trim(), 0);
            if (reservasSobrepostas >= acomodacao.getVagasRestantes()) {
                request.setAttribute("msg", "Desculpe, a acomodação '" + acomodacao.getNome() + "' não possui vagas suficientes para o período de " + checkIn + " a " + checkOut + ".");
                request.setAttribute("tipoMsg", "warning");
                return "resultado.jsp";
            }

            double valorDiaria = acomodacao.getValorDiaria();
            tipoQuarto = acomodacao.getNome();

            // Permite ajuste manual de diária apenas para equipe da recepção
            if (hospede != null && hospede.isRecepcao() && request.getParameter("txtValorDiaria") != null) {
                String vdParam = request.getParameter("txtValorDiaria").trim();
                if (!vdParam.isEmpty()) {
                    try {
                        double vdManual = Double.parseDouble(vdParam);
                        if (vdManual > 0) {
                            valorDiaria = vdManual;
                        }
                    } catch (NumberFormatException e) {
                        request.setAttribute("msg", "Valor de diária manual inválido: " + vdParam);
                        request.setAttribute("tipoMsg", "danger");
                        return "resultado.jsp";
                    }
                }
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
                ItemServico cafe = ServicoFactory.obterFabrica("cafe").criarServico((int) numDiarias);
                servicos.add(cafe);
                totalServicos += cafe.getSubtotal();
            }
            if (request.getParameter("chkTransfer") != null) {
                ItemServico transfer = ServicoFactory.obterFabrica("transfer").criarServico();
                servicos.add(transfer);
                totalServicos += transfer.getSubtotal();
            }
            if (request.getParameter("chkPasseio") != null) {
                ItemServico passeio = ServicoFactory.obterFabrica("passeio").criarServico(qtdHospedes);
                servicos.add(passeio);
                totalServicos += passeio.getSubtotal();
            }
            if (request.getParameter("chkSpa") != null) {
                ItemServico spa = ServicoFactory.obterFabrica("spa").criarServico();
                servicos.add(spa);
                totalServicos += spa.getSubtotal();
            }

            // B1: Recalcula o valor total unificado via Padrão STRATEGY (diárias, desconto long-stay, serviços, taxa e desconto PIX)
            model.strategy.CalculadoraPreco calculadora = new model.strategy.CalculadoraPreco();
            model.strategy.ResultadoCalculoPreco calculo = calculadora.calcular(numDiarias, valorDiaria, servicos, formaPagamento);
            double valorTotalCalculado = calculo.getValorTotalFinal();

            ReservaBuilder builder = ReservaBuilder.novo()
                    .comHospede(hospede)
                    .comPeriodo(checkIn, checkOut)
                    .comQuantidadeHospedes(qtdHospedes)
                    .comAcomodacao(acomodacao)
                    .comTipoQuarto(tipoQuarto)
                    .comValorDiaria(valorDiaria)
                    .comValorTotal(valorTotalCalculado)
                    .comFormaPagamento(formaPagamento)
                    .comObservacoes(observacoes)
                    .comStatus("CONFIRMADA");

            if (acomodacao != null && acomodacao.getCapacidadePessoas() > 0) {
                builder.comCapacidadeMaxima(acomodacao.getCapacidadePessoas());
            }

            for (ItemServico s : servicos) {
                builder.adicionarServico(s);
            }

            Reserva reserva = builder.constroi();

            int idGerado = dao.cadastrar(reserva);
            reserva.setId(idGerado);

            request.setAttribute("reserva", reserva);
            request.setAttribute("msg", "Reserva confirmada com sucesso.");
            request.setAttribute("tipoMsg", "success");

            return "detalhesReserva.jsp";

        } catch (Exception e) {
            java.util.logging.Logger.getLogger(CadastraReservaAction.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Erro ao cadastrar reserva", e);
            String msg = (e instanceof IllegalArgumentException)
                    ? e.getMessage()
                    : "Não foi possível concluir a reserva no momento. Por favor, tente novamente.";
            request.setAttribute("msg", msg);
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
