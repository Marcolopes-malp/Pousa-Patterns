<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="util.Html"%>
<%
    String msg = (String) request.getAttribute("msg");
    if (msg == null) {
        msg = "Operação concluída com sucesso.";
    }
    String tipoMsg = (String) request.getAttribute("tipoMsg");
    if (tipoMsg == null) {
        tipoMsg = "success";
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Aviso | Pousada Paradiso</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body style="background: var(--bg-surface); min-height: 100vh; display: flex; flex-direction: column;">

    <%@ include file="fragments/header.jspf" %>

    <div style="flex: 1; display: flex; align-items: center; justify-content: center; padding: 3rem 1rem;">
        <div style="max-width: 520px; width: 100%;">
            <div style="background: #FFFFFF; border: 1px solid var(--border-default); border-radius: var(--radius-md); padding: 2.5rem; text-align: center; box-shadow: var(--shadow-card);">
                <div class="message-bar <%= Html.esc(tipoMsg) %>" style="margin-bottom: 1.5rem; font-size: 1rem;">
                    <%= Html.esc(msg) %>
                </div>

                <div style="display: flex; justify-content: center; gap: 1rem; margin-top: 2rem;">
                    <a href="controller.do?btnop=MinhasReservas" class="btn-primary-action" style="font-size: 0.9rem;">
                        Minhas reservas
                    </a>
                    <a href="controller.do?btnop=ConsultaTodos" class="btn-secondary-action" style="font-size: 0.9rem;">
                        Página inicial
                    </a>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="fragments/footer.jspf" %>

</body>
</html>
