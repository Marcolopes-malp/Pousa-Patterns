<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String redirect = request.getParameter("redirect");
    if (redirect == null) redirect = "";
    String msg = (String) request.getAttribute("msg");
    String tipoMsg = (String) request.getAttribute("tipoMsg");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Criar Conta | Pousada Paradiso</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body style="background: var(--bg-surface); min-height: 100vh; display: flex; flex-direction: column;">

    <header class="site-header">
        <div class="header-inner">
            <a href="controller.do?btnop=ConsultaTodos" class="brand-link">
                <span>🏖️</span>
                <span>Pousada Paradiso</span>
            </a>
            <div class="nav-right">
                <a href="controller.do?btnop=ConsultaTodos" class="nav-link">&larr; Voltar para as acomodações</a>
            </div>
        </div>
    </header>

    <div style="max-width: 480px; margin: 2.5rem auto; width: 100%; padding: 0 1rem;">
        <div style="background: #FFFFFF; border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: 2rem; box-shadow: var(--shadow-card);">
            <h2 style="font-size: 1.4rem; font-weight: 800; margin-bottom: 0.3rem;">Criar sua conta</h2>
            <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.5rem;">
                Cadastre seus dados para reservar acomodações e receber suas confirmações.
            </p>

            <% if (msg != null) { %>
                <div class="message-bar <%= tipoMsg != null ? tipoMsg : "danger" %>">
                    <%= msg %>
                </div>
            <% } %>

            <form action="controller.do" method="POST">
                <input type="hidden" name="btnop" value="CadastraCliente">
                <input type="hidden" name="redirect" value="<%= redirect %>">

                <div class="form-group" style="margin-bottom: 0.9rem;">
                    <label for="txtNome">Nome completo</label>
                    <input type="text" id="txtNome" name="txtNome" class="input-text" placeholder="Nome e sobrenome" required>
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

                <div class="form-group" style="margin-bottom: 0.9rem;">
                    <label for="txtEmail">E-mail</label>
                    <input type="email" id="txtEmail" name="txtEmail" class="input-text" placeholder="seu@email.com" required>
                </div>

                <div class="form-group" style="margin-bottom: 0.9rem;">
                    <label for="txtCidade">Cidade de origem</label>
                    <input type="text" id="txtCidade" name="txtCidade" class="input-text" placeholder="Ex: São Paulo - SP">
                </div>

                <div class="form-group" style="margin-bottom: 1.5rem;">
                    <label for="txtSenha">Senha</label>
                    <input type="password" id="txtSenha" name="txtSenha" class="input-text" placeholder="Mínimo 6 caracteres" required>
                </div>

                <button type="submit" class="btn-primary-action" style="width: 100%; padding: 0.85rem; border-radius: var(--radius-sm);">
                    Criar conta
                </button>
            </form>

            <div style="margin-top: 1.5rem; padding-top: 1.25rem; border-top: 1px solid var(--border-light); font-size: 0.85rem; text-align: center; color: var(--text-secondary);">
                Já tem cadastro? <a href="login.jsp?redirect=<%= redirect %>" style="color: var(--text-primary); font-weight: 700; text-decoration: underline;">Faça login</a>
            </div>
        </div>
    </div>

</body>
</html>
