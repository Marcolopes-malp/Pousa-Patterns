<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Hospede"%>
<%@page import="model.Acomodacao"%>
<%@page import="dao.AcomodacaoDAO"%>
<%
    Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");
    Acomodacao acomodacao = (Acomodacao) request.getAttribute("acomodacao");
    if (acomodacao == null) {
        int id = 1;
        try {
            id = Integer.parseInt(request.getParameter("acomodacaoId"));
        } catch (Exception ignored) {}
        acomodacao = new AcomodacaoDAO().buscarPorId(id);
    }

    String checkInParam = request.getParameter("txtCheckIn");
    if (checkInParam == null || checkInParam.isEmpty()) checkInParam = "2026-10-10";
    String checkOutParam = request.getParameter("txtCheckOut");
    if (checkOutParam == null || checkOutParam.isEmpty()) checkOutParam = "2026-10-15";
    String qtdHospedesParam = request.getParameter("txtQtdHospedes");
    if (qtdHospedesParam == null || qtdHospedesParam.isEmpty()) qtdHospedesParam = "2";
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Finalizar Reserva | <%= acomodacao.getNome() %></title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .step-container {
            background: #FFFFFF;
            border: 1px solid var(--border-default);
            border-radius: var(--radius-md);
            padding: 1.75rem;
            margin-bottom: 1.5rem;
            box-shadow: var(--shadow-card);
        }
        .step-header {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            margin-bottom: 1.25rem;
            padding-bottom: 0.75rem;
            border-bottom: 1px solid var(--border-light);
        }
        .step-number {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: #222222;
            color: #FFFFFF;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.85rem;
            font-weight: 800;
        }
        .step-title {
            font-size: 1.15rem;
            font-weight: 800;
            color: var(--text-primary);
        }
        .payment-methods {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
            gap: 0.75rem;
            margin-bottom: 1.25rem;
        }
        .payment-card {
            border: 1px solid var(--border-default);
            border-radius: var(--radius-sm);
            padding: 0.9rem;
            cursor: pointer;
            text-align: center;
            transition: all 0.2s;
            font-size: 0.85rem;
            font-weight: 700;
        }
        .payment-card input {
            display: none;
        }
        .payment-card.active {
            border-color: #222222;
            background: #F7F7F7;
            box-shadow: 0 0 0 1px #222222;
        }
        .payment-details {
            background: var(--bg-surface);
            border: 1px solid var(--border-light);
            border-radius: var(--radius-sm);
            padding: 1.25rem;
            margin-top: 1rem;
        }
    </style>
</head>
<body style="background: var(--bg-surface);">

    <header class="site-header">
        <div class="header-inner">
            <a href="controller.do?btnop=ConsultaTodos" class="brand-link">
                <span>🏖️</span>
                <span>Pousada Paradiso</span>
            </a>
            <div class="nav-right">
                <% if (usuario != null) { %>
                    <span style="font-size: 0.85rem; color: var(--text-secondary);">Logado como <strong><%= usuario.getNomeCompleto() %></strong></span>
                    <a href="controller.do?btnop=MinhasReservas" class="nav-link">Minhas Reservas</a>
                <% } else { %>
                    <a href="login.jsp?redirect=controller.do?btnop=NovaReserva&acomodacaoId=<%= acomodacao.getId() %>" class="nav-link">Já tem cadastro? Entrar</a>
                <% } %>
            </div>
        </div>
    </header>

    <main class="container">
<%
    String msg = (String) request.getAttribute("msg");
    if (msg == null) msg = request.getParameter("msg");
    String tipoMsg = (String) request.getAttribute("tipoMsg");
    if (tipoMsg == null) tipoMsg = request.getParameter("tipoMsg");
%>

        <div style="margin-bottom: 1.5rem;">
            <a href="controller.do?btnop=ConsultaTodos" style="color: var(--text-secondary); text-decoration: none; font-size: 0.875rem;">
                &larr; Voltar para a lista de acomodações
            </a>
        </div>

        <% if (msg != null) { %>
            <div class="message-bar <%= tipoMsg != null ? tipoMsg : "danger" %>" style="margin-bottom: 1.5rem;">
                <%= msg %>
            </div>
        <% } %>

        <form action="controller.do" method="POST" id="formReserva" class="checkout-layout">
            <input type="hidden" name="btnop" value="Cadastra">
            <input type="hidden" name="acomodacaoId" value="<%= acomodacao.getId() %>">
            <input type="hidden" name="txtTipoQuarto" value="<%= acomodacao.getNome() %>">
            <input type="hidden" id="txtValorDiaria" name="txtValorDiaria" value="<%= acomodacao.getValorDiaria() %>">

            <div class="checkout-main">

                <!-- ETAPA 1: DADOS DA RESERVA (SEGREGAÇÃO CLARA) -->
                <div class="step-container">
                    <div class="step-header">
                        <div class="step-number">1</div>
                        <h2 class="step-title">Dados da Reserva</h2>
                    </div>

                    <div style="background: var(--bg-surface); border: 1px solid var(--border-light); border-radius: var(--radius-sm); padding: 1rem; margin-bottom: 1.25rem; display: flex; align-items: center; justify-content: space-between;">
                        <div>
                            <span style="font-size: 0.75rem; text-transform: uppercase; font-weight: 700; color: var(--text-secondary);">Acomodação selecionada</span>
                            <div style="font-weight: 800; font-size: 1rem; margin-top: 0.15rem;"><%= acomodacao.getNome() %></div>
                            <div style="font-size: 0.8rem; color: var(--text-secondary);"><%= acomodacao.getTipo() %> • Capacidade até <%= acomodacao.getCapacidadePessoas() %> pessoas</div>
                        </div>
                        <div style="text-align: right;">
                            <div style="font-weight: 800; font-size: 1.1rem;">R$ <%= String.format("%.0f", acomodacao.getValorDiaria()) %></div>
                            <span style="font-size: 0.75rem; color: var(--text-secondary);">por diária</span>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="txtCheckIn">Data de entrada (Check-in)</label>
                            <input type="date" id="txtCheckIn" name="txtCheckIn" class="input-text" value="<%= checkInParam %>" required onchange="calcularTotais()">
                        </div>
                        <div class="form-group">
                            <label for="txtCheckOut">Data de saída (Check-out)</label>
                            <input type="date" id="txtCheckOut" name="txtCheckOut" class="input-text" value="<%= checkOutParam %>" required onchange="calcularTotais()">
                        </div>
                    </div>

                    <div class="form-group" style="margin-bottom: 1.5rem;">
                        <label for="txtQtdHospedes">Número de hóspedes</label>
                        <select id="txtQtdHospedes" name="txtQtdHospedes" class="select-text" style="max-width: 220px;" onchange="calcularTotais()">
                            <% for (int i = 1; i <= acomodacao.getCapacidadePessoas(); i++) { %>
                                <option value="<%= i %>" <%= String.valueOf(i).equals(qtdHospedesParam) ? "selected" : "" %>><%= i %> <%= i == 1 ? "pessoa" : "pessoas" %></option>
                            <% } %>
                        </select>
                    </div>

                    <label style="font-size: 0.85rem; font-weight: 700; display: block; margin-bottom: 0.6rem; color: var(--text-primary);">
                        Serviços adicionais contratados na reserva (1:N)
                    </label>

                    <label class="service-option">
                        <input type="checkbox" id="chkCafe" name="chkCafe" value="true" onchange="calcularTotais()">
                        <div class="service-info">
                            <strong>Café da Manhã Colonial (+ R$ 65 / diária)</strong>
                            <span>Buffet artesanal completo servido na varanda do quarto.</span>
                        </div>
                    </label>

                    <label class="service-option">
                        <input type="checkbox" id="chkTransfer" name="chkTransfer" value="true" onchange="calcularTotais()">
                        <div class="service-info">
                            <strong>Translado Executivo Aeroporto (+ R$ 180 taxa única)</strong>
                            <span>Veículo privativo climatizado ida e volta aeroporto/pousada.</span>
                        </div>
                    </label>

                    <label class="service-option">
                        <input type="checkbox" id="chkPasseio" name="chkPasseio" value="true" onchange="calcularTotais()">
                        <div class="service-info">
                            <strong>Passeio de Escuna nas Ilhas (+ R$ 120 / pessoa)</strong>
                            <span>Roteiro por praias preservadas com parada para mergulho.</span>
                        </div>
                    </label>

                    <label class="service-option" style="margin-bottom: 0;">
                        <input type="checkbox" id="chkSpa" name="chkSpa" value="true" onchange="calcularTotais()">
                        <div class="service-info">
                            <strong>Massagem Terapêutica Relaxante (+ R$ 150)</strong>
                            <span>Sessão individual de 50 minutos com aromaterapia.</span>
                        </div>
                    </label>
                </div>

                <!-- ETAPA 2: IDENTIFICAÇÃO DO HÓSPEDE (RELACIONAMENTO 1:1) -->
                <div class="step-container">
                    <div class="step-header">
                        <div class="step-number">2</div>
                        <h2 class="step-title">Identificação do Hóspede Titular (1:1)</h2>
                    </div>

                    <% if (usuario != null) { %>
                        <div style="background: var(--bg-surface); border: 1px solid var(--border-light); border-radius: var(--radius-sm); padding: 1.25rem;">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                                <div style="font-weight: 800; font-size: 1.05rem;"><%= usuario.getNomeCompleto() %></div>
                                <span class="status-badge CONFIRMADA">Conta verificada</span>
                            </div>
                            <div style="color: var(--text-secondary); font-size: 0.85rem;">CPF: <strong><%= usuario.getCpf() %></strong> • E-mail: <strong><%= usuario.getEmail() %></strong></div>
                            <div style="color: var(--text-secondary); font-size: 0.85rem; margin-top: 0.2rem;">Telefone: <strong><%= usuario.getTelefone() %></strong> • Origem: <strong><%= usuario.getCidadeOrigem() %></strong></div>
                        </div>
                    <% } else { %>
                        <div class="form-row">
                            <div class="form-group" style="grid-column: span 2;">
                                <label for="txtNomeHospede">Nome completo *</label>
                                <input type="text" id="txtNomeHospede" name="txtNomeHospede" class="input-text" placeholder="Nome completo do responsável" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="txtCpf">CPF *</label>
                                <input type="text" id="txtCpf" name="txtCpf" class="input-text" placeholder="000.000.000-00" required>
                            </div>
                            <div class="form-group">
                                <label for="txtTelefone">Telefone com DDD *</label>
                                <input type="text" id="txtTelefone" name="txtTelefone" class="input-text" placeholder="(11) 98888-7777" required>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label for="txtEmail">E-mail para confirmação *</label>
                                <input type="email" id="txtEmail" name="txtEmail" class="input-text" placeholder="seu@email.com" required>
                            </div>
                            <div class="form-group">
                                <label for="txtCidadeOrigem">Cidade de origem</label>
                                <input type="text" id="txtCidadeOrigem" name="txtCidadeOrigem" class="input-text" placeholder="Ex: São Paulo - SP">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group" style="grid-column: span 2;">
                                <label for="txtSenha">Crie uma senha de acesso *</label>
                                <input type="password" id="txtSenha" name="txtSenha" class="input-text" placeholder="Mínimo de 6 caracteres para acessar suas reservas" required minlength="6">
                            </div>
                        </div>
                    <% } %>
                </div>

                <!-- ETAPA 3: PAGAMENTO POR ÚLTIMO (SEGREGAÇÃO CLARA) -->
                <div class="step-container">
                    <div class="step-header">
                        <div class="step-number">3</div>
                        <h2 class="step-title">Pagamento</h2>
                    </div>

                    <label style="font-size: 0.85rem; font-weight: 700; display: block; margin-bottom: 0.6rem;">
                        Escolha como deseja pagar:
                    </label>

                    <div class="payment-methods">
                        <label class="payment-card active" id="cardPix" onclick="selecionarPagamento('PIX')">
                            <input type="radio" name="txtFormaPagamento" value="PIX" checked>
                            <div>⚡ PIX</div>
                            <div style="font-size: 0.7rem; color: #137333; font-weight: 600;">5% de desconto</div>
                        </label>

                        <label class="payment-card" id="cardCartao" onclick="selecionarPagamento('CARTAO_CREDITO')">
                            <input type="radio" name="txtFormaPagamento" value="CARTAO_CREDITO">
                            <div>💳 Cartão de Crédito</div>
                            <div style="font-size: 0.7rem; color: var(--text-secondary); font-weight: 400;">Em até 6x sem juros</div>
                        </label>

                        <label class="payment-card" id="cardTransf" onclick="selecionarPagamento('TRANSFERENCIA')">
                            <input type="radio" name="txtFormaPagamento" value="TRANSFERENCIA">
                            <div>🏦 Transferência</div>
                            <div style="font-size: 0.7rem; color: var(--text-secondary); font-weight: 400;">Envio de comprovante</div>
                        </label>

                        <label class="payment-card" id="cardPresencial" onclick="selecionarPagamento('DINHEIRO')">
                            <input type="radio" name="txtFormaPagamento" value="DINHEIRO">
                            <div>💵 No Check-In</div>
                            <div style="font-size: 0.7rem; color: var(--text-secondary); font-weight: 400;">Dinheiro ou débito</div>
                        </label>
                    </div>

                    <div id="detalhesPix" class="payment-details">
                        <div style="font-weight: 700; margin-bottom: 0.3rem;">Pagamento instantâneo via PIX</div>
                        <div style="font-size: 0.85rem; color: var(--text-secondary);">
                            O QR Code e a chave de cópia do PIX serão gerados automaticamente na confirmação da reserva com 5% de desconto aplicado.
                        </div>
                    </div>

                    <div id="detalhesCartao" class="payment-details" style="display: none;">
                        <div style="font-weight: 700; margin-bottom: 0.6rem;">Dados do cartão de crédito</div>
                        <div class="form-row">
                            <div class="form-group" style="grid-column: span 2;">
                                <label>Número do cartão</label>
                                <input type="text" class="input-text" placeholder="0000 0000 0000 0000">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>Validade</label>
                                <input type="text" class="input-text" placeholder="MM/AA">
                            </div>
                            <div class="form-group">
                                <label>CVV</label>
                                <input type="text" class="input-text" placeholder="123">
                            </div>
                        </div>
                    </div>

                    <div class="form-group" style="margin-top: 1.25rem;">
                        <label for="txtObservacoes">Observações ou solicitações especiais</label>
                        <textarea id="txtObservacoes" name="txtObservacoes" class="input-text" rows="2" placeholder="Ex: previsão de horário de chegada, berço extra, restrições alimentares..."></textarea>
                    </div>
                </div>

            </div>

            <!-- Coluna Direita: Resumo Fixo de Preços -->
            <aside class="checkout-sidebar">
                <div class="checkout-card">
                    <div class="summary-acc-thumb">
                        <img src="<%= acomodacao.getImagemUrl() %>" alt="<%= acomodacao.getNome() %>" class="summary-img">
                        <div>
                            <div class="summary-type"><%= acomodacao.getTipo() %></div>
                            <h4 class="summary-title"><%= acomodacao.getNome() %></h4>
                            <div style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.2rem;">
                                ★ <%= acomodacao.getAvaliacao() %> (<%= acomodacao.getTotalAvaliacoes() %> avaliações)
                            </div>
                        </div>
                    </div>

                    <div class="price-breakdown">
                        <div class="price-item">
                            <span>R$ <%= String.format("%.0f", acomodacao.getValorDiaria()) %> x <span id="numNoites">5</span> noites</span>
                            <span id="subtotalDiarias">R$ 2.250,00</span>
                        </div>
                        <div class="price-item" id="rowDesconto" style="color: var(--success-text); display: none;">
                            <span>Desconto longa estadia</span>
                            <span id="valorDesconto">- R$ 0,00</span>
                        </div>
                        <div class="price-item" id="rowDescontoPix" style="color: var(--success-text);">
                            <span>Desconto 5% PIX</span>
                            <span id="valorDescontoPix">- R$ 112,50</span>
                        </div>
                        <div class="price-item" id="rowServicos" style="display: none;">
                            <span>Serviços adicionais</span>
                            <span id="valorServicos">R$ 0,00</span>
                        </div>
                        <div class="price-item">
                            <span>Taxa de serviço da pousada</span>
                            <span style="color: var(--success-text); font-weight: 700;">Grátis</span>
                        </div>
                        <div class="price-total">
                            <span>Total a pagar</span>
                            <span id="totalFinal">R$ 2.137,50</span>
                        </div>
                    </div>

                    <button type="submit" class="btn-confirm">
                        Confirmar e pagar
                    </button>

                    <div style="font-size: 0.75rem; color: var(--text-secondary); text-align: center; margin-top: 0.85rem;">
                        Cancelamento flexível até 48h antes do check-in.
                    </div>
                </div>
            </aside>
        </form>
    </main>

    <script>
        var formaPagamentoAtual = "PIX";

        function selecionarPagamento(metodo) {
            formaPagamentoAtual = metodo;
            document.querySelectorAll('.payment-card').forEach(function(card) {
                card.classList.remove('active');
            });

            var cardMap = {
                'PIX': 'cardPix',
                'CARTAO_CREDITO': 'cardCartao',
                'TRANSFERENCIA': 'cardTransf',
                'DINHEIRO': 'cardPresencial'
            };

            var selected = document.getElementById(cardMap[metodo]);
            if (selected) {
                selected.classList.add('active');
                selected.querySelector('input').checked = true;
            }

            document.getElementById('detalhesPix').style.display = (metodo === 'PIX') ? 'block' : 'none';
            document.getElementById('detalhesCartao').style.display = (metodo === 'CARTAO_CREDITO') ? 'block' : 'none';

            calcularTotais();
        }

        function calcularTotais() {
            var checkIn = document.getElementById("txtCheckIn").value;
            var checkOut = document.getElementById("txtCheckOut").value;
            var valorDiaria = parseFloat(document.getElementById("txtValorDiaria").value) || 250;

            var d1 = new Date(checkIn);
            var d2 = new Date(checkOut);
            var diffTime = d2.getTime() - d1.getTime();
            var noites = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            if (isNaN(noites) || noites <= 0) noites = 1;

            document.getElementById("numNoites").innerText = noites;
            var subtotalDiarias = noites * valorDiaria;
            document.getElementById("subtotalDiarias").innerText = "R$ " + subtotalDiarias.toLocaleString('pt-BR', {minimumFractionDigits: 2});

            // Desconto longa estadia
            var descontoEstadia = 0;
            if (noites >= 7) {
                descontoEstadia = subtotalDiarias * 0.15;
            } else if (noites >= 4) {
                descontoEstadia = subtotalDiarias * 0.10;
            }

            var rowDesc = document.getElementById("rowDesconto");
            if (descontoEstadia > 0) {
                rowDesc.style.display = "flex";
                document.getElementById("valorDesconto").innerText = "- R$ " + descontoEstadia.toLocaleString('pt-BR', {minimumFractionDigits: 2});
            } else {
                rowDesc.style.display = "none";
            }

            // Serviços
            var totalServicos = 0;
            if (document.getElementById("chkCafe").checked) totalServicos += (65 * noites);
            if (document.getElementById("chkTransfer").checked) totalServicos += 180;
            if (document.getElementById("chkPasseio").checked) totalServicos += (120 * parseInt(document.getElementById("txtQtdHospedes").value || 2));
            if (document.getElementById("chkSpa").checked) totalServicos += 150;

            var rowServ = document.getElementById("rowServicos");
            if (totalServicos > 0) {
                rowServ.style.display = "flex";
                document.getElementById("valorServicos").innerText = "R$ " + totalServicos.toLocaleString('pt-BR', {minimumFractionDigits: 2});
            } else {
                rowServ.style.display = "none";
            }

            var subtotal = subtotalDiarias - descontoEstadia + totalServicos;

            // Desconto PIX 5%
            var descontoPix = 0;
            var rowPix = document.getElementById("rowDescontoPix");
            if (formaPagamentoAtual === "PIX") {
                descontoPix = subtotal * 0.05;
                rowPix.style.display = "flex";
                document.getElementById("valorDescontoPix").innerText = "- R$ " + descontoPix.toLocaleString('pt-BR', {minimumFractionDigits: 2});
            } else {
                rowPix.style.display = "none";
            }

            var totalFinal = subtotal - descontoPix;
            document.getElementById("totalFinal").innerText = "R$ " + totalFinal.toLocaleString('pt-BR', {minimumFractionDigits: 2});
        }

        window.onload = function() {
            selecionarPagamento('PIX');
        };
    </script>

</body>
</html>
