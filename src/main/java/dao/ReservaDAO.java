package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.Acomodacao;
import model.Hospede;
import model.ItemServico;
import model.Reserva;
import util.FabricaConexao;

/**
 * Padrão de Projeto DAO (Data Access Object) para a entidade principal Reserva.
 * Mapeia todos os atributos e gerencia os relacionamentos 1:1 (Hospede) e 1:N (ItemServico).
 *
 * Implementa transações ACID (D2) com commit/rollback em conexão única e
 * consultas otimizadas com JOIN e busca de serviços em lote (D1) eliminando o problema N+1.
 */
public class ReservaDAO {

    private final HospedeDAO hospedeDAO = new HospedeDAO();
    private final ItemServicoDAO servicoDAO = new ItemServicoDAO();
    private final AcomodacaoDAO acomodacaoDAO = new AcomodacaoDAO();

    /**
     * D2: Grava hóspede (se novo), reserva e serviços adicionais (1:N)
     * sob uma única transação atômica (ACID) com controle de commit e rollback.
     */
    public int cadastrar(Reserva reserva) throws ClassNotFoundException, SQLException {
        try (Connection con = FabricaConexao.getConexao()) {
            boolean autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                // 1. Se a reserva possuir um novo hóspede sem ID cadastrado, salva o hóspede primeiro
                if (reserva.getHospede() != null && reserva.getHospede().getId() <= 0) {
                    int hospedeId = hospedeDAO.cadastrar(con, reserva.getHospede());
                    reserva.getHospede().setId(hospedeId);
                }

                String sql = "INSERT INTO reservas (codigo_localizador, data_checkin, data_checkout, " +
                        "quantidade_hospedes, tipo_quarto, valor_diaria, valor_total, status, " +
                        "forma_pagamento, observacoes, data_criacao, hospede_id, acomodacao_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                int idGerado = 0;
                try (PreparedStatement comando = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
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
                    String dataCriacao = reserva.getDataCriacao();
                    if (dataCriacao == null || dataCriacao.trim().isEmpty()) {
                        dataCriacao = java.time.LocalDate.now().toString();
                        reserva.setDataCriacao(dataCriacao);
                    }
                    comando.setString(11, dataCriacao);

                    if (reserva.getHospede() != null && reserva.getHospede().getId() > 0) {
                        comando.setInt(12, reserva.getHospede().getId());
                    } else {
                        comando.setNull(12, java.sql.Types.INTEGER);
                    }

                    int acomId = reserva.getAcomodacaoId();
                    if (acomId <= 0 && reserva.getAcomodacao() != null) {
                        acomId = reserva.getAcomodacao().getId();
                    }
                    if (acomId <= 0 && reserva.getTipoQuarto() != null) {
                        Acomodacao a = acomodacaoDAO.buscarPorNome(reserva.getTipoQuarto());
                        if (a != null) {
                            acomId = a.getId();
                        }
                    }

                    if (acomId > 0) {
                        comando.setInt(13, acomId);
                    } else {
                        comando.setNull(13, java.sql.Types.INTEGER);
                    }

                    comando.executeUpdate();

                    try (ResultSet rs = comando.getGeneratedKeys()) {
                        if (rs.next()) {
                            idGerado = rs.getInt(1);
                            reserva.setId(idGerado);
                        }
                    }
                }

                // 2. Salva os serviços adicionais vinculados à reserva (1:N)
                if (idGerado > 0 && reserva.getServicos() != null) {
                    for (ItemServico s : reserva.getServicos()) {
                        s.setReservaId(idGerado);
                        servicoDAO.cadastrar(con, s);
                    }
                }

                con.commit();
                return idGerado > 0 ? idGerado : reserva.getId();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    /**
     * D2: Atualiza a reserva e seus serviços adicionais em transação única com commit/rollback.
     */
    public void atualizar(Reserva reserva) throws ClassNotFoundException, SQLException {
        try (Connection con = FabricaConexao.getConexao()) {
            boolean autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                // B7: Desacoplamento da edição de hóspede da edição de reserva.
                String sql = "UPDATE reservas SET codigo_localizador = ?, data_checkin = ?, data_checkout = ?, " +
                        "quantidade_hospedes = ?, tipo_quarto = ?, valor_diaria = ?, valor_total = ?, " +
                        "status = ?, forma_pagamento = ?, observacoes = ?, hospede_id = ?, acomodacao_id = ? " +
                        "WHERE id = ?";

                try (PreparedStatement comando = con.prepareStatement(sql)) {
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

                    int acomId = reserva.getAcomodacaoId();
                    if (acomId <= 0 && reserva.getAcomodacao() != null) {
                        acomId = reserva.getAcomodacao().getId();
                    }
                    if (acomId <= 0 && reserva.getTipoQuarto() != null) {
                        Acomodacao a = acomodacaoDAO.buscarPorNome(reserva.getTipoQuarto());
                        if (a != null) {
                            acomId = a.getId();
                        }
                    }

                    if (acomId > 0) {
                        comando.setInt(12, acomId);
                    } else {
                        comando.setNull(12, java.sql.Types.INTEGER);
                    }

                    comando.setInt(13, reserva.getId());
                    comando.executeUpdate();
                }

                // Se houver serviços atualizados na reserva, atualiza atomicamente
                if (reserva.getServicos() != null && !reserva.getServicos().isEmpty()) {
                    servicoDAO.deletarPorReserva(con, reserva.getId());
                    for (ItemServico s : reserva.getServicos()) {
                        s.setReservaId(reserva.getId());
                        servicoDAO.cadastrar(con, s);
                    }
                }

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    /**
     * D2: Exclui a reserva e seus serviços vinculados sob transação única.
     */
    public void deletar(int id) throws ClassNotFoundException, SQLException {
        try (Connection con = FabricaConexao.getConexao()) {
            boolean autoCommitOriginal = con.getAutoCommit();
            con.setAutoCommit(false);
            try {
                servicoDAO.deletarPorReserva(con, id);

                String sql = "DELETE FROM reservas WHERE id = ?";
                try (PreparedStatement comando = con.prepareStatement(sql)) {
                    comando.setInt(1, id);
                    comando.executeUpdate();
                }
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(autoCommitOriginal);
            }
        }
    }

    /**
     * D1: Busca reserva por ID utilizando JOIN com hóspedes.
     */
    public Reserva consultarById(int id) throws ClassNotFoundException, SQLException {
        String sql = "SELECT r.*, " +
                "h.id AS h_id, h.nome_completo AS h_nome, h.cpf AS h_cpf, h.email AS h_email, " +
                "h.telefone AS h_telefone, h.cidade_origem AS h_cidade, h.perfil AS h_perfil " +
                "FROM reservas r " +
                "LEFT JOIN hospedes h ON r.hospede_id = h.id " +
                "WHERE r.id = ?";

        Reserva reserva = null;
        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    reserva = mapearReservaComHospede(rs);
                }
            }
        }

        if (reserva != null) {
            reserva.setServicos(servicoDAO.listarPorReserva(reserva.getId()));
        }
        return reserva;
    }

    /**
     * D1: Eliminação de problema N+1.
     * Utiliza LEFT JOIN com hospedes e busca todos os serviços com um único comando SQL (WHERE reserva_id IN (...)).
     */
    public List<Reserva> consultarTodos() throws ClassNotFoundException, SQLException {
        List<Reserva> lista = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();

        String sql = "SELECT r.*, " +
                "h.id AS h_id, h.nome_completo AS h_nome, h.cpf AS h_cpf, h.email AS h_email, " +
                "h.telefone AS h_telefone, h.cidade_origem AS h_cidade, h.perfil AS h_perfil " +
                "FROM reservas r " +
                "LEFT JOIN hospedes h ON r.hospede_id = h.id " +
                "ORDER BY r.id DESC";

        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql);
             ResultSet rs = comando.executeQuery()) {

            while (rs.next()) {
                Reserva r = mapearReservaComHospede(rs);
                lista.add(r);
                ids.add(r.getId());
            }
        }

        if (!ids.isEmpty()) {
            Map<Integer, List<ItemServico>> mapaServicos = servicoDAO.listarPorMultiplasReservas(ids);
            for (Reserva r : lista) {
                List<ItemServico> itens = mapaServicos.get(r.getId());
                r.setServicos(itens != null ? itens : new ArrayList<>());
            }
        }

        return lista;
    }

    /**
     * D1: Listagem por hóspede com JOIN e busca de serviços em lote.
     */
    public List<Reserva> listarPorHospede(int hospedeId) throws ClassNotFoundException, SQLException {
        List<Reserva> lista = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();

        String sql = "SELECT r.*, " +
                "h.id AS h_id, h.nome_completo AS h_nome, h.cpf AS h_cpf, h.email AS h_email, " +
                "h.telefone AS h_telefone, h.cidade_origem AS h_cidade, h.perfil AS h_perfil " +
                "FROM reservas r " +
                "LEFT JOIN hospedes h ON r.hospede_id = h.id " +
                "WHERE r.hospede_id = ? " +
                "ORDER BY r.id DESC";

        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, hospedeId);
            try (ResultSet rs = comando.executeQuery()) {
                while (rs.next()) {
                    Reserva r = mapearReservaComHospede(rs);
                    lista.add(r);
                    ids.add(r.getId());
                }
            }
        }

        if (!ids.isEmpty()) {
            Map<Integer, List<ItemServico>> mapaServicos = servicoDAO.listarPorMultiplasReservas(ids);
            for (Reserva r : lista) {
                List<ItemServico> itens = mapaServicos.get(r.getId());
                r.setServicos(itens != null ? itens : new ArrayList<>());
            }
        }

        return lista;
    }

    public int contarReservasSobrepostas(int acomodacaoId, String checkIn, String checkOut, int reservaIdIgnorar) throws ClassNotFoundException, SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE acomodacao_id = ? " +
                "AND status <> 'CANCELADA' " +
                "AND data_checkin < ? " +
                "AND data_checkout > ? " +
                (reservaIdIgnorar > 0 ? "AND id <> ?" : "");

        try (Connection con = FabricaConexao.getConexao();
             PreparedStatement comando = con.prepareStatement(sql)) {
            comando.setInt(1, acomodacaoId);
            comando.setString(2, checkOut);
            comando.setString(3, checkIn);
            if (reservaIdIgnorar > 0) {
                comando.setInt(4, reservaIdIgnorar);
            }
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Reserva mapearReservaComHospede(ResultSet rs) throws SQLException {
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

        int hospedeId = rs.getInt("h_id");
        if (hospedeId > 0) {
            Hospede h = new Hospede();
            h.setId(hospedeId);
            h.setNomeCompleto(rs.getString("h_nome"));
            h.setCpf(rs.getString("h_cpf"));
            h.setEmail(rs.getString("h_email"));
            h.setTelefone(rs.getString("h_telefone"));
            h.setCidadeOrigem(rs.getString("h_cidade"));
            h.setPerfil(rs.getString("h_perfil"));
            r.setHospede(h);
        }

        int acomodacaoId = 0;
        try {
            acomodacaoId = rs.getInt("acomodacao_id");
        } catch (SQLException ignored) {}
        r.setAcomodacaoId(acomodacaoId);

        if (acomodacaoId > 0) {
            r.setAcomodacao(acomodacaoDAO.buscarPorId(acomodacaoId));
        } else if (r.getTipoQuarto() != null) {
            Acomodacao a = acomodacaoDAO.buscarPorNome(r.getTipoQuarto());
            if (a != null) {
                r.setAcomodacao(a);
                r.setAcomodacaoId(a.getId());
            }
        }

        return r;
    }
}
