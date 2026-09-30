package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Padrão Factory para gerenciamento centralizado de conexões JDBC.
 * Conforme especificado na tarefa D3:
 * - Seleção de banco por variável de ambiente (DB_URL / DB_ENGINE / DB_PATH), com H2 como padrão.
 * - Eliminação de fallback silencioso em tempo de execução que fragmenta dados.
 * - Inicialização do schema thread-safe a partir de schema.sql e listener de contexto.
 * - Compatibilidade plena de sintaxe SQL entre MySQL e H2.
 */
public class FabricaConexao {

    private static final Logger LOGGER = Logger.getLogger(FabricaConexao.class.getName());
    private static volatile boolean tabelasInicializadas = false;

    public static String getUrlConfigurada() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }

        String engine = System.getenv("DB_ENGINE");
        if ("mysql".equalsIgnoreCase(engine)) {
            return "jdbc:mysql://localhost:3306/pousada_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        }

        String dbPath = System.getenv("DB_PATH");
        if (dbPath == null || dbPath.trim().isEmpty()) {
            dbPath = "./pousada_db";
        }
        return "jdbc:h2:" + dbPath.trim() + ";DB_CLOSE_DELAY=-1;MODE=MySQL";
    }

    public static String getUserConfigurado() {
        String envUser = System.getenv("DB_USER");
        if (envUser != null) {
            return envUser;
        }
        return getUrlConfigurada().contains("mysql") ? "root" : "sa";
    }

    public static String getPassConfigurado() {
        String envPass = System.getenv("DB_PASS");
        return (envPass != null) ? envPass : "";
    }

    public static Connection getConexao() throws ClassNotFoundException, SQLException {
        String url = getUrlConfigurada();
        if (url.contains("mysql")) {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } else if (url.contains("h2")) {
            Class.forName("org.h2.Driver");
        }

        Connection con = DriverManager.getConnection(url, getUserConfigurado(), getPassConfigurado());

        if (!tabelasInicializadas) {
            synchronized (FabricaConexao.class) {
                if (!tabelasInicializadas) {
                    inicializarBanco(con);
                    tabelasInicializadas = true;
                }
            }
        }

        return con;
    }

    public static synchronized void inicializarSchema() throws ClassNotFoundException, SQLException {
        try (Connection con = getConexao()) {
            if (!tabelasInicializadas) {
                inicializarBanco(con);
                tabelasInicializadas = true;
            }
        }
    }

    private static void inicializarBanco(Connection con) {
        executarSchemaSql(con);
        popularDadosIniciais(con);
    }

    private static void executarSchemaSql(Connection con) {
        try (InputStream in = FabricaConexao.class.getResourceAsStream("/schema.sql")) {
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                     Statement stmt = con.createStatement()) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("--") || line.isEmpty()) continue;
                        sb.append(line).append(" ");
                        if (line.endsWith(";")) {
                            String sql = sb.toString().replace(";", "").trim();
                            if (!sql.isEmpty()) {
                                stmt.execute(sql);
                            }
                            sb.setLength(0);
                        }
                    }
                }
            } else {
                // Fallback DDL inline caso executado fora do empacotamento JAR/WAR
                executarDDLInline(con);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao carregar schema.sql via stream, executando DDL inline", e);
            executarDDLInline(con);
        }
    }

    private static void executarDDLInline(Connection con) {
        try (Statement stmt = con.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS hospedes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome_completo VARCHAR(150) NOT NULL, " +
                    "cpf VARCHAR(20) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "telefone VARCHAR(30) NOT NULL, " +
                    "cidade_origem VARCHAR(100), " +
                    "senha VARCHAR(255), " +
                    "perfil VARCHAR(20) DEFAULT 'CLIENTE')");

            stmt.execute("CREATE TABLE IF NOT EXISTS acomodacoes (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "nome VARCHAR(100) NOT NULL UNIQUE, " +
                    "tipo VARCHAR(50) NOT NULL, " +
                    "descricao VARCHAR(500), " +
                    "capacidade_pessoas INT NOT NULL, " +
                    "valor_diaria DOUBLE NOT NULL, " +
                    "vagas_restantes INT NOT NULL, " +
                    "avaliacao DOUBLE NOT NULL, " +
                    "total_avaliacoes INT NOT NULL, " +
                    "imagem_url VARCHAR(255), " +
                    "comodidades VARCHAR(255))");

            stmt.execute("CREATE TABLE IF NOT EXISTS reservas (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "codigo_localizador VARCHAR(30) NOT NULL UNIQUE, " +
                    "data_checkin VARCHAR(20) NOT NULL, " +
                    "data_checkout VARCHAR(20) NOT NULL, " +
                    "quantidade_hospedes INT NOT NULL, " +
                    "tipo_quarto VARCHAR(80) NOT NULL, " +
                    "valor_diaria DOUBLE NOT NULL, " +
                    "valor_total DOUBLE NOT NULL, " +
                    "status VARCHAR(30) NOT NULL, " +
                    "forma_pagamento VARCHAR(50) NOT NULL, " +
                    "observacoes VARCHAR(500), " +
                    "data_criacao VARCHAR(30) NOT NULL, " +
                    "hospede_id INT, " +
                    "acomodacao_id INT, " +
                    "FOREIGN KEY (hospede_id) REFERENCES hospedes(id) ON DELETE SET NULL, " +
                    "FOREIGN KEY (acomodacao_id) REFERENCES acomodacoes(id) ON DELETE SET NULL)");

            stmt.execute("CREATE TABLE IF NOT EXISTS itens_servicos (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "reserva_id INT NOT NULL, " +
                    "nome VARCHAR(100) NOT NULL, " +
                    "descricao VARCHAR(255), " +
                    "preco_unitario DOUBLE NOT NULL, " +
                    "quantidade INT NOT NULL, " +
                    "FOREIGN KEY (reserva_id) REFERENCES reservas(id) ON DELETE CASCADE)");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Erro na criação DDL inline", ex);
        }
    }

    private static void popularDadosIniciais(Connection con) {
        try (Statement stmt = con.createStatement()) {
            String hashAdmin = Seguranca.gerarHashSenha("admin123");
            String hashCliente = Seguranca.gerarHashSenha("123456");

            // Seed acomodações
            try (ResultSet rsAcom = stmt.executeQuery("SELECT count(*) FROM acomodacoes")) {
                if (rsAcom.next() && rsAcom.getInt(1) == 0) {
                    stmt.execute("INSERT INTO acomodacoes (id, nome, tipo, descricao, capacidade_pessoas, valor_diaria, vagas_restantes, avaliacao, total_avaliacoes, imagem_url, comodidades) VALUES " +
                            "(1, 'Bangalô Vista Mar & Deck Privativo', 'Bangalô', 'Bangalô exclusivo situado a 30 metros da areia, com varanda panorâmica, rede de descanso e banheira de hidromassagem externa.', 2, 450.0, 2, 4.97, 48, 'https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?auto=format&fit=crop&w=800&q=80', 'Wi-Fi • Hidro • Vista Mar • Café incluso • Ar Split'), " +
                            "(2, 'Suíte Master com Hidro & Lareira', 'Suíte', 'Ampla suíte com cama super king, banheira de hidromassagem dupla cromoterápica, lareira ecológica e vista para a mata nativa preservada.', 2, 520.0, 1, 4.92, 35, 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80', 'Wi-Fi • Hidro Dupla • Lareira • Cama Super King • Frigobar Retrô'), " +
                            "(3, 'Chalé Família nas Palmeiras', 'Chalé', 'Espaço aconchegante de dois pavimentos cercado por coqueiros e jardim tropical. Perfeito para famílias com crianças.', 4, 380.0, 3, 4.88, 42, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80', 'Wi-Fi • Cozinha Compacta • 2 Quartos • Deck com Churrasqueira • Estacionamento'), " +
                            "(4, 'Suíte Standard Jardim Colonial', 'Suíte', 'Acomodação confortável e silenciosa no casarão colonial principal, com piso em madeira de demolição e vista para o jardim interno.', 2, 250.0, 4, 4.85, 29, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80', 'Wi-Fi • Ar Split • Cama Queen • Chuveiro a Gás • Mesa de Trabalho')");
                }
            }

            // Seed usuários
            try (ResultSet rsHosp = stmt.executeQuery("SELECT count(*) FROM hospedes")) {
                if (rsHosp.next() && rsHosp.getInt(1) == 0) {
                    stmt.execute("INSERT INTO hospedes (nome_completo, cpf, email, telefone, cidade_origem, senha, perfil) VALUES " +
                            "('Recepção Pousada Paradiso', '000.000.000-00', 'recepcao@pousada.com.br', '(11) 3333-4444', 'Mogi das Cruzes - SP', '" + hashAdmin + "', 'RECEPCAO'), " +
                            "('Marco Antonio Lopes Pedro', '458.129.384-90', 'marco.pedro@pousada.com.br', '(11) 98765-4321', 'Mogi das Cruzes - SP', '" + hashCliente + "', 'CLIENTE'), " +
                            "('Mariana Silveira Ramos', '321.654.987-12', 'mariana.ramos@email.com', '(11) 97123-8899', 'São Paulo - SP', '" + hashCliente + "', 'CLIENTE'), " +
                            "('Lucas Henrique Prado', '876.543.210-55', 'lucas.prado@email.com', '(21) 99887-1122', 'Rio de Janeiro - RJ', '" + hashCliente + "', 'CLIENTE')");

                    stmt.execute("INSERT INTO reservas (codigo_localizador, data_checkin, data_checkout, quantidade_hospedes, tipo_quarto, valor_diaria, valor_total, status, forma_pagamento, observacoes, data_criacao, hospede_id, acomodacao_id) VALUES " +
                            "('POUS-2026-X01', '2026-10-10', '2026-10-15', 2, 'Bangalô Vista Mar & Deck Privativo', 450.0, 2450.0, 'CONFIRMADA', 'PIX', 'Hóspedes em comemoração de aniversário.', '2026-09-28', 1, 1), " +
                            "('POUS-2026-X02', '2026-11-01', '2026-11-04', 3, 'Chalé Família nas Palmeiras', 380.0, 1340.0, 'PENDENTE', 'CARTAO_CREDITO', 'Solicitou berço para bebê.', '2026-09-28', 2, 3), " +
                            "('POUS-2026-X03', '2026-12-20', '2026-12-27', 2, 'Suíte Master com Hidro & Lareira', 520.0, 3940.0, 'CONFIRMADA', 'PIX', 'Check-in tardio previsto para 21h.', '2026-09-28', 3, 2)");

                    stmt.execute("INSERT INTO itens_servicos (reserva_id, nome, descricao, preco_unitario, quantidade) VALUES " +
                            "(1, 'Café Colonial na Cama', 'Buffet servido na varanda privativa', 65.0, 2), " +
                            "(1, 'Passeio de Escuna', 'Tour paradisíaco pelas ilhas', 120.0, 2), " +
                            "(2, 'Café Colonial na Cama', 'Buffet servido na varanda privativa', 65.0, 3), " +
                            "(3, 'Transfer Executivo', 'Translado privativo aeroporto ida e volta', 180.0, 1), " +
                            "(3, 'Massagem Terapêutica', 'Sessão individual com pedras quentes', 150.0, 1)");
                }
            }

            // Garante que o usuário da recepção sempre exista para demonstração
            try {
                stmt.execute("INSERT INTO hospedes (nome_completo, cpf, email, telefone, cidade_origem, senha, perfil) " +
                        "SELECT 'Recepção Pousada Paradiso', '000.000.000-00', 'recepcao@pousada.com.br', '(11) 3333-4444', 'Mogi das Cruzes - SP', '" + hashAdmin + "', 'RECEPCAO' " +
                        "WHERE NOT EXISTS (SELECT 1 FROM hospedes WHERE email = 'recepcao@pousada.com.br')");
            } catch (Exception ignored) {}

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erro ao popular dados iniciais", e);
        }
    }
}
