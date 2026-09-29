<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Reserva"%>
<%@page import="model.Hospede"%>
<%@page import="model.Acomodacao"%>
<%@page import="util.Html"%>
<%
    Reserva r = (Reserva) request.getAttribute("reserva");
    if (r == null) {
        response.sendRedirect("controller.do?btnop=Admin");
        return;
    }
    Hospede h = r.getHospede();
    if (h == null) h = new Hospede();
    List<Acomodacao> acomodacoes = (List<Acomodacao>) request.getAttribute("acomodacoes");
    if (acomodacoes == null) {
        acomodacoes = new dao.AcomodacaoDAO().listarTodas();
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Editar Reserva <%= Html.esc(r.getCodigoLocalizador()) %> | Pousada Paradiso</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body style="background: var(--bg-surface);">

    <header class="site-header">
        <div class="header-inner">
            <a href="controller.do?btnop=ConsultaTodos" class="brand-link">
                <span>🏖️</span>
                <span>Pousada Paradiso</span>
                <span class="brand-badge">Painel Recepção</span>
            </a>
            <div class="nav-right">
                <a href="controller.do?btnop=Admin" class="nav-link">&larr; Voltar para a gestão</a>
            </div>
        </div>
    </header>

    <main class="container" style="max-width: 820px;">
        <div style="background: #FFFFFF; border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: 2rem; box-shadow: var(--shadow-card);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem;">
                <div>
                    <h1 style="font-size: 1.5rem; font-weight: 800; margin-bottom: 0.2rem;">Editar Reserva</h1>
                    <p style="color: var(--text-secondary); font-size: 0.9rem;">
                        Código: <strong><%= Html.esc(r.getCodigoLocalizador()) %></strong>
                    </p>
                </div>
                <span class="status-badge <%= Html.esc(r.getStatus()) %>"><%= Html.esc(r.getStatus()) %></span>
            </div>

            <form action="controller.do" method="POST">
                <input type="hidden" name="btnop" value="Atualiza">
                <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                <input type="hidden" name="txtId" value="<%= r.getId() %>">
                <input type="hidden" name="txtHospedeId" value="<%= h.getId() %>">
                <input type="hidden" name="txtCodigo" value="<%= Html.esc(r.getCodigoLocalizador()) %>">

                <h3 class="section-title">Hóspede titular</h3>
                <div class="form-row">
                    <div class="form-group" style="grid-column: span 2;">
                        <label for="txtNomeHospede">Nome completo</label>
                        <input type="text" id="txtNomeHospede" name="txtNomeHospede" class="input-text" value="<%= Html.esc(h.getNomeCompleto()) %>" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="txtCpf">CPF</label>
                        <input type="text" id="txtCpf" name="txtCpf" class="input-text" value="<%= Html.esc(h.getCpf()) %>" required>
                    </div>
                    <div class="form-group">
                        <label for="txtTelefone">Telefone</label>
                        <input type="text" id="txtTelefone" name="txtTelefone" class="input-text" value="<%= Html.esc(h.getTelefone()) %>" required>
                    </div>
                </div>
                <div class="form-row" style="margin-bottom: 2rem;">
                    <div class="form-group">
                        <label for="txtEmail">E-mail</label>
                        <input type="email" id="txtEmail" name="txtEmail" class="input-text" value="<%= Html.esc(h.getEmail()) %>" required>
                    </div>
                    <div class="form-group">
                        <label for="txtCidadeOrigem">Cidade de origem</label>
                        <input type="text" id="txtCidadeOrigem" name="txtCidadeOrigem" class="input-text" value="<%= Html.esc(h.getCidadeOrigem()) %>">
                    </div>
                </div>

                <h3 class="section-title">Dados da hospedagem</h3>
                <div class="form-row">
                    <div class="form-group">
                        <label for="txtCheckIn">Check-in</label>
                        <input type="date" id="txtCheckIn" name="txtCheckIn" class="input-text" value="<%= Html.esc(r.getDataCheckIn()) %>" required>
                    </div>
                    <div class="form-group">
                        <label for="txtCheckOut">Check-out</label>
                        <input type="date" id="txtCheckOut" name="txtCheckOut" class="input-text" value="<%= Html.esc(r.getDataCheckOut()) %>" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="acomodacaoId">Acomodação</label>
                        <select id="acomodacaoId" name="acomodacaoId" class="select-text" onchange="atualizarDadosAcomodacao(this)" required>
                            <% for (Acomodacao a : acomodacoes) {
                                boolean selecionada = (r.getAcomodacaoId() > 0 && r.getAcomodacaoId() == a.getId())
                                        || (r.getAcomodacaoId() <= 0 && a.getNome().equalsIgnoreCase(r.getTipoQuarto()));
                            %>
                                <option value="<%= a.getId() %>" data-nome="<%= Html.esc(a.getNome()) %>" data-diaria="<%= a.getValorDiaria() %>" data-capacidade="<%= a.getCapacidadePessoas() %>" <%= selecionada ? "selected" : "" %>>
                                    <%= Html.esc(a.getNome()) %> (Capacidade: <%= a.getCapacidadePessoas() %> • R$ <%= String.format(java.util.Locale.US, "%.2f", a.getValorDiaria()) %>/dia)
                                </option>
                            <% } %>
                        </select>
                        <input type="hidden" id="txtTipoQuarto" name="txtTipoQuarto" value="<%= Html.esc(r.getTipoQuarto()) %>">
                    </div>
                    <div class="form-group">
                        <label for="txtValorDiaria">Valor da diária (R$)</label>
                        <input type="number" step="0.01" id="txtValorDiaria" name="txtValorDiaria" class="input-text" value="<%= r.getValorDiaria() %>" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="txtValorTotal">Valor total (R$)</label>
                        <input type="number" step="0.01" id="txtValorTotal" name="txtValorTotal" class="input-text" value="<%= r.getValorTotal() %>" required>
                    </div>
                    <div class="form-group">
                        <label for="txtQtdHospedes">Hóspedes</label>
                        <input type="number" id="txtQtdHospedes" name="txtQtdHospedes" min="1" max="10" value="<%= r.getQuantidadeHospedes() %>" class="input-text" required>
                    </div>
                </div>
                <div class="form-row" style="margin-bottom: 2rem;">
                    <div class="form-group">
                        <label for="txtStatus">Status</label>
                        <select id="txtStatus" name="txtStatus" class="select-text">
                            <option value="PENDENTE" <%= "PENDENTE".equalsIgnoreCase(r.getStatus()) ? "selected" : "" %>>PENDENTE</option>
                            <option value="CONFIRMADA" <%= "CONFIRMADA".equalsIgnoreCase(r.getStatus()) ? "selected" : "" %>>CONFIRMADA</option>
                            <option value="CHECKIN_ATIVO" <%= "CHECKIN_ATIVO".equalsIgnoreCase(r.getStatus()) ? "selected" : "" %>>CHECKIN_ATIVO</option>
                            <option value="FINALIZADA" <%= "FINALIZADA".equalsIgnoreCase(r.getStatus()) ? "selected" : "" %>>FINALIZADA</option>
                            <option value="CANCELADA" <%= "CANCELADA".equalsIgnoreCase(r.getStatus()) ? "selected" : "" %>>CANCELADA</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="txtFormaPagamento">Forma de pagamento</label>
                        <select id="txtFormaPagamento" name="txtFormaPagamento" class="select-text">
                            <option value="PIX" <%= "PIX".equalsIgnoreCase(r.getFormaPagamento()) ? "selected" : "" %>>PIX</option>
                            <option value="CARTAO_CREDITO" <%= "CARTAO_CREDITO".equalsIgnoreCase(r.getFormaPagamento()) ? "selected" : "" %>>Cartão de crédito</option>
                            <option value="TRANSFERENCIA" <%= "TRANSFERENCIA".equalsIgnoreCase(r.getFormaPagamento()) ? "selected" : "" %>>Transferência bancária</option>
                            <option value="DINHEIRO" <%= "DINHEIRO".equalsIgnoreCase(r.getFormaPagamento()) ? "selected" : "" %>>Dinheiro</option>
                        </select>
                    </div>
                </div>

                <div class="form-group" style="margin-bottom: 2rem;">
                    <label for="txtObservacoes">Observações</label>
                    <textarea id="txtObservacoes" name="txtObservacoes" class="input-text" rows="2"><%= Html.esc(r.getObservacoes()) %></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 1rem;">
                    <a href="controller.do?btnop=Admin" class="btn-secondary-action">Cancelar</a>
                    <button type="submit" class="btn-primary-action">
                        Atualizar dados
                    </button>
                </div>
            </form>
        </div>
    </main>

    <script>
        function atualizarDadosAcomodacao(selectElem) {
            const opt = selectElem.options[selectElem.selectedIndex];
            if (opt) {
                const nome = opt.getAttribute('data-nome');
                const diaria = opt.getAttribute('data-diaria');
                const cap = opt.getAttribute('data-capacidade');
                if (nome) {
                    const txtTipo = document.getElementById('txtTipoQuarto');
                    if (txtTipo) txtTipo.value = nome;
                }
                if (diaria && document.getElementById('txtValorDiaria')) {
                    document.getElementById('txtValorDiaria').value = diaria;
                }
                if (cap && document.getElementById('txtQtdHospedes')) {
                    document.getElementById('txtQtdHospedes').max = cap;
                }
            }
        }
    </script>
</body>
</html>
