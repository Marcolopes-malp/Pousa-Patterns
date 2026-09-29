package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.ItemServico;
import util.FabricaConexao;

/**
 * Padrão de Projeto DAO para Itens de Serviços Adicionais (Relacionamento 1:N).
 */
public class ItemServicoDAO {

    public int cadastrar(ItemServico item) throws ClassNotFoundException, SQLException {
        String sql = "INSERT INTO itens_servicos (reserva_id, nome, descricao, preco_unitario, quantidade) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                    ItemServico item = new ItemServico();
                    item.setId(rs.getInt("id"));
                    item.setReservaId(rs.getInt("reserva_id"));
                    item.setNome(rs.getString("nome"));
                    item.setDescricao(rs.getString("descricao"));
                    item.setPrecoUnitario(rs.getDouble("preco_unitario"));
                    item.setQuantidade(rs.getInt("quantidade"));
                    lista.add(item);
                }
            }
        }
        return lista;
    }

    public void deletarPorReserva(int reservaId) throws ClassNotFoundException, SQLException {
        String sql = "DELETE FROM itens_servicos WHERE reserva_id = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, reservaId);
            comando.executeUpdate();
        }
    }
}
