<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Reserva"%>
<%@page import="dao.ReservaDAO"%>
<%
    ReservaDAO dao = new ReservaDAO();
    List<Reserva> lista = dao.consultarTodos();

    int totalReservas = (lista != null) ? lista.size() : 0;
    long confirmadas = 0;
    double faturamentoTotal = 0.0;
    int totalHospedes = 0;

    if (lista != null) {
        for (Reserva r : lista) {
            if ("CONFIRMADA".equalsIgnoreCase(r.getStatus()) || "CHECKIN_ATIVO".equalsIgnoreCase(r.getStatus())) {
                confirmadas++;
            }
            faturamentoTotal += r.getValorTotal();
            totalHospedes += r.getQuantidadeHospedes();
        }
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Painel da Recepção | Pousada Paradiso</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .admin-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.875rem;
            margin-top: 1rem;
        }
        .admin-table th, .admin-table td {
            padding: 0.85rem 1rem;
            border-bottom: 1px solid var(--border-light);
            text-align: left;
        }
        .admin-table th {
            background: var(--bg-surface);
            font-weight: 700;
            color: var(--text-secondary);
            font-size: 0.75rem;
            text-transform: uppercase;
        }
    </style>
</head>
<body style="background: #FAFAFA;">

    <header class="site-header">
        <div class="header-inner">
            <a href="controller.do?btnop=ConsultaTodos" class="brand-link">
                <span>🏖️</span>
                <span>Pousada Paradiso</span>
                <span class="brand-badge" style="background: #222222; color: #FFFFFF;">Painel da Recepção</span>
            </a>
            <div class="nav-right">
                <a href="controller.do?btnop=ConsultaTodos" class="nav-link">&larr; Visão do Cliente</a>
                <a href="formCadastro.jsp" class="btn-primary-action" style="padding: 0.5rem 1rem; font-size: 0.85rem;">+ Nova Reserva Manual</a>
            </div>
        </div>
    </header>

    <main class="container">
        <div style="display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 2rem;">
            <div>
                <h1 style="font-size: 1.8rem; font-weight: 800; letter-spacing: -0.02em;">Gestão de Reservas</h1>
                <p style="color: var(--text-secondary); font-size: 0.95rem;">
                    Visão administrativa completa para avaliação de CRUD, DAO e padrões de projeto.
                </p>
            </div>
            <div style="display: flex; gap: 1rem;">
                <div style="background: #FFFFFF; border: 1px solid var(--border-default); padding: 0.6rem 1rem; border-radius: var(--radius-sm); font-size: 0.85rem;">
                    Total de reservas: <strong><%= totalReservas %></strong>
                </div>
                <div style="background: #FFFFFF; border: 1px solid var(--border-default); padding: 0.6rem 1rem; border-radius: var(--radius-sm); font-size: 0.85rem;">
                    Faturamento: <strong>R$ <%= String.format("%.2f", faturamentoTotal) %></strong>
                </div>
            </div>
        </div>

        <div style="background: #FFFFFF; border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: 1.5rem; box-shadow: var(--shadow-card);">
            <div style="overflow-x: auto;">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>Localizador</th>
                            <th>Hóspede (1:1)</th>
                            <th>Período</th>
                            <th>Acomodação</th>
                            <th>Valor Total</th>
                            <th>Status</th>
                            <th style="text-align: right;">Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (lista != null && !lista.isEmpty()) { %>
                            <% for (Reserva r : lista) {
                                String nomeHospede = (r.getHospede() != null) ? r.getHospede().getNomeCompleto() : "Não informado";
                            %>
                            <tr>
                                <td>
                                    <strong><%= r.getCodigoLocalizador() %></strong>
                                    <div style="font-size: 0.75rem; color: var(--text-muted);">ID #<%= r.getId() %></div>
                                </td>
                                <td>
                                    <strong><%= nomeHospede %></strong>
                                    <div style="font-size: 0.75rem; color: var(--text-secondary);"><%= (r.getHospede() != null) ? r.getHospede().getTelefone() : "" %></div>
                                </td>
                                <td>
                                    <div><%= r.getDataCheckIn() %> &rarr; <%= r.getDataCheckOut() %></div>
                                    <div style="font-size: 0.75rem; color: var(--text-muted);"><%= r.getQuantidadeHospedes() %> hóspedes</div>
                                </td>
                                <td><%= r.getTipoQuarto() %></td>
                                <td><strong>R$ <%= String.format("%.2f", r.getValorTotal()) %></strong></td>
                                <td><span class="status-badge <%= r.getStatus() %>"><%= r.getStatus() %></span></td>
                                <td style="text-align: right; white-space: nowrap;">
                                    <a href="controller.do?btnop=ConsultaById&id=<%= r.getId() %>" class="btn-secondary-action" style="padding: 0.35rem 0.7rem; font-size: 0.8rem;">Ver</a>
                                    <a href="controller.do?btnop=Edita&id=<%= r.getId() %>" class="btn-secondary-action" style="padding: 0.35rem 0.7rem; font-size: 0.8rem;">Editar</a>
                                    <a href="controller.do?btnop=ProcessarCheckInAutomatico&id=<%= r.getId() %>" class="btn-primary-action" style="padding: 0.35rem 0.7rem; font-size: 0.8rem; background: #059669;">Check-in</a>
                                    <a href="controller.do?btnop=Deleta&id=<%= r.getId() %>" class="btn-secondary-action" style="padding: 0.35rem 0.7rem; font-size: 0.8rem; color: #C5221F;" onclick="return confirm('Confirma a exclusão desta reserva?')">Excluir</a>
                                </td>
                            </tr>
                            <% } %>
                        <% } else { %>
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 2rem; color: var(--text-secondary);">
                                    Nenhuma reserva registrada.
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <footer class="site-footer">
        <div class="footer-inner">
            <div>© 2026 Pousada Paradiso. Painel de Gestão da Recepção.</div>
            <div>Aluno: Marco Antonio Lopes Pedro (G14) • Universidade de Mogi das Cruzes</div>
        </div>
    </footer>

</body>
</html>
