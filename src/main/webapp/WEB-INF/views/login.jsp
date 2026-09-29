<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="util.Html"%>
<%
    String redirect = request.getParameter("redirect");
    if (redirect == null || redirect.trim().isEmpty()) {
        redirect = (String) request.getAttribute("redirect");
    }
    if (redirect == null) redirect = "";
    String msg = (String) request.getAttribute("msg");
    if (msg == null) msg = request.getParameter("msg");
    String tipoMsg = (String) request.getAttribute("tipoMsg");
    if (tipoMsg == null) tipoMsg = request.getParameter("tipoMsg");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Entrar | Pousada Paradiso</title>
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

    <div style="max-width: 440px; margin: 3rem auto; width: 100%; padding: 0 1rem;">
        <div style="background: #FFFFFF; border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: 2rem; box-shadow: var(--shadow-card);">
            <h2 style="font-size: 1.4rem; font-weight: 800; margin-bottom: 0.3rem;">Acesse sua conta</h2>
            <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.5rem;">
                Gerencie suas reservas e acompanhe seus check-ins.
            </p>

            <% if (msg != null) { %>
                <div class="message-bar <%= tipoMsg != null ? Html.esc(tipoMsg) : "danger" %>">
                    <%= Html.esc(msg) %>
                </div>
            <% } %>

            <form action="controller.do" method="POST">
                <input type="hidden" name="btnop" value="LoginCliente">
                <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                <input type="hidden" name="redirect" value="<%= Html.esc(redirect) %>">

                <div class="form-group" style="margin-bottom: 1rem;">
                    <label for="txtEmail">E-mail</label>
                    <input type="email" id="txtEmail" name="txtEmail" class="input-text" placeholder="seu@email.com" required>
                </div>

                <div class="form-group" style="margin-bottom: 1.5rem;">
                    <label for="txtSenha">Senha</label>
                    <input type="password" id="txtSenha" name="txtSenha" class="input-text" placeholder="Sua senha" required>
                </div>

                <button type="submit" class="btn-primary-action" style="width: 100%; padding: 0.85rem; border-radius: var(--radius-sm);">
                    Entrar
                </button>
            </form>

            <div style="margin-top: 1rem; padding: 0.75rem; background: var(--bg-surface); border-radius: var(--radius-sm); font-size: 0.75rem; color: var(--text-secondary); text-align: center; border: 1px dashed var(--border-default);">
                <strong>Contas de demonstração para testes:</strong><br>
                Recepção: <code>recepcao@pousada.com.br</code> (senha: <code>admin123</code>)<br>
                Hóspede: <code>marco.pedro@pousada.com.br</code> (senha: <code>123456</code>)
            </div>

            <div style="margin-top: 1.25rem; padding-top: 1rem; border-top: 1px solid var(--border-light); font-size: 0.85rem; text-align: center; color: var(--text-secondary);">
                <%
                    String linkCadastro = "controller.do?btnop=Cadastro" + (redirect != null && !redirect.trim().isEmpty() ? "&redirect=" + java.net.URLEncoder.encode(redirect.trim(), "UTF-8") : "");
                %>
                Ainda não tem conta? <a href="<%= Html.esc(linkCadastro) %>" style="color: var(--text-primary); font-weight: 700; text-decoration: underline;">Cadastre-se aqui</a>
            </div>
        </div>
    </div>

</body>
</html>
