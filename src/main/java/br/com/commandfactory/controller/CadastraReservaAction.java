package br.com.commandfactory.controller;

import dao.HospedeDAO;
import dao.ReservaDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;
import model.ItemServico;
import model.Reserva;
import model.ReservaBuilder;
import model.factory.ServicoFactory;

public class CadastraReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            HttpSession session = request.getSession();
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
                session.setAttribute("usuarioLogado", hospede);
            }

            String checkIn = request.getParameter("txtCheckIn");
            String checkOut = request.getParameter("txtCheckOut");
            int qtdHospedes = 1;
            try {
                qtdHospedes = Integer.parseInt(request.getParameter("txtQtdHospedes"));
            } catch (Exception ignored) {}

            String tipoQuarto = request.getParameter("txtTipoQuarto");
            double valorDiaria = 250.0;
            try {
                valorDiaria = Double.parseDouble(request.getParameter("txtValorDiaria"));
            } catch (Exception ignored) {}

            String formaPagamento = request.getParameter("txtFormaPagamento");
            String observacoes = request.getParameter("txtObservacoes");

            ReservaBuilder builder = ReservaBuilder.novo()
                    .comHospede(hospede)
                    .comPeriodo(checkIn, checkOut)
                    .comQuantidadeHospedes(qtdHospedes)
                    .comTipoQuarto(tipoQuarto)
                    .comValorDiaria(valorDiaria)
                    .comFormaPagamento(formaPagamento)
                    .comObservacoes(observacoes)
                    .comStatus("CONFIRMADA");

            if (request.getParameter("chkCafe") != null) {
                ItemServico cafe = ServicoFactory.obterFabrica("cafe").criarServico();
                builder.adicionarServico(cafe);
            }
            if (request.getParameter("chkTransfer") != null) {
                ItemServico transfer = ServicoFactory.obterFabrica("transfer").criarServico();
                builder.adicionarServico(transfer);
            }
            if (request.getParameter("chkPasseio") != null) {
                ItemServico passeio = ServicoFactory.obterFabrica("passeio").criarServico();
                builder.adicionarServico(passeio);
            }
            if (request.getParameter("chkSpa") != null) {
                ItemServico spa = ServicoFactory.obterFabrica("spa").criarServico();
                builder.adicionarServico(spa);
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
