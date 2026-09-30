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
        try (Connection con = FabricaConexao.getConexao()) {
            return cadastrar(con, hospede);
        }
    }

    public int cadastrar(Connection con, Hospede hospede) throws SQLException {
        String sql = "INSERT INTO hospedes (nome_completo, cpf, email, telefone, cidade_origem, senha, perfil) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement comando = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, hospede.getNomeCompleto());
            comando.setString(2, hospede.getCpf());
            comando.setString(3, hospede.getEmail());
            comando.setString(4, hospede.getTelefone());
            comando.setString(5, hospede.getCidadeOrigem());

            String senha = hospede.getSenha();
            String hashParaSalvar = (senha != null && senha.startsWith("PBKDF2$"))
                    ? senha
                    : util.Seguranca.gerarHashSenha(senha != null ? senha : "123456");

            comando.setString(6, hashParaSalvar);
            comando.setString(7, hospede.getPerfil() != null ? hospede.getPerfil() : "CLIENTE");
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
        if (email == null || senha == null) {
            return null;
        }

        Hospede hospede = buscarPorEmail(email.trim());
        if (hospede == null) {
            return null;
        }

        if (util.Seguranca.verificarSenha(senha.trim(), hospede.getSenha())) {
            // Se a senha no banco ainda estava em texto puro (legado), migra automaticamente para PBKDF2
            if (hospede.getSenha() != null && !hospede.getSenha().startsWith("PBKDF2$")) {
                atualizarSenha(hospede.getId(), util.Seguranca.gerarHashSenha(senha.trim()));
            }
            return hospede;
        }

        return null;
    }

    public void atualizarSenha(int id, String hashSenha) throws ClassNotFoundException, SQLException {
        String sql = "UPDATE hospedes SET senha = ? WHERE id = ?";
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setString(1, hashSenha);
            comando.setInt(2, id);
            comando.executeUpdate();
        }
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
        try (Connection con = FabricaConexao.getConexao()) {
            atualizar(con, hospede);
        }
    }

    public void atualizar(Connection con, Hospede hospede) throws SQLException {
        String sql = "UPDATE hospedes SET nome_completo = ?, cpf = ?, email = ?, telefone = ?, cidade_origem = ? WHERE id = ?";
        try (PreparedStatement comando = con.prepareStatement(sql)) {
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
        try {
            String p = rs.getString("perfil");
            h.setPerfil(p != null ? p : "CLIENTE");
        } catch (SQLException ignored) {
            h.setPerfil("CLIENTE");
        }
        return h;
    }
}
