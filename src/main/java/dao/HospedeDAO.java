package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Hospede;
import util.FabricaConexao;

public class HospedeDAO {

    public int cadastrar(Hospede hospede) throws ClassNotFoundException, SQLException {
        String sql = "INSERT INTO hospedes (nome_completo, cpf, email, telefone, cidade_origem, senha) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, hospede.getNomeCompleto());
            comando.setString(2, hospede.getCpf());
            comando.setString(3, hospede.getEmail());
            comando.setString(4, hospede.getTelefone());
            comando.setString(5, hospede.getCidadeOrigem());
            comando.setString(6, hospede.getSenha() != null ? hospede.getSenha() : "123456");
            comando.executeUpdate();

            try (ResultSet rs = comando.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGerado = rs.getInt(1);
                    hospede.setId(idGerado);
                    return idGerado;
                }
            }
        }
        return hospede.getId();
    }

    public Hospede autenticar(String email, String senha) throws ClassNotFoundException, SQLException {
        String sql = "SELECT * FROM hospedes WHERE email = ? AND senha = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setString(1, email != null ? email.trim() : "");
            comando.setString(2, senha != null ? senha.trim() : "");
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return mapearHospede(rs);
                }
            }
        }
        return null;
    }

    public Hospede buscarPorEmail(String email) throws ClassNotFoundException, SQLException {
        String sql = "SELECT * FROM hospedes WHERE email = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setString(1, email != null ? email.trim() : "");
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return mapearHospede(rs);
                }
            }
        }
        return null;
    }

    public void atualizar(Hospede hospede) throws ClassNotFoundException, SQLException {
        String sql = "UPDATE hospedes SET nome_completo = ?, cpf = ?, email = ?, telefone = ?, cidade_origem = ? WHERE id = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setString(1, hospede.getNomeCompleto());
            comando.setString(2, hospede.getCpf());
            comando.setString(3, hospede.getEmail());
            comando.setString(4, hospede.getTelefone());
            comando.setString(5, hospede.getCidadeOrigem());
            comando.setInt(6, hospede.getId());
            comando.executeUpdate();
        }
    }

    public void deletar(int id) throws ClassNotFoundException, SQLException {
        String sql = "DELETE FROM hospedes WHERE id = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, id);
            comando.executeUpdate();
        }
    }

    public Hospede consultarById(int id) throws ClassNotFoundException, SQLException {
        String sql = "SELECT * FROM hospedes WHERE id = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return mapearHospede(rs);
                }
            }
        }
        return null;
    }

    public List<Hospede> consultarTodos() throws ClassNotFoundException, SQLException {
        List<Hospede> lista = new ArrayList<>();
        String sql = "SELECT * FROM hospedes ORDER BY nome_completo ASC";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql);
             ResultSet rs = comando.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearHospede(rs));
            }
        }
        return lista;
    }

    private Hospede mapearHospede(ResultSet rs) throws SQLException {
        Hospede h = new Hospede();
        h.setId(rs.getInt("id"));
        h.setNomeCompleto(rs.getString("nome_completo"));
        h.setCpf(rs.getString("cpf"));
        h.setEmail(rs.getString("email"));
        h.setTelefone(rs.getString("telefone"));
        h.setCidadeOrigem(rs.getString("cidade_origem"));
        try {
            h.setSenha(rs.getString("senha"));
        } catch (SQLException ignored) {}
        return h;
    }
}
