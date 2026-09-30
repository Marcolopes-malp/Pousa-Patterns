<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Hospede"%>
<%@page import="model.Acomodacao"%>
<%@page import="util.Html"%>
<%
    Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");
    List<Acomodacao> acomodacoes = (List<Acomodacao>) request.getAttribute("acomodacoes");
    if (acomodacoes == null) {
        acomodacoes = new java.util.ArrayList<>();
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Pousada Paradiso | Acomodações e Reservas</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>

    <%@ include file="fragments/header.jspf" %>

    <main class="container">
        <section class="hero-compact">
            <h1>Acomodações à beira-mar e na mata</h1>
            <p>Escolha uma acomodação, confira a disponibilidade e reserve com confirmação imediata.</p>

            <form action="controller.do" method="GET" class="search-strip">
                <input type="hidden" name="btnop" value="NovaReserva">
                <div class="search-item">
                    <span>Acomodação</span>
                    <select name="acomodacaoId">
                        <% for (Acomodacao a : acomodacoes) { %>
                            <option value="<%= a.getId() %>"><%= Html.esc(a.getNome()) %></option>
                        <% } %>
                    </select>
                </div>
                <%
                    String hojeIndex = java.time.LocalDate.now().toString();
                    String padraoInIndex = java.time.LocalDate.now().plusDays(1).toString();
                    String padraoOutIndex = java.time.LocalDate.now().plusDays(6).toString();
                %>
                <div class="search-item">
                    <span>Entrada</span>
                    <input type="date" name="txtCheckIn" value="<%= padraoInIndex %>" min="<%= hojeIndex %>">
                </div>
                <div class="search-item">
                    <span>Saída</span>
                    <input type="date" name="txtCheckOut" value="<%= padraoOutIndex %>" min="<%= padraoInIndex %>">
                </div>
                <div class="search-item">
                    <span>Hóspedes</span>
                    <select name="txtQtdHospedes">
                        <option value="1">1 pessoa</option>
                        <option value="2" selected>2 pessoas</option>
                        <option value="3">3 pessoas</option>
                        <option value="4">4 pessoas</option>
                    </select>
                </div>
                <button type="submit" class="btn-search">Buscar vagas</button>
            </form>
        </section>

        <section>
            <div class="accommodations-grid">
                <% for (Acomodacao a : acomodacoes) { %>
                <div class="acc-card">
                    <div class="acc-image-wrapper">
                        <img src="<%= Html.esc(a.getImagemUrl()) %>" alt="<%= Html.esc(a.getNome()) %>" class="acc-image" loading="lazy">
                        <div class="demand-pill">
                            <span class="demand-dot"></span>
                            <% if (a.getVagasRestantes() <= 2) { %>
                                Restam <%= a.getVagasRestantes() %> vagas
                            <% } else { %>
                                <%= a.getVagasRestantes() %> vagas disponíveis
                            <% } %>
                        </div>
                    </div>

                    <div class="acc-content">
                        <div class="acc-header">
                            <h3 class="acc-title"><%= Html.esc(a.getNome()) %></h3>
                            <div class="acc-rating">★ <%= a.getAvaliacao() %> (<%= a.getTotalAvaliacoes() %>)</div>
                        </div>
                        <div class="acc-subtitle">Até <%= a.getCapacidadePessoas() %> hóspedes • <%= Html.esc(a.getTipo()) %></div>
                        <div class="acc-amenities"><%= Html.esc(a.getComodidades()) %></div>

                        <div class="acc-price-row">
                            <div class="acc-price">
                                <strong>R$ <%= String.format("%.0f", a.getValorDiaria()) %></strong> / noite
                            </div>
                            <a href="controller.do?btnop=NovaReserva&acomodacaoId=<%= a.getId() %>" class="btn-reserve-sm">
                                Reservar
                            </a>
                        </div>
                    </div>
                </div>
                <% } %>
            </div>
        </section>
    </main>

    <%@ include file="fragments/footer.jspf" %>

</body>
</html>
