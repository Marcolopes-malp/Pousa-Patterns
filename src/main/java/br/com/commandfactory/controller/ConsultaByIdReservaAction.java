package br.com.commandfactory.controller;

import dao.ReservaDAO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Hospede;
import model.Reserva;

/**
 * Comando para Consultar uma Reserva por ID com todos os detalhes e serviços.
 * Controle de acesso: restrito ao hóspede titular ou à recepção.
 */
public class ConsultaByIdReservaAction implements ICommand {

    @Override
    public String executar(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            HttpSession session = request.getSession();
            Hospede usuario = (Hospede) session.getAttribute("usuarioLogado");

            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                request.setAttribute("msg", "ID da reserva não informado.");
                request.setAttribute("tipoMsg", "warning");
                return "resultado.jsp";
            }
            int id = Integer.parseInt(idParam.trim());

            if (usuario == null) {
                response.sendRedirect("controller.do?btnop=Login&redirect=" + java.net.URLEncoder.encode("controller.do?btnop=ConsultaById&id=" + id, "UTF-8"));
                return null;
            }

            ReservaDAO dao = new ReservaDAO();
            Reserva reserva = dao.consultarById(id);

            if (reserva == null) {
                request.setAttribute("msg", "Reserva não encontrada para o ID: " + id);
                request.setAttribute("tipoMsg", "warning");
                return "resultado.jsp";
            }

            // S2: Controle de Acesso: apenas a recepção ou o próprio titular da reserva podem acessar
            if (!usuario.isRecepcao() && (reserva.getHospede() == null || reserva.getHospede().getId() != usuario.getId())) {
                request.setAttribute("msg", "Acesso não autorizado: você não tem permissão para visualizar esta reserva.");
                request.setAttribute("tipoMsg", "danger");
                return "resultado.jsp";
            }

            request.setAttribute("reserva", reserva);
            return "detalhesReserva.jsp";

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("msg", "Erro ao consultar reserva por ID: " + e.getMessage());
            request.setAttribute("tipoMsg", "danger");
            return "resultado.jsp";
        }
    }
}
