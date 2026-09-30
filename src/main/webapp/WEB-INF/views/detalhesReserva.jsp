<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Reserva"%>
<%@page import="model.Hospede"%>
<%@page import="model.ItemServico"%>
<%@page import="util.Html"%>
<%
    Reserva r = (Reserva) request.getAttribute("reserva");
    if (r == null) {
        response.sendRedirect("controller.do?btnop=ConsultaTodos");
        return;
    }
    Hospede h = r.getHospede();
    if (h == null) h = new Hospede();

    Object pinAcesso = request.getAttribute("pinAcesso");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Comprovante de Reserva <%= Html.esc(r.getCodigoLocalizador()) %> | Pousada Paradiso</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <%@ include file="fragments/header.jspf" %>

    <main class="container" style="max-width: 860px;">

        <% if (pinAcesso != null) { %>
        <div class="message-bar success" style="padding: 1.25rem; font-size: 1rem; margin-bottom: 2rem;">
            <div style="font-weight: 800; font-size: 1.1rem; margin-bottom: 0.25rem;">
                Check-in online realizado com sucesso
            </div>
            <div>
                PIN de acesso da fechadura digital: <strong style="font-size: 1.2rem; letter-spacing: 2px;"><%= Html.esc(pinAcesso) %></strong>
            </div>
            <div style="font-size: 0.85rem; font-weight: 400; margin-top: 0.4rem; color: #1E4620;">
                O quarto está liberado para sua chegada a partir das 14h00.
            </div>
        </div>
        <% } %>

        <div style="background: #FFFFFF; border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: 2.5rem; box-shadow: var(--shadow-card);">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 1.5rem; border-bottom: 1px solid var(--border-light); margin-bottom: 1.5rem;">
                <div>
                    <span class="status-badge <%= Html.esc(r.getStatus()) %>" style="margin-bottom: 0.5rem;"><%= Html.esc(r.getStatus()) %></span>
                    <h1 style="font-size: 1.6rem; font-weight: 800; letter-spacing: -0.02em;">
                        Reserva <%= Html.esc(r.getCodigoLocalizador()) %>
                    </h1>
                    <p style="color: var(--text-secondary); font-size: 0.85rem;">
                        Emitida em <%= Html.esc(r.getDataCriacao()) %>
                    </p>
                </div>
                <div style="display: flex; gap: 0.5rem;">
                    <% if ("CONFIRMADA".equalsIgnoreCase(r.getStatus())) { %>
                        <form method="POST" action="controller.do" style="display:inline;">
                            <input type="hidden" name="btnop" value="ProcessarCheckInAutomatico">
                            <input type="hidden" name="id" value="<%= r.getId() %>">
                            <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>">
                            <button type="submit" class="btn-primary-action" style="background: #059669; padding: 0.55rem 1rem; font-size: 0.85rem; border:none; cursor:pointer;">
                                Fazer check-in agora
                            </button>
                        </form>
                    <% } %>
                    <button onclick="window.print()" class="btn-secondary-action" style="padding: 0.55rem 1rem; font-size: 0.85rem;">
                        Imprimir comprovante
                    </button>
                </div>
            </div>

            <!-- Dados da Estadia e Hóspede (A6: grid responsivo) -->
            <div class="reserva-grid-2col">
                <div style="background: var(--bg-surface); border: 1px solid var(--border-light); border-radius: var(--radius-sm); padding: 1.25rem;">
                    <h3 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-secondary);">
                        Acomodação
                    </h3>
                    <div style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.25rem;"><%= Html.esc(r.getTipoQuarto()) %></div>
                    <div style="color: var(--text-secondary); font-size: 0.85rem;">Entrada: <strong><%= Html.esc(r.getDataCheckIn()) %></strong> (a partir das 14h)</div>
                    <div style="color: var(--text-secondary); font-size: 0.85rem;">Saída: <strong><%= Html.esc(r.getDataCheckOut()) %></strong> (até as 12h)</div>
                    <div style="color: var(--text-secondary); font-size: 0.85rem; margin-top: 0.25rem;"><%= r.getQuantidadeHospedes() %> hóspedes</div>
                </div>

                <div style="background: var(--bg-surface); border: 1px solid var(--border-light); border-radius: var(--radius-sm); padding: 1.25rem;">
                    <h3 style="font-size: 0.95rem; font-weight: 700; margin-bottom: 0.75rem; text-transform: uppercase; letter-spacing: 0.5px; color: var(--text-secondary);">
                        Titular da reserva
                    </h3>
                    <div style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.25rem;"><%= Html.esc(h.getNomeCompleto()) %></div>
                    <div style="color: var(--text-secondary); font-size: 0.85rem;"><%= Html.esc(h.getEmail()) %></div>
                    <div style="color: var(--text-secondary); font-size: 0.85rem;"><%= Html.esc(h.getTelefone()) %></div>
                    <div style="color: var(--text-secondary); font-size: 0.85rem;">CPF: <%= Html.esc(h.getCpf()) %></div>
                </div>
            </div>

            <!-- Serviços Adicionais (1:N) -->
            <% if (r.getServicos() != null && !r.getServicos().isEmpty()) { %>
            <div style="margin-bottom: 2rem;">
                <h3 style="font-size: 1rem; font-weight: 700; margin-bottom: 0.75rem;">Serviços adicionais contratados</h3>
                <div style="border: 1px solid var(--border-light); border-radius: var(--radius-sm); overflow: hidden;">
                    <% for (ItemServico s : r.getServicos()) { %>
                    <div style="display: flex; justify-content: space-between; padding: 0.75rem 1rem; border-bottom: 1px solid var(--border-light);">
                        <div>
                            <strong><%= Html.esc(s.getNome()) %></strong>
                            <div style="font-size: 0.8rem; color: var(--text-secondary);"><%= Html.esc(s.getDescricao()) %></div>
                        </div>
                        <div style="text-align: right;">
                            <div>R$ <%= String.format("%.2f", s.getSubtotal()) %></div>
                            <div style="font-size: 0.75rem; color: var(--text-muted);"><%= s.getQuantidade() %>x R$ <%= String.format("%.2f", s.getPrecoUnitario()) %></div>
                        </div>
                    </div>
                    <% } %>
                </div>
            </div>
            <% } %>

            <!-- Totalização -->
            <div style="border-top: 1px solid var(--border-light); padding-top: 1.5rem; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <div style="font-size: 0.85rem; color: var(--text-secondary);">Forma de pagamento</div>
                    <div style="font-weight: 700;"><%= Html.esc(r.getFormaPagamento()) %></div>
                </div>
                <div style="text-align: right;">
                    <div style="font-size: 0.85rem; color: var(--text-secondary);">Valor total pago / a pagar</div>
                    <div style="font-size: 1.8rem; font-weight: 800; color: var(--text-primary);">
                        R$ <%= String.format("%.2f", r.getValorTotal()) %>
                    </div>
                </div>
            </div>

            <% if (r.getObservacoes() != null && !r.getObservacoes().isEmpty()) { %>
            <div style="margin-top: 1.5rem; padding: 1rem; background: var(--bg-surface); border-radius: var(--radius-sm); font-size: 0.85rem; color: var(--text-secondary);">
                <strong>Observações:</strong> <%= Html.esc(r.getObservacoes()) %>
            </div>
            <% } %>

            <div style="margin-top: 2rem; display: flex; justify-content: space-between;">
                <a href="controller.do?btnop=MinhasReservas" class="btn-secondary-action" style="font-size: 0.85rem;">
                    &larr; Ver minhas reservas
                </a>
                <a href="controller.do?btnop=ConsultaTodos" class="btn-primary-action" style="font-size: 0.85rem;">
                    Página inicial
                </a>
            </div>
        </div>
    </main>

    <%@ include file="fragments/footer.jspf" %>

</body>
</html>
