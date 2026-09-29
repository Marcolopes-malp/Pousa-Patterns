<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Reserva"%>
<%@page import="model.Hospede"%>
<%@page import="model.ItemServico"%>
<%@page import="util.Html"%>
<%
    Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");
    if (usuario == null) {
        response.sendRedirect("controller.do?btnop=Login");
        return;
    }
    List<Reserva> lista = (List<Reserva>) request.getAttribute("listaMinhasReservas");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Minhas Reservas | Pousada Paradiso</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <header class="site-header">
        <div class="header-inner">
            <a href="controller.do?btnop=ConsultaTodos" class="brand-link">
                <span>🏖️</span>
                <span>Pousada Paradiso</span>
            </a>
            <div class="nav-right">
                <a href="controller.do?btnop=ConsultaTodos" class="nav-link">Explorar Acomodações</a>
                <div class="user-pill">
                    <div class="user-avatar"><%= Html.esc(usuario.getNomeCompleto().substring(0, 1).toUpperCase()) %></div>
                    <span><%= Html.esc(usuario.getNomeCompleto().split(" ")[0]) %></span>
                </div>
                <form action="controller.do" method="POST" style="display:inline;">
                    <input type="hidden" name="btnop" value="LogoutCliente">
                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                    <button type="submit" class="nav-link" style="background:none; border:none; color: var(--text-secondary); font-size: 0.85rem; cursor:pointer;">Sair</button>
                </form>
            </div>
        </div>
    </header>

    <main class="container" style="max-width: 980px;">
        <div style="margin-bottom: 2rem;">
            <h1 style="font-size: 1.8rem; font-weight: 800; letter-spacing: -0.02em; margin-bottom: 0.3rem;">
                Minhas viagens e reservas
            </h1>
            <p style="color: var(--text-secondary); font-size: 0.95rem;">
                Acompanhe o status das suas estadias, realize o check-in online e visualize seus comprovantes.
            </p>
        </div>

        <% if (lista != null && !lista.isEmpty()) { %>
            <% for (Reserva r : lista) { %>
                <div class="booking-card">
                    <div>
                        <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.5rem;">
                            <span class="status-badge <%= Html.esc(r.getStatus()) %>"><%= Html.esc(r.getStatus()) %></span>
                            <span style="font-size: 0.8rem; color: var(--text-secondary);">Código: <strong><%= Html.esc(r.getCodigoLocalizador()) %></strong></span>
                        </div>

                        <h3 style="font-size: 1.25rem; font-weight: 800; margin-bottom: 0.35rem;"><%= Html.esc(r.getTipoQuarto()) %></h3>
                        <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 0.75rem;">
                            📅 <%= Html.esc(r.getDataCheckIn()) %> &rarr; <%= Html.esc(r.getDataCheckOut()) %> • <%= r.getQuantidadeHospedes() %> hóspedes
                        </p>

                        <% if (r.getServicos() != null && !r.getServicos().isEmpty()) { %>
                            <div style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 0.75rem;">
                                <strong>Serviços inclusos:</strong>
                                <% for (ItemServico s : r.getServicos()) { %>
                                    <span style="background: var(--bg-surface); padding: 0.15rem 0.45rem; border-radius: 4px; margin-right: 0.3rem;"><%= Html.esc(s.getNome()) %></span>
                                <% } %>
                            </div>
                        <% } %>

                        <% if (r.getObservacoes() != null && !r.getObservacoes().isEmpty()) { %>
                            <div style="font-size: 0.8rem; background: #F8FAFC; border: 1px solid var(--border-light); padding: 0.5rem 0.75rem; border-radius: var(--radius-sm); color: #334155;">
                                <%= Html.esc(r.getObservacoes()) %>
                            </div>
                        <% } %>
                    </div>

                    <div style="display: flex; flex-direction: column; justify-content: space-between; align-items: flex-end; border-left: 1px solid var(--border-light); padding-left: 1.5rem;">
                        <div style="text-align: right;">
                            <span style="font-size: 0.8rem; color: var(--text-secondary);">Valor total:</span>
                            <div style="font-size: 1.4rem; font-weight: 800;">R$ <%= String.format("%.2f", r.getValorTotal()) %></div>
                            <span style="font-size: 0.75rem; color: var(--text-secondary);"><%= Html.esc(r.getFormaPagamento()) %></span>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 0.5rem; width: 100%; margin-top: 1rem;">
                            <a href="controller.do?btnop=ConsultaById&id=<%= r.getId() %>" class="btn-secondary-action" style="padding: 0.5rem; font-size: 0.85rem; text-align: center;">
                                Ver comprovante
                            </a>
                            <% if ("CONFIRMADA".equalsIgnoreCase(r.getStatus())) { %>
                                <form method="POST" action="controller.do" style="width: 100%;">
                                    <input type="hidden" name="btnop" value="ProcessarCheckInAutomatico">
                                    <input type="hidden" name="id" value="<%= r.getId() %>">
                                    <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                                    <button type="submit" class="btn-primary-action" style="width: 100%; padding: 0.5rem; font-size: 0.85rem; text-align: center; background: #059669; border:none; cursor:pointer;">
                                        Fazer check-in online
                                    </button>
                                </form>
                            <% } %>
                        </div>
                    </div>
                </div>
            <% } %>
        <% } else { %>
            <div style="background: var(--bg-surface); border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: 3rem; text-align: center;">
                <h3 style="font-size: 1.2rem; font-weight: 700; margin-bottom: 0.5rem;">Nenhuma reserva encontrada</h3>
                <p style="color: var(--text-secondary); font-size: 0.95rem; margin-bottom: 1.5rem;">
                    Você ainda não tem reservas ativas. Escolha uma das nossas acomodações para sua próxima viagem.
                </p>
                <a href="controller.do?btnop=ConsultaTodos" class="btn-primary-action">
                    Explorar acomodações
                </a>
            </div>
        <% } %>
    </main>

    <footer class="site-footer">
        <div class="footer-inner">
            <div>© 2026 Pousada Paradiso. Avaliação M1 - Engenharia de Software (UMC).</div>
            <div>Aluno: Marco Antonio Lopes Pedro (G14)</div>
        </div>
    </footer>

</body>
</html>
