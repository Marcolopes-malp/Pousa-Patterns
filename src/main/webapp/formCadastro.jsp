<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nova Reserva Manual | Pousada Paradiso</title>
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
                <a href="admin.jsp" class="nav-link">&larr; Voltar para a gestão</a>
            </div>
        </div>
    </header>

    <main class="container" style="max-width: 820px;">
        <div style="background: #FFFFFF; border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: 2rem; box-shadow: var(--shadow-card);">
            <h1 style="font-size: 1.5rem; font-weight: 800; margin-bottom: 0.3rem;">Cadastrar reserva manual</h1>
            <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 2rem;">
                Registro direto de hóspede e acomodação pela equipe da recepção.
            </p>

            <form action="controller.do" method="POST">
                <input type="hidden" name="btnop" value="Cadastra">

                <h3 class="section-title">Dados do hóspede titular</h3>
                <div class="form-row">
                    <div class="form-group" style="grid-column: span 2;">
                        <label for="txtNomeHospede">Nome completo</label>
                        <input type="text" id="txtNomeHospede" name="txtNomeHospede" class="input-text" placeholder="Nome do hóspede" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="txtCpf">CPF</label>
                        <input type="text" id="txtCpf" name="txtCpf" class="input-text" placeholder="000.000.000-00" required>
                    </div>
                    <div class="form-group">
                        <label for="txtTelefone">Telefone</label>
                        <input type="text" id="txtTelefone" name="txtTelefone" class="input-text" placeholder="(11) 98888-7777" required>
                    </div>
                </div>
                <div class="form-row" style="margin-bottom: 2rem;">
                    <div class="form-group">
                        <label for="txtEmail">E-mail</label>
                        <input type="email" id="txtEmail" name="txtEmail" class="input-text" placeholder="hospede@email.com" required>
                    </div>
                    <div class="form-group">
                        <label for="txtCidadeOrigem">Cidade de origem</label>
                        <input type="text" id="txtCidadeOrigem" name="txtCidadeOrigem" class="input-text" placeholder="São Paulo - SP">
                    </div>
                </div>

                <h3 class="section-title">Dados da hospedagem</h3>
                <div class="form-row">
                    <div class="form-group">
                        <label for="txtCheckIn">Check-in</label>
                        <input type="date" id="txtCheckIn" name="txtCheckIn" class="input-text" value="2026-10-10" required>
                    </div>
                    <div class="form-group">
                        <label for="txtCheckOut">Check-out</label>
                        <input type="date" id="txtCheckOut" name="txtCheckOut" class="input-text" value="2026-10-15" required>
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label for="txtTipoQuarto">Acomodação</label>
                        <select id="txtTipoQuarto" name="txtTipoQuarto" class="select-text">
                            <option value="Bangalô Vista Mar & Deck Privativo">Bangalô Vista Mar & Deck Privativo</option>
                            <option value="Suíte Master com Hidro & Lareira">Suíte Master com Hidro & Lareira</option>
                            <option value="Chalé Família nas Palmeiras">Chalé Família nas Palmeiras</option>
                            <option value="Suíte Standard Jardim Colonial">Suíte Standard Jardim Colonial</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="txtValorDiaria">Valor da diária (R$)</label>
                        <input type="number" step="0.01" id="txtValorDiaria" name="txtValorDiaria" class="input-text" value="450.00" required>
                    </div>
                </div>
                <div class="form-row" style="margin-bottom: 2rem;">
                    <div class="form-group">
                        <label for="txtQtdHospedes">Hóspedes</label>
                        <input type="number" id="txtQtdHospedes" name="txtQtdHospedes" min="1" max="10" value="2" class="input-text" required>
                    </div>
                    <div class="form-group">
                        <label for="txtFormaPagamento">Forma de pagamento</label>
                        <select id="txtFormaPagamento" name="txtFormaPagamento" class="select-text">
                            <option value="PIX">PIX</option>
                            <option value="CARTAO_CREDITO" selected>Cartão de crédito</option>
                            <option value="TRANSFERENCIA">Transferência bancária</option>
                            <option value="DINHEIRO">Dinheiro</option>
                        </select>
                    </div>
                </div>

                <h3 class="section-title">Serviços adicionais opcionais</h3>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-bottom: 2rem;">
                    <label class="service-option" style="margin: 0;">
                        <input type="checkbox" name="chkCafe" value="true">
                        <div class="service-info">
                            <strong>Café Colonial (+ R$ 65)</strong>
                        </div>
                    </label>
                    <label class="service-option" style="margin: 0;">
                        <input type="checkbox" name="chkTransfer" value="true">
                        <div class="service-info">
                            <strong>Transfer Aeroporto (+ R$ 180)</strong>
                        </div>
                    </label>
                    <label class="service-option" style="margin: 0;">
                        <input type="checkbox" name="chkPasseio" value="true">
                        <div class="service-info">
                            <strong>Passeio de Escuna (+ R$ 120)</strong>
                        </div>
                    </label>
                    <label class="service-option" style="margin: 0;">
                        <input type="checkbox" name="chkSpa" value="true">
                        <div class="service-info">
                            <strong>Massagem Terapêutica (+ R$ 150)</strong>
                        </div>
                    </label>
                </div>

                <div class="form-group" style="margin-bottom: 2rem;">
                    <label for="txtObservacoes">Observações</label>
                    <textarea id="txtObservacoes" name="txtObservacoes" class="input-text" rows="2" placeholder="Observações internas da recepção"></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 1rem;">
                    <a href="admin.jsp" class="btn-secondary-action">Cancelar</a>
                    <button type="submit" class="btn-primary-action">
                        Salvar reserva
                    </button>
                </div>
            </form>
        </div>
    </main>

</body>
</html>
