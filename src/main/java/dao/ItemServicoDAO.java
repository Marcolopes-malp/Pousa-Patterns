package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.ItemServico;
import util.FabricaConexao;

/**
 * Padrão de Projeto DAO para Itens de Serviços Adicionais (Relacionamento 1:N).
 */
public class ItemServicoDAO {

    public int cadastrar(ItemServico item) throws ClassNotFoundException, SQLException {
        try (Connection con = FabricaConexao.getConexao()) {
            return cadastrar(con, item);
        }
    }

    public int cadastrar(Connection con, ItemServico item) throws SQLException {
        String sql = "INSERT INTO itens_servicos (reserva_id, nome, descricao, preco_unitario, quantidade) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement comando = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setInt(1, item.getReservaId());
            comando.setString(2, item.getNome());
            comando.setString(3, item.getDescricao());
            comando.setDouble(4, item.getPrecoUnitario());
            comando.setInt(5, item.getQuantidade());
            comando.executeUpdate();

            try (ResultSet rs = comando.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    item.setId(idGerado);
                    return idGerado;
                }
            }
        }
        return item.getId();
    }

    public List<ItemServico> listarPorReserva(int reservaId) throws ClassNotFoundException, SQLException {
        List<ItemServico> lista = new ArrayList<>();
        String sql = "SELECT * FROM itens_servicos WHERE reserva_id = ? ORDER BY id ASC";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, reservaId);
            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearItemServico(rs));
                }
            }
        }
        return lista;
    }

    /**
     * D1: Eliminação de problema N+1.
     * Busca todos os serviços associados a uma lista de reservas em um único comando SQL.
     */
    public Map<Integer, List<ItemServico>> listarPorMultiplasReservas(List<Integer> reservaIds) throws ClassNotFoundException, SQLException {
        Map<Integer, List<ItemServico>> mapa = new HashMap<>();
        if (reservaIds == null || reservaIds.isEmpty()) {
            return mapa;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < reservaIds.size(); i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = "SELECT * FROM itens_servicos WHERE reserva_id IN (" + placeholders + ") ORDER BY id ASC";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            for (int i = 0; i < reservaIds.size(); i++) {
                comando.setInt(i + 1, reservaIds.get(i));
            }
            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    ItemServico item = mapearItemServico(rs);
                    mapa.computeIfAbsent(item.getReservaId(), k -> new ArrayList<>()).add(item);
                }
            }
        }
        return mapa;
    }

    public void deletarPorReserva(int reservaId) throws ClassNotFoundException, SQLException {
        try (Connection con = FabricaConexao.getConexao()) {
            deletarPorReserva(con, reservaId);
        }
    }

    public void deletarPorReserva(Connection con, int reservaId) throws SQLException {
        String sql = "DELETE FROM itens_servicos WHERE reserva_id = ?";
        try (PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, reservaId);
            comando.executeUpdate();
        }
    }

    private ItemServico mapearItemServico(ResultSet rs) throws SQLException {
        ItemServico item = new ItemServico();
        item.setId(rs.getInt("id"));
        item.setReservaId(rs.getInt("reserva_id"));
        item.setNome(rs.getString("nome"));
        item.setDescricao(rs.getString("descricao"));
        item.setPrecoUnitario(rs.getDouble("preco_unitario"));
        item.setQuantidade(rs.getInt("quantidade"));
        return item;
    }
}
