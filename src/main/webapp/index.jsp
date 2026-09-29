<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Hospede"%>
<%@page import="model.Acomodacao"%>
<%@page import="dao.AcomodacaoDAO"%>
<%
    Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");
    AcomodacaoDAO acomodacaoDAO = new AcomodacaoDAO();
    List<Acomodacao> acomodacoes = acomodacaoDAO.listarTodas();
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

    <header class="site-header">
        <div class="header-inner">
            <a href="controller.do?btnop=ConsultaTodos" class="brand-link">
                <span>🏖️</span>
                <span>Pousada Paradiso</span>
                <span class="brand-badge">Litoral Norte</span>
            </a>

            <div class="nav-right">
                <% if (usuario != null) { %>
                    <a href="controller.do?btnop=MinhasReservas" class="nav-link">Minhas Reservas</a>
                    <div class="user-pill">
                        <div class="user-avatar"><%= usuario.getNomeCompleto().substring(0, 1).toUpperCase() %></div>
                        <span><%= usuario.getNomeCompleto().split(" ")[0] %></span>
                    </div>
                    <a href="controller.do?btnop=LogoutCliente" class="nav-link" style="color: var(--text-secondary); font-size: 0.85rem;">Sair</a>
                <% } else { %>
                    <a href="login.jsp" class="nav-link">Entrar</a>
                    <a href="cadastro.jsp" class="btn-primary-action" style="padding: 0.55rem 1.1rem; font-size: 0.85rem; border-radius: var(--radius-full);">Cadastrar</a>
                <% } %>
            </div>
        </div>
    </header>

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
                            <option value="<%= a.getId() %>"><%= a.getNome() %></option>
                        <% } %>
                    </select>
                </div>
                <div class="search-item">
                    <span>Entrada</span>
                    <input type="date" name="txtCheckIn" value="2026-10-10">
                </div>
                <div class="search-item">
                    <span>Saída</span>
                    <input type="date" name="txtCheckOut" value="2026-10-15">
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
                        <img src="<%= a.getImagemUrl() %>" alt="<%= a.getNome() %>" class="acc-image" loading="lazy">
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
                            <h3 class="acc-title"><%= a.getNome() %></h3>
                            <div class="acc-rating">★ <%= a.getAvaliacao() %> (<%= a.getTotalAvaliacoes() %>)</div>
                        </div>
                        <div class="acc-subtitle">Até <%= a.getCapacidadePessoas() %> hóspedes • <%= a.getTipo() %></div>
                        <div class="acc-amenities"><%= a.getComodidades() %></div>

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

    <footer class="site-footer">
        <div class="footer-inner">
            <div>
                © 2026 Pousada Paradiso. Sistema de Reservas para Avaliação M1 - Eng. Software (UMC).
            </div>
            <div>
                Aluno: Marco Antonio Lopes Pedro (G14) • 
                <a href="admin.jsp" class="footer-link">Painel da Recepção (Gestão Completa)</a>
            </div>
        </div>
    </footer>

</body>
</html>
