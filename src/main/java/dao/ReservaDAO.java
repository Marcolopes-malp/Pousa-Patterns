package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Hospede;
import model.ItemServico;
import model.Reserva;
import util.FabricaConexao;

/**
 * Padrão de Projeto DAO (Data Access Object) para a entidade principal Reserva.
 * Mapeia todos os 11+ atributos e gerencia os relacionamentos 1:1 (Hospede) e 1:N (ItemServico).
 */
public class ReservaDAO {

    private final HospedeDAO hospedeDAO = new HospedeDAO();
    private final ItemServicoDAO servicoDAO = new ItemServicoDAO();

    public int cadastrar(Reserva reserva) throws ClassNotFoundException, SQLException {
        // Se a reserva possuir um novo hóspede sem ID cadastrado, salva o hóspede primeiro
        if (reserva.getHospede() != null && reserva.getHospede().getId() <= 0) {
            int hospedeId = hospedeDAO.cadastrar(reserva.getHospede());
            reserva.getHospede().setId(hospedeId);
        }

        String sql = "INSERT INTO reservas (codigo_localizador, data_checkin, data_checkout, " +
                "quantidade_hospedes, tipo_quarto, valor_diaria, valor_total, status, " +
                "forma_pagamento, observacoes, data_criacao, hospede_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setString(1, reserva.getCodigoLocalizador());
            comando.setString(2, reserva.getDataCheckIn());
            comando.setString(3, reserva.getDataCheckOut());
            comando.setInt(4, reserva.getQuantidadeHospedes());
            comando.setString(5, reserva.getTipoQuarto());
            comando.setDouble(6, reserva.getValorDiaria());
            comando.setDouble(7, reserva.getValorTotal());
            comando.setString(8, reserva.getStatus());
            comando.setString(9, reserva.getFormaPagamento());
            comando.setString(10, reserva.getObservacoes());
            comando.setString(11, reserva.getDataCriacao());

            if (reserva.getHospede() != null && reserva.getHospede().getId() > 0) {
                comando.setInt(12, reserva.getHospede().getId());
            } else {
                comando.setNull(12, java.sql.Types.INTEGER);
            }

            comando.executeUpdate();

            try (ResultSet rs = comando.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    reserva.setId(idGerado);

                    // Salva os serviços adicionais vinculados à reserva (1:N)
                    if (reserva.getServicos() != null) {
                        for (ItemServico s : reserva.getServicos()) {
                            s.setReservaId(idGerado);
                            servicoDAO.cadastrar(s);
                        }
                    }
                    return idGerado;
                }
            }
        }
        return reserva.getId();
    }

    public void atualizar(Reserva reserva) throws ClassNotFoundException, SQLException {
        if (reserva.getHospede() != null && reserva.getHospede().getId() > 0) {
            hospedeDAO.atualizar(reserva.getHospede());
        }

        String sql = "UPDATE reservas SET codigo_localizador = ?, data_checkin = ?, data_checkout = ?, " +
                "quantidade_hospedes = ?, tipo_quarto = ?, valor_diaria = ?, valor_total = ?, " +
                "status = ?, forma_pagamento = ?, observacoes = ?, hospede_id = ? " +
                "WHERE id = ?";

        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {

            comando.setString(1, reserva.getCodigoLocalizador());
            comando.setString(2, reserva.getDataCheckIn());
            comando.setString(3, reserva.getDataCheckOut());
            comando.setInt(4, reserva.getQuantidadeHospedes());
            comando.setString(5, reserva.getTipoQuarto());
            comando.setDouble(6, reserva.getValorDiaria());
            comando.setDouble(7, reserva.getValorTotal());
            comando.setString(8, reserva.getStatus());
            comando.setString(9, reserva.getFormaPagamento());
            comando.setString(10, reserva.getObservacoes());

            if (reserva.getHospede() != null && reserva.getHospede().getId() > 0) {
                comando.setInt(11, reserva.getHospede().getId());
            } else {
                comando.setNull(11, java.sql.Types.INTEGER);
            }
            comando.setInt(12, reserva.getId());

            comando.executeUpdate();
        }
    }

    public void deletar(int id) throws ClassNotFoundException, SQLException {
        // Remove os serviços relacionados primeiro (garante integridade em caso de restrição)
        servicoDAO.deletarPorReserva(id);

        String sql = "DELETE FROM reservas WHERE id = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    public Reserva consultarById(int id) throws ClassNotFoundException, SQLException {
        String sql = "SELECT * FROM reservas WHERE id = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return mapearReservaCompleta(rs);
                }
            }
        }
        return null;
    }

    public List<Reserva> consultarTodos() throws ClassNotFoundException, SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reservas ORDER BY id DESC";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql);
             ResultSet rs = comando.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearReservaCompleta(rs));
            }
        }
        return lista;
    }

    public List<Reserva> listarPorHospede(int hospedeId) throws ClassNotFoundException, SQLException {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT * FROM reservas WHERE hospede_id = ? ORDER BY id DESC";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, hospedeId);
            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReservaCompleta(rs));
                }
            }
        }
        return lista;
    }

    private Reserva mapearReservaCompleta(ResultSet rs) throws SQLException, ClassNotFoundException {
        Reserva r = new Reserva();
        r.setId(rs.getInt("id"));
        r.setCodigoLocalizador(rs.getString("codigo_localizador"));
        r.setDataCheckIn(rs.getString("data_checkin"));
        r.setDataCheckOut(rs.getString("data_checkout"));
        r.setQuantidadeHospedes(rs.getInt("quantidade_hospedes"));
        r.setTipoQuarto(rs.getString("tipo_quarto"));
        r.setValorDiaria(rs.getDouble("valor_diaria"));
        r.setValorTotal(rs.getDouble("valor_total"));
        r.setStatus(rs.getString("status"));
        r.setFormaPagamento(rs.getString("forma_pagamento"));
        r.setObservacoes(rs.getString("observacoes"));
        r.setDataCriacao(rs.getString("data_criacao"));

        int hospedeId = rs.getInt("hospede_id");
        if (hospedeId > 0) {
            Hospede h = hospedeDAO.consultarById(hospedeId);
            r.setHospede(h);
        }

        // Carrega os itens 1:N
        List<ItemServico> servicos = servicoDAO.listarPorReserva(r.getId());
        r.setServicos(servicos);

        return r;
    }
}
